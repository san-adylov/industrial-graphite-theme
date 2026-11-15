package dev.industrial.graphite;

import com.intellij.ide.FileIconPatcher;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * Последнее слово для иконок файлов.
 * В дереве проекта IntelliJ сначала спрашивает IconProvider'ы (там плагин Spring Boot из Ultimate ставит свою
 * иконку для application.yml/.yaml/.properties), и только если они промолчали — FileIconProvider'ы.
 * Патчеры же применяются после всех провайдеров — и в дереве, и во вкладках, — поэтому подменяем здесь.
 * Трогаем только файлы, которые знает {@link IndustrialFileIconProvider}; остальные иконки не меняем.
 */
public final class IndustrialFileIconPatcher implements FileIconPatcher {
    @Override
    public @NotNull Icon patchIcon(@NotNull Icon icon, @NotNull VirtualFile file, int flags, @Nullable Project project) {
        if (file.isDirectory()) return icon;
        Icon ours = IndustrialFileIconProvider.forName(file.getName());
        return ours != null ? ours : icon;
    }
}
