package dev.industrial.graphite;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
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
    private static final Logger LOG = Logger.getInstance(IndustrialToolWindowListener.class);

    private final Project project;

    public IndustrialToolWindowListener(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public void toolWindowsRegistered(@NotNull List<String> ids, @NotNull ToolWindowManager manager) {
        if (ids.stream().noneMatch(id -> iconFor(id) != null)) return;
        // Не manager.invokeLater: он выполняет код внутри корутины, которая строит окно IDE,
        // и любое исключение там обрывает инициализацию UI (пропадают виджеты тулбара).
        // Обычный invokeLater срабатывает на EDT уже после того, как окно собрано.
        ApplicationManager.getApplication().invokeLater(
                () -> applyIcons(ids, manager), ModalityState.nonModal(), project.getDisposed());
    }

    private static void applyIcons(List<String> ids, ToolWindowManager manager) {
        for (String id : ids) {
            Icon icon = iconFor(id);
            if (icon == null) continue;
            try {
                ToolWindow window = manager.getToolWindow(id);
                if (window == null || window.isDisposed()) continue;
                // Сначала грузим иконку сами: если она не рендерится (размер 0x0),
                // не трогаем кнопку панели и оставляем стандартную иконку.
                if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) continue;
                window.setIcon(icon);
            } catch (RuntimeException | LinkageError e) {
                // Иконка — косметика: сбой здесь не должен ломать IDE
                LOG.info("Industrial Graphite: cannot set icon for tool window " + id + ": " + e);
            }
        }
    }

    private static Icon iconFor(String id) {
        switch (id) {
            case "Database": return IndustrialIcons.TOOL_WINDOW_DATABASE;
            case "Endpoints": return IndustrialIcons.TOOL_WINDOW_ENDPOINTS;
            default: return null;
        }
    }
}
