package dev.industrial.graphite;

import com.intellij.ide.plugins.cl.PluginAwareClassLoader;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.IconPathPatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Подменяет иконки gutter и узлов БД у плагинов Spring / JPA / Database (см. {@link PluginIconRules}). */
final class IndustrialIconPatcher extends IconPathPatcher {
    private static final Logger LOG = Logger.getInstance(IndustrialIconPatcher.class);
    private static final Pattern WIDTH = Pattern.compile("<svg[^>]*?\\swidth=\"(\\d+)");
    private static final String NONE = "";

    /** ключ: pluginId + путь → наш путь или NONE */
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    /** pluginId|путь, которые мы подменили, — чтобы отдать загрузчик ресурсов нашего плагина */
    private final Set<String> patched = ConcurrentHashMap.newKeySet();

    @Override
    public @Nullable String patchPath(@NotNull String path, @Nullable ClassLoader classLoader) {
        if (!(classLoader instanceof PluginAwareClassLoader) || !path.endsWith(".svg")) return null;
        String pluginId = ((PluginAwareClassLoader) classLoader).getPluginId().getIdString();
        PluginIconRules.Group group = PluginIconRules.group(pluginId, path);
        if (group == null) return null;

        String key = pluginId + '|' + path;
        String result = cache.computeIfAbsent(key, k -> {
            int size = svgWidth(classLoader, path);
            String target = PluginIconRules.target(group, path, size);
            if (target == null && size >= 12 && size <= 14) {
                LOG.info("Industrial Graphite: gutter icon without mapping: " + pluginId + " " + path);
            }
            return target == null ? NONE : target;
        });
        if (result.isEmpty()) return null;
        patched.add(key);
        return result;
    }

    @Override
    public @Nullable ClassLoader getContextClassLoader(@NotNull String path, @Nullable ClassLoader originalClassLoader) {
        if (!(originalClassLoader instanceof PluginAwareClassLoader)) return null;
        String pluginId = ((PluginAwareClassLoader) originalClassLoader).getPluginId().getIdString();
        return patched.contains(pluginId + '|' + path) ? IndustrialIconPatcher.class.getClassLoader() : null;
    }

    private static int svgWidth(ClassLoader cl, String path) {
        String resource = path.startsWith("/") ? path.substring(1) : path;
        try (InputStream in = cl.getResourceAsStream(resource)) {
            if (in == null) return -1;
            byte[] head = in.readNBytes(4096);
            Matcher m = WIDTH.matcher(new String(head, StandardCharsets.UTF_8));
            return m.find() ? Integer.parseInt(m.group(1)) : -1;
        } catch (Exception e) {
            return -1;
        }
    }
}
