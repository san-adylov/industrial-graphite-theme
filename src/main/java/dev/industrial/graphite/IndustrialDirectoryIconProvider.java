package dev.industrial.graphite;

import com.intellij.ide.IconProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaResourceRootType;

import javax.swing.Icon;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

/**
 * Иконки папок в дереве проекта, как в спеке «Industrial Resources Icons»:
 * excluded → корни (source / test / resources / test resources) → модуль →
 * особые папки ресурсов (static, templates, db/migration, i18n, config) → пакет → обычная папка.
 */
public final class IndustrialDirectoryIconProvider extends IconProvider implements DumbAware {

    /** Если в этой версии IDE нет нужного API — один раз выключаемся и оставляем стандартные иконки. */
    private static volatile boolean disabled;

    private static final class RootTypes {
        static final Set<JavaResourceRootType> RESOURCES = Collections.singleton(JavaResourceRootType.RESOURCE);
        static final Set<JavaResourceRootType> TEST_RESOURCES = Collections.singleton(JavaResourceRootType.TEST_RESOURCE);
    }

    @Override
    public @Nullable Icon getIcon(@NotNull PsiElement element, int flags) {
        if (disabled || !(element instanceof PsiDirectory)) return null;
        PsiDirectory dir = (PsiDirectory) element;
        VirtualFile vf = dir.getVirtualFile();
        Project project = dir.getProject();
        if (vf == null || project == null) return null;

        try {
            ProjectFileIndex index = ProjectRootManager.getInstance(project).getFileIndex();
            return resolve(index, vf, dir.getName());
        } catch (LinkageError e) {
            // NoSuchFieldError / NoClassDefFoundError / NoSuchMethodError — несовместимая версия API
            disabled = true;
            return null;
        }
    }

    static @Nullable Icon resolve(ProjectFileIndex index, VirtualFile vf, String name) {
        if (index.isExcluded(vf)) {
            VirtualFile parent = vf.getParent();
            // Иконка «опасности» — только на корне исключённой области, внутри — обычные папки
            return parent == null || !index.isExcluded(parent) ? IndustrialIcons.EXCLUDE_ROOT : IndustrialIcons.FOLDER;
        }

        VirtualFile sourceRoot = index.getSourceRootForFile(vf);
        // Тип корня спрашиваем только для папок внутри корней исходников
        boolean underResources = sourceRoot != null
                && index.isUnderSourceRootOfType(vf, RootTypes.RESOURCES);
        boolean underTestResources = sourceRoot != null && !underResources
                && index.isUnderSourceRootOfType(vf, RootTypes.TEST_RESOURCES);

        if (vf.equals(sourceRoot)) {
            if (underTestResources) return IndustrialIcons.TEST_RESOURCES_ROOT;
            if (underResources) return IndustrialIcons.RESOURCES_ROOT;
            return index.isInTestSourceContent(vf) ? IndustrialIcons.TEST_ROOT : IndustrialIcons.SOURCE_ROOT;
        }
        if (vf.equals(index.getContentRootForFile(vf))) return IndustrialIcons.MODULE;

        boolean javaPackage = sourceRoot != null && !underResources && !underTestResources;
        if (javaPackage) {
            Icon modulith = modulithIcon(vf, index.getContentRootForFile(vf));
            return modulith != null ? modulith : IndustrialIcons.PACKAGE;
        }

        Icon special = special(name, vf.getParent());
        return special != null ? special : IndustrialIcons.FOLDER;
    }

    private static final ModulithDetector MODULITH = new ModulithDetector();

    private static @Nullable Icon modulithIcon(VirtualFile dir, @Nullable VirtualFile contentRoot) {
        ModulithDetector.Role role = MODULITH.role(new VfsNode(dir), contentRoot == null ? null : new VfsNode(contentRoot));
        if (role == null) return null;
        switch (role) {
            case MODULE: return IndustrialIcons.MODULITH_MODULE;
            case OPEN_MODULE: return IndustrialIcons.MODULITH_OPEN_MODULE;
            case SHARED_MODULE: return IndustrialIcons.MODULITH_SHARED_MODULE;
            case EXPOSED: return IndustrialIcons.MODULITH_EXPOSED;
            case INTERNAL: return IndustrialIcons.MODULITH_INTERNAL;
            case NAMED_INTERFACE: return IndustrialIcons.MODULITH_NAMED_INTERFACE;
            default: return null;
        }
    }

    /** Обёртка VirtualFile для {@link ModulithDetector}. */
    private static final class VfsNode implements ModulithDetector.Node {
        private static final int MAX_TEXT = 256 * 1024;
        private final VirtualFile file;
        VfsNode(VirtualFile file) { this.file = file; }
        @Override public String name() { return file.getName(); }
        @Override public @Nullable ModulithDetector.Node parent() { VirtualFile p = file.getParent(); return p == null ? null : new VfsNode(p); }
        @Override public @Nullable ModulithDetector.Node child(String name) { VirtualFile c = file.findChild(name); return c == null ? null : new VfsNode(c); }
        @Override public java.util.List<VfsNode> children() {
            VirtualFile[] kids = file.getChildren();
            java.util.List<VfsNode> out = new java.util.ArrayList<>(kids == null ? 0 : kids.length);
            if (kids != null) for (VirtualFile k : kids) out.add(new VfsNode(k));
            return out;
        }
        @Override public boolean isDirectory() { return file.isDirectory(); }
        @Override public @Nullable String text() {
            if (file.isDirectory() || file.getLength() > MAX_TEXT) return null;
            try { return new String(file.contentsToByteArray(), java.nio.charset.StandardCharsets.UTF_8); }
            catch (java.io.IOException e) { return null; }
        }
        @Override public String key() { return file.getPath(); }
    }

    private static @Nullable Icon special(String dirName, @Nullable VirtualFile parent) {
        String name = dirName.toLowerCase(Locale.ROOT);
        switch (name) {
            case "static": case "public": case "assets": return IndustrialIcons.FOLDER_STATIC;
            case "templates": case "views": return IndustrialIcons.FOLDER_TEMPLATES;
            case "migration": case "migrations": case "changelog": case "changelogs": case "flyway": case "liquibase":
                return IndustrialIcons.FOLDER_MIGRATION;
            case "i18n": case "messages": case "locales": case "locale": case "lang": case "l10n":
                return IndustrialIcons.FOLDER_I18N;
            case "config": case "configs": case "configuration": return IndustrialIcons.FOLDER_CONFIG;
            default: return null;
        }
    }
}
