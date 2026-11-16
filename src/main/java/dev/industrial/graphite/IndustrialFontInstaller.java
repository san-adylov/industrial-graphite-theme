package dev.industrial.graphite;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.UISettings;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.colors.FontPreferences;
import com.intellij.openapi.editor.colors.ModifiableFontPreferences;
import com.intellij.openapi.editor.colors.impl.AppEditorFontOptions;
import org.jetbrains.annotations.NotNull;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.util.List;

/**
 * Подключает IBM Plex Mono (лежит в плагине, лицензия OFL) и один раз ставит его
 * шрифтом интерфейса и редактора. Дальше настройки — твои: если поменяешь шрифт в Settings,
 * плагин не будет его перетирать.
 */
public final class IndustrialFontInstaller implements AppLifecycleListener {
    public static final String FAMILY = "IBM Plex Mono";
    private static final String APPLIED_KEY = "dev.industrial.graphite.font.applied.v1";
    private static final String[] FILES = {
            "IBMPlexMono-Regular.ttf", "IBMPlexMono-Italic.ttf", "IBMPlexMono-Bold.ttf", "IBMPlexMono-BoldItalic.ttf",
    };

    @Override
    public void appFrameCreated(@NotNull List<String> commandLineArgs) {
        registerFonts();
        PropertiesComponent props = PropertiesComponent.getInstance();
        if (props.getBoolean(APPLIED_KEY)) return;
        // appFrameCreated вызывается из фоновой корутины, а UISettings/LafManager — только на EDT.
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                if (props.getBoolean(APPLIED_KEY)) return;
                applyUiFont();
                applyEditorFont();
                props.setValue(APPLIED_KEY, true);
            } catch (LinkageError ignored) {
                // несовместимая версия API — шрифт можно выбрать вручную в Settings
            }
        }, ModalityState.nonModal());
    }

    /** Регистрируем шрифт в Java при каждом запуске, чтобы он был доступен, даже если не установлен в системе. */
    static void registerFonts() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String file : FILES) {
            try (InputStream in = IndustrialFontInstaller.class.getResourceAsStream("/fonts/" + file)) {
                if (in != null) ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, in));
            } catch (Exception ignored) {
                // повреждённый файл или headless-окружение — просто пропускаем
            }
        }
    }

    private static void applyUiFont() {
        UISettings ui = UISettings.getInstance();
        ui.setOverrideLafFonts(true);
        ui.setFontFace(FAMILY);
        ui.fireUISettingsChanged();
        LafManager.getInstance().updateUI();
    }

    private static void applyEditorFont() {
        FontPreferences prefs = AppEditorFontOptions.getInstance().getFontPreferences();
        if (!(prefs instanceof ModifiableFontPreferences)) return;
        ModifiableFontPreferences m = (ModifiableFontPreferences) prefs;
        int size = prefs.getSize(prefs.getFontFamily());   // размер оставляем твой
        m.clearFonts();
        m.register(FAMILY, size);
    }
}
