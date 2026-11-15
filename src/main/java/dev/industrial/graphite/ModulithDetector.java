package dev.industrial.graphite;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Определяет роль Java-пакета в Spring Modulith по тексту файлов — без индексов и PSI,
 * поэтому работает и во время индексации и не грузит AST в дереве проекта.
 *
 * <ul>
 *   <li>корневой пакет — тот, где лежит класс с {@code @SpringBootApplication} или {@code @Modulithic};</li>
 *   <li>модуль — прямой подпакет корневого; {@code sharedModules} → shared, {@code @ApplicationModule(type = OPEN)} → open;</li>
 *   <li>подпакет модуля: {@code @NamedInterface} → named interface; api / access / spi / events → exposed; остальные → internal.</li>
 * </ul>
 * Включается, только если в проекте есть Spring Modulith (зависимость в pom.xml / build.gradle или {@code @Modulithic}).
 */
final class ModulithDetector {

    /** Минимальный доступ к файловой системе — в IDE это VirtualFile, в тесте — подделка. */
    interface Node {
        String name();
        @Nullable Node parent();
        @Nullable Node child(String name);
        List<? extends Node> children();
        boolean isDirectory();
        /** Текст файла или null, если файл большой/нечитаемый. */
        @Nullable String text();
        String key();
    }

    enum Role { MODULE, OPEN_MODULE, SHARED_MODULE, EXPOSED, INTERNAL, NAMED_INTERFACE }

    private static final long TTL_MS = 5_000;
    private static final Pattern SHARED = Pattern.compile("sharedModules\\s*=\\s*(\\{[^}]*}|\"[^\"]*\")");
    /** @NamedInterface / @org.springframework.modulith.NamedInterface */
    private static final Pattern NAMED_INTERFACE = Pattern.compile("@(?:[\\w.]+\\.)?NamedInterface\\b");
    private static final Pattern APPLICATION_MODULE = Pattern.compile("@(?:[\\w.]+\\.)?ApplicationModule\\b");
    private static final Pattern MODULITHIC = Pattern.compile("@(?:[\\w.]+\\.)?Modulithic\\b");
    private static final Pattern BOOT_APP = Pattern.compile("@(?:[\\w.]+\\.)?SpringBootApplication\\b");
    private static final Pattern QUOTED = Pattern.compile("\"([^\"]+)\"");
    private static final String[] BUILD_FILES = {"pom.xml", "build.gradle", "build.gradle.kts", "libs.versions.toml"};

    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    private static final class Cached {
        final long time; final Object value;
        Cached(Object value) { this.time = System.currentTimeMillis(); this.value = value; }
    }

    @SuppressWarnings("unchecked")
    private <T> T cached(String key, java.util.function.Supplier<T> compute) {
        Cached c = cache.get(key);
        if (c != null && System.currentTimeMillis() - c.time < TTL_MS) return (T) c.value;
        T v = compute.get();
        cache.put(key, new Cached(v));
        return v;
    }

    /** Роль пакета или null, если это обычный пакет. */
    @Nullable Role role(Node dir, @Nullable Node contentRoot) {
        Node parent = dir.parent();
        if (parent == null) return null;

        // модуль: родитель — корневой пакет приложения
        RootInfo root = rootInfo(parent);
        if (root != null) {
            if (!modulithEnabled(contentRoot, root)) return null;
            String name = dir.name();
            if (root.shared.contains(name)) return Role.SHARED_MODULE;
            String pkgInfo = packageInfo(dir);
            if (pkgInfo != null && APPLICATION_MODULE.matcher(pkgInfo).find() && pkgInfo.contains("OPEN")) return Role.OPEN_MODULE;
            return Role.MODULE;
        }

        // подпакет модуля: дед — корневой пакет
        Node grand = parent.parent();
        if (grand == null) return null;
        RootInfo grandRoot = rootInfo(grand);
        if (grandRoot == null || !modulithEnabled(contentRoot, grandRoot)) return null;
        String pkgInfo = packageInfo(dir);
        if (pkgInfo != null && NAMED_INTERFACE.matcher(pkgInfo).find()) return Role.NAMED_INTERFACE;
        switch (dir.name().toLowerCase(Locale.ROOT)) {
            case "api": case "access": case "spi": case "events": case "event": case "exposed": case "web":
                return Role.EXPOSED;
            default:
                return Role.INTERNAL;
        }
    }

    private static final class RootInfo {
        final boolean modulithic; final java.util.Set<String> shared;
        RootInfo(boolean modulithic, java.util.Set<String> shared) { this.modulithic = modulithic; this.shared = shared; }
    }

    private static final RootInfo NOT_ROOT = new RootInfo(false, java.util.Set.of());

    private @Nullable RootInfo rootInfo(Node dir) {
        RootInfo info = cached("root:" + dir.key(), () -> {
            for (Node f : dir.children()) {
                if (f.isDirectory() || !f.name().endsWith(".java")) continue;
                String text = f.text();
                if (text == null) continue;
                boolean modulithic = MODULITHIC.matcher(text).find();
                if (!modulithic && !BOOT_APP.matcher(text).find()) continue;
                java.util.Set<String> shared = new java.util.HashSet<>();
                Matcher m = SHARED.matcher(text);
                if (m.find()) {
                    Matcher q = QUOTED.matcher(m.group(1));
                    while (q.find()) shared.add(q.group(1));
                }
                return new RootInfo(modulithic, shared);
            }
            return NOT_ROOT;
        });
        return info == NOT_ROOT ? null : info;
    }

    private boolean modulithEnabled(@Nullable Node contentRoot, RootInfo root) {
        if (root.modulithic) return true;
        if (contentRoot == null) return false;
        return cached("modulith:" + contentRoot.key(), () -> {
            for (Node base = contentRoot; base != null; base = base.parent()) {
                for (String b : BUILD_FILES) {
                    Node f = base.child(b);
                    String text = f == null ? null : f.text();
                    if (text != null && text.contains("spring-modulith")) return true;
                }
                if (base.child("settings.gradle") != null || base.child("settings.gradle.kts") != null
                        || base.child(".git") != null) break;   // дошли до корня проекта
            }
            return false;
        });
    }

    private @Nullable String packageInfo(Node dir) {
        String v = cached("pi:" + dir.key(), () -> {
            Node f = dir.child("package-info.java");
            String t = f == null ? null : f.text();
            return t == null ? "" : t;
        });
        return v.isEmpty() ? null : v;
    }
}
