package dev.industrial.graphite;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** При старте IDE подключает подменщик иконок закрытых плагинов (Spring, JPA, Database). */
public final class IndustrialStartup implements AppLifecycleListener {
    private static volatile boolean installed;

    @Override
    public void appFrameCreated(@NotNull List<String> commandLineArgs) {
        if (installed) return;
        try {
            IconLoader.installPathPatcher(new IndustrialIconPatcher());
            installed = true;
        } catch (LinkageError ignored) {
            // несовместимая версия API — остаются стандартные иконки этих плагинов
        }
    }
}
