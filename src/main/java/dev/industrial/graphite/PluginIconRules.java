package dev.industrial.graphite;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Правила подмены иконок закрытых плагинов IntelliJ IDEA Ultimate (Spring, JPA, Database).
 * Их пути не опубликованы, поэтому сопоставляем по плагину, размеру исходной иконки и ключевым словам в имени файла.
 * Чистая логика без зависимостей от IDE — покрыта тестом.
 */
final class PluginIconRules {
    private PluginIconRules() {}

    enum Group { MODULITH, SPRING, JPA, DATABASE }

    static @Nullable Group group(String pluginId, String path) {
        String id = pluginId.toLowerCase(Locale.ROOT);
        if (id.contains("modulith") || (id.contains("spring") && path.toLowerCase(Locale.ROOT).contains("modulith"))) {
            return Group.MODULITH;
        }
        if (id.contains("spring")) return Group.SPRING;
        if (id.contains("jpa") || id.contains("persistence") || id.contains("javaee") || id.contains("jakarta")
                || id.contains("hibernate")) return Group.JPA;
        if (id.contains("database") || id.equals("com.intellij.datagrip")) return Group.DATABASE;
        return null;
    }

    /** Имя файла без каталога, расширения и суффиксов вариантов. */
    static String baseName(String path) {
        String name = path.substring(path.lastIndexOf('/') + 1).toLowerCase(Locale.ROOT);
        if (name.endsWith(".svg")) name = name.substring(0, name.length() - 4);
        else if (name.endsWith(".png")) name = name.substring(0, name.length() - 4);
        for (String suffix : new String[]{"@2x", "_dark", "@14x14", "@12x12", "@20x20"}) {
            name = name.replace(suffix, "");
        }
        return name;
    }

    /**
     * @param size ширина исходной иконки (12–14 — gutter, 16 — узлы дерева); иконки других размеров не трогаем
     * @return путь к нашей иконке или null
     */
    static @Nullable String target(Group group, String path, int size) {
        String n = baseName(path);
        boolean gutter = size >= 12 && size <= 14;
        String g14 = size == 14 ? "_14" : "";
        if (gutter) {
            String id = gutterId(group, n);
            if (id == null) return null;
            if (id.startsWith("modulith:")) return "/icons/industrial/modulith/" + id.substring(9) + g14 + ".svg";
            return "/icons/industrial/gutter/" + id + g14 + ".svg";
        }
        if (group == Group.MODULITH && (size == 16 || size == 20)) {
            String id = modulithNodeId(n);
            if (id == null) return null;
            // иконки панели/действий: у нас база 16 px + вариант @20x20 рядом
            return "/icons/industrial/modulith/" + id + (size == 20 && TOOL_LIKE.contains(id) ? "@20x20" : "") + ".svg";
        }
        if (size == 16 && group == Group.DATABASE) {
            String id = databaseNodeId(n);
            return id == null ? null : "/icons/industrial/sys/" + id + ".svg";
        }
        return null;
    }

    private static boolean has(String n, String... words) {
        for (String w : words) if (n.contains(w)) return true;
        return false;
    }

    private static final java.util.Set<String> TOOL_LIKE =
            java.util.Set.of("toolwindowModulith", "modulithDependency", "modulithVerify", "modulithDocs");

    private static @Nullable String modulithNodeId(String n) {
        if (has(n, "toolwindow") || n.equals("modulith") || has(n, "applicationmodules")) return "toolwindowModulith";
        if (has(n, "dependenc")) return "modulithDependency";
        if (has(n, "verif")) return "modulithVerify";
        if (has(n, "doc")) return "modulithDocs";
        if (has(n, "namedinterface")) return "modulithNamedInterface";
        if (has(n, "open")) return "modulithOpenModule";
        if (has(n, "shared")) return "modulithSharedModule";
        if (has(n, "internal")) return "modulithInternalPackage";
        if (has(n, "exposed", "api")) return "modulithExposedPackage";
        if (has(n, "module")) return "modulithModule";
        return null;
    }

    private static @Nullable String gutterId(Group group, String n) {
        switch (group) {
            case MODULITH:
                if (has(n, "violation", "boundary", "illegal")) return "modulith:gutterModuleViolation";
                if (has(n, "publication", "publish")) return "modulith:gutterEventPublication";
                if (has(n, "moduletest", "test", "scenario")) return "modulith:gutterModuleTest";
                if (has(n, "listener", "listen")) return "modulith:gutterModuleListener";
                return gutterId(Group.SPRING, n);
            case SPRING:
                if (has(n, "candidate")) return "gutterBeanCandidates";
                if (has(n, "autowir", "inject")) return "gutterAutowired";
                if (has(n, "publish")) return "gutterEventPublisher";
                if (has(n, "listen")) return "gutterEventListener";
                if (has(n, "schedul", "cron")) return "gutterScheduled";
                if (has(n, "transact")) return "gutterTransactional";
                if (has(n, "configurationprop", "configprop", "properties")) return "gutterConfigProperties";
                if (has(n, "repositor")) return "gutterRepository";
                if (has(n, "mapping", "endpoint", "request", "url", "web", "http")) return "gutterEndpoint";
                if (has(n, "bean", "spring", "component", "service", "controller", "factory", "config")) return "gutterSpringBean";
                return null;
            case JPA:
                if (has(n, "entity", "embeddable", "mappedsuperclass")) return "gutterEntity";
                if (has(n, "relation", "tomany", "toone", "association", "join")) return "gutterRelationship";
                if (n.equals("id") || has(n, "primarykey", "identifier", "idattribute")) return "gutterId";
                if (has(n, "column", "attribute", "field", "basic")) return "gutterColumn";
                if (has(n, "repositor")) return "gutterRepository";
                return null;
            case DATABASE:
                if (has(n, "run", "execute", "console")) return "gutterRunSql";
                return null;
            default:
                return null;
        }
    }

    private static @Nullable String databaseNodeId(String n) {
        if (has(n, "datasource")) return "dataSource";
        if (has(n, "primarykey") || n.equals("pk")) return "primaryKey";
        if (has(n, "foreignkey") || n.equals("fk")) return "foreignKey";
        if (has(n, "disconnect")) return "disconnect";
        if (has(n, "connect")) return "connect";
        if (has(n, "execute", "runquery", "run")) return "executeQuery";
        if (has(n, "sequence")) return "sequence";
        if (has(n, "trigger")) return "trigger";
        if (has(n, "index")) return "index";
        if (has(n, "view")) return "view";
        if (has(n, "function", "routine", "procedure")) return "function";
        if (has(n, "column")) return "column";
        if (has(n, "schema")) return "schema";
        if (has(n, "table")) return "table";
        return null;
    }
}
