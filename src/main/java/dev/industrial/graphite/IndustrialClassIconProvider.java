package dev.industrial.graphite;

import com.intellij.ide.IconProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiReferenceList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.Set;

/**
 * Иконки Java-классов из набора Industrial.
 *
 * Производительность: провайдер вызывается для каждого класса в дереве, автодополнении и поиске,
 * поэтому он НЕ резолвит ничего — ни аннотации, ни суперклассы, ни индексы. Смотрит только на
 * короткие имена аннотаций и имена в extends прямо из PSI/стабов файла. Это дёшево и работает
 * даже во время индексации (DumbAware).
 */
public final class IndustrialClassIconProvider extends IconProvider implements DumbAware {

    private static final int CONFIG = 1, BEAN = 2, TEST = 4;

    private static final Set<String> CONFIG_ANNOTATIONS = Set.of(
            "SpringBootApplication", "Configuration", "AutoConfiguration", "TestConfiguration");

    private static final Set<String> BEAN_ANNOTATIONS = Set.of(
            "Component", "Service", "Repository", "Controller", "RestController",
            "ControllerAdvice", "RestControllerAdvice", "ConfigurationProperties", "FeignClient", "Aspect");

    private static final Set<String> TEST_ANNOTATIONS = Set.of(
            "SpringBootTest", "WebMvcTest", "WebFluxTest", "DataJpaTest", "DataJdbcTest", "JdbcTest",
            "JsonTest", "DataMongoTest", "RestClientTest", "SpringJUnitConfig");

    /** Базовые интерфейсы Spring Data — сравниваются по имени в extends, без резолва. */
    private static final Set<String> SPRING_DATA_REPOSITORIES = Set.of(
            "Repository", "CrudRepository", "ListCrudRepository", "PagingAndSortingRepository",
            "ListPagingAndSortingRepository", "JpaRepository", "JpaSpecificationExecutor",
            "QueryByExampleExecutor", "MongoRepository", "ReactiveCrudRepository", "ReactiveMongoRepository",
            "R2dbcRepository", "ElasticsearchRepository", "ReactiveSortingRepository");

    /** Если в этой версии IDE нет нужного API — один раз выключаемся и оставляем стандартные иконки. */
    private static volatile boolean disabled;

    @Override
    public @Nullable Icon getIcon(@NotNull PsiElement element, int flags) {
        if (disabled || !(element instanceof PsiClass)) return null;
        try {
            return iconFor((PsiClass) element);
        } catch (LinkageError e) {
            disabled = true;
            return null;
        }
    }

    private static Icon iconFor(PsiClass cls) {

        if (cls.isAnnotationType()) return IndustrialIcons.ANNOTATION;

        int kinds = annotationKinds(cls);
        String name = cls.getName();

        if ((kinds & TEST) != 0 || isTestName(name)) return IndustrialIcons.TEST_CLASS;
        if ((kinds & CONFIG) != 0) return IndustrialIcons.SPRING;
        if (cls.isEnum()) return IndustrialIcons.ENUM;

        boolean bean = (kinds & BEAN) != 0;
        if (cls.isInterface()) {
            return bean || extendsAny(cls, SPRING_DATA_REPOSITORIES)
                    ? IndustrialIcons.INTERFACE_BEAN : IndustrialIcons.INTERFACE;
        }
        if (cls.isRecord()) return bean ? IndustrialIcons.RECORD_BEAN : IndustrialIcons.RECORD;
        if (isExceptionName(name) || extendsException(cls)) return IndustrialIcons.EXCEPTION;
        if (cls.hasModifierProperty("abstract")) {
            return bean ? IndustrialIcons.ABSTRACT_CLASS_BEAN : IndustrialIcons.ABSTRACT_CLASS;
        }
        return bean ? IndustrialIcons.CLASS_BEAN : IndustrialIcons.CLASS;
    }

    /** Один проход по аннотациям класса, только короткие имена (например @Service или @o.s.s.Service). */
    private static int annotationKinds(PsiClass cls) {
        PsiModifierList list = cls.getModifierList();
        if (list == null) return 0;
        int kinds = 0;
        for (PsiAnnotation annotation : list.getAnnotations()) {
            PsiJavaCodeReferenceElement ref = annotation.getNameReferenceElement();
            String shortName = ref == null ? null : ref.getReferenceName();
            if (shortName == null) continue;
            if (CONFIG_ANNOTATIONS.contains(shortName)) kinds |= CONFIG;
            else if (BEAN_ANNOTATIONS.contains(shortName)) kinds |= BEAN;
            else if (TEST_ANNOTATIONS.contains(shortName)) kinds |= TEST;
        }
        return kinds;
    }

    private static boolean extendsAny(PsiClass cls, Set<String> names) {
        PsiReferenceList extendsList = cls.getExtendsList();
        if (extendsList == null) return false;
        for (PsiJavaCodeReferenceElement ref : extendsList.getReferenceElements()) {
            if (names.contains(ref.getReferenceName())) return true;
        }
        return false;
    }

    private static boolean extendsException(PsiClass cls) {
        PsiReferenceList extendsList = cls.getExtendsList();
        if (extendsList == null) return false;
        for (PsiJavaCodeReferenceElement ref : extendsList.getReferenceElements()) {
            String n = ref.getReferenceName();
            if (n != null && (isExceptionName(n) || n.equals("Throwable"))) return true;
        }
        return false;
    }

    private static boolean isExceptionName(@Nullable String name) {
        return name != null && (name.endsWith("Exception") || name.endsWith("Error"));
    }

    private static boolean isTestName(@Nullable String name) {
        return name != null && (name.endsWith("Test") || name.endsWith("Tests") || name.endsWith("IT"));
    }
}
