package dev.industrial.graphite;

import com.intellij.ide.FileIconProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Иконки файлов по имени. Для .java иконку даёт класс внутри файла (или тип файла через тему). */
public final class IndustrialFileIconProvider implements FileIconProvider, DumbAware {

    /** application.yml, application-dev.properties, bootstrap-prod.yaml … — конфиги Spring Boot. */
    private static final Pattern SPRING_CONFIG =
            Pattern.compile("(application|bootstrap)(?:-([\\w.-]+))?\\.(ya?ml|properties)");
    /** Flyway: V1__init.sql, V1_2__x.sql, U3__undo.sql, R__views.sql. */
    private static final Pattern SQL_MIGRATION =
            Pattern.compile("([vu]\\d+(_\\d+)*|r)__.+\\.sql");
    private static final Pattern LOG_CONFIG =
            Pattern.compile("(logback|log4j2?)[-\\w]*\\.(xml|ya?ml|properties|json|groovy)");

    @Override
    public @Nullable Icon getIcon(@NotNull VirtualFile file, int flags, @Nullable Project project) {
        if (file.isDirectory()) return null;
        return forName(file.getName());
    }

    /** Иконка конфига Spring по спеку «Industrial Spring Config Icons». */
    private static Icon springConfig(String kind, @Nullable String profile, boolean yaml) {
        if (kind.equals("bootstrap")) {
            return yaml ? IndustrialIcons.SPRING_BOOTSTRAP_YAML : IndustrialIcons.SPRING_BOOTSTRAP_PROPERTIES;
        }
        if (profile == null) {
            return yaml ? IndustrialIcons.SPRING_APP_YAML : IndustrialIcons.SPRING_APP_PROPERTIES;
        }
        switch (profileKind(profile)) {
            case "dev": return yaml ? IndustrialIcons.SPRING_YAML_DEV : IndustrialIcons.SPRING_PROPERTIES_DEV;
            case "test": return yaml ? IndustrialIcons.SPRING_YAML_TEST : IndustrialIcons.SPRING_PROPERTIES_TEST;
            case "prod": return yaml ? IndustrialIcons.SPRING_YAML_PROD : IndustrialIcons.SPRING_PROPERTIES_PROD;
            // профиль без отдельной иконки (local-docker, stage2, …): YAML — значок профиля, properties — базовая спринговая
            default: return yaml ? IndustrialIcons.YAML_PROFILE : IndustrialIcons.SPRING_APP_PROPERTIES;
        }
    }

    private static String profileKind(String profile) {
        switch (profile) {
            case "dev": case "develop": case "development": case "local": return "dev";
            case "test": case "tests": case "testing": case "qa": case "it": case "ci": case "integration": return "test";
            case "prod": case "production": case "live": case "release": return "prod";
            default: return "";
        }
    }

    static @Nullable Icon forName(String fileName) {
        String name = fileName.toLowerCase(Locale.ROOT);
        Matcher spring = SPRING_CONFIG.matcher(name);
        if (spring.matches()) return springConfig(spring.group(1), spring.group(2), !spring.group(3).equals("properties"));
        if (SQL_MIGRATION.matcher(name).matches()) return IndustrialIcons.SQL_MIGRATION;
        if (LOG_CONFIG.matcher(name).matches()) return IndustrialIcons.LOG_CONFIG;
        if (name.equals(".env") || name.startsWith(".env.")) return IndustrialIcons.ENV;
        if (name.startsWith("readme")) return IndustrialIcons.README;

        int dot = name.lastIndexOf('.');
        if (dot < 0) return null;
        switch (name.substring(dot + 1)) {
            case "xml": case "xsd": case "pom": return IndustrialIcons.XML;
            case "yml": case "yaml": return IndustrialIcons.YAML;
            case "sql": case "psql": case "pgsql": return IndustrialIcons.POSTGRESQL;
            case "properties": return IndustrialIcons.PROPERTIES;
            case "json": return IndustrialIcons.JSON;
            case "html": case "htm": return IndustrialIcons.HTML;
            case "css": case "scss": case "sass": case "less": return IndustrialIcons.CSS;
            case "js": case "mjs": case "cjs": return IndustrialIcons.JS;
            case "png": case "jpg": case "jpeg": case "gif": case "svg": case "ico": case "webp": case "bmp":
                return IndustrialIcons.IMAGE;
            case "env": return IndustrialIcons.ENV;
            case "txt": return IndustrialIcons.TEXT;
            case "md": case "markdown": return IndustrialIcons.MD;
            default: return null;
        }
    }
}
