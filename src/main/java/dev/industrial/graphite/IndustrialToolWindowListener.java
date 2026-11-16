package dev.industrial.graphite;

import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.util.List;

/**
 * Иконки панелей Database и Endpoints: они из закрытых плагинов IntelliJ IDEA Ultimate,
 * путей их иконок нет в открытых исходниках, поэтому ставим иконку напрямую по ID панели.
 * Остальные панели подменяются темой по точным путям.
 */
public final class IndustrialToolWindowListener implements ToolWindowManagerListener {
    @Override
    public void toolWindowsRegistered(@NotNull List<String> ids, @NotNull ToolWindowManager manager) {
        if (ids.stream().noneMatch(id -> iconFor(id) != null)) return;
        // Событие приходит из фоновой корутины; setIcon можно вызывать только на EDT,
        // и только когда кнопки панелей уже созданы, иначе иконка масштабируется в 0x0.
        manager.invokeLater(() -> {
            try {
                for (String id : ids) {
                    Icon icon = iconFor(id);
                    if (icon == null) continue;
                    ToolWindow window = manager.getToolWindow(id);
                    if (window != null && !window.isDisposed()) window.setIcon(icon);
                }
            } catch (LinkageError ignored) {
                // несовместимая версия API — оставляем стандартные иконки
            }
        });
    }

    private static Icon iconFor(String id) {
        switch (id) {
            case "Database": return IndustrialIcons.TOOL_WINDOW_DATABASE;
            case "Endpoints": return IndustrialIcons.TOOL_WINDOW_ENDPOINTS;
            default: return null;
        }
    }
}
