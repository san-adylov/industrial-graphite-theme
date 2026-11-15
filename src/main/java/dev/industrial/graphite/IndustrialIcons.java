package dev.industrial.graphite;

import com.intellij.openapi.util.IconLoader;

import javax.swing.Icon;

/** Иконки Industrial: один векторный SVG 16x16 на иконку, IDE сама масштабирует под HiDPI. */
public final class IndustrialIcons {
    private IndustrialIcons() {}

    private static Icon load(String name) {
        return IconLoader.getIcon("/icons/industrial/" + name + ".svg", IndustrialIcons.class);
    }

    public static final Icon CLASS = load("class");
    public static final Icon ABSTRACT_CLASS = load("abstractClass");
    public static final Icon INTERFACE = load("interface");
    public static final Icon ENUM = load("enum");
    public static final Icon RECORD = load("record");
    public static final Icon ANNOTATION = load("annotation");
    public static final Icon EXCEPTION = load("exception");
    public static final Icon TEST_CLASS = load("testClass");

    public static final Icon SPRING = load("spring");
    public static final Icon CLASS_BEAN = load("class_bean");
    public static final Icon ABSTRACT_CLASS_BEAN = load("abstractClass_bean");
    public static final Icon INTERFACE_BEAN = load("interface_bean");
    public static final Icon RECORD_BEAN = load("record_bean");

    public static final Icon JAVA = load("java");
    public static final Icon XML = load("xml");
    public static final Icon YAML = load("yaml");
    public static final Icon POSTGRESQL = load("postgresql");

    // --- Ресурсы: файлы ---
    public static final Icon PROPERTIES = load("properties");
    public static final Icon YAML_PROFILE = load("yamlProfile");
    public static final Icon JSON = load("json");
    public static final Icon HTML = load("html");
    public static final Icon CSS = load("css");
    public static final Icon JS = load("js");
    public static final Icon IMAGE = load("image");
    public static final Icon ENV = load("env");
    public static final Icon SQL_MIGRATION = load("sqlMigration");
    public static final Icon LOG_CONFIG = load("logConfig");
    public static final Icon TEXT = load("text");

    // --- Ресурсы: папки и корни ---
    public static final Icon FOLDER = load("folder");
    public static final Icon PACKAGE = load("package");
    public static final Icon MODULE = load("module");
    public static final Icon SOURCE_ROOT = load("sourceRoot");
    public static final Icon TEST_ROOT = load("testRoot");
    public static final Icon RESOURCES_ROOT = load("resourcesRoot");
    public static final Icon TEST_RESOURCES_ROOT = load("testResourcesRoot");
    public static final Icon EXCLUDE_ROOT = load("excludeRoot");
    public static final Icon GENERATED_ROOT = load("generatedRoot");
    public static final Icon FOLDER_STATIC = load("folderStatic");
    public static final Icon FOLDER_TEMPLATES = load("folderTemplates");
    public static final Icon FOLDER_MIGRATION = load("folderMigration");
    public static final Icon FOLDER_I18N = load("folderI18n");
    public static final Icon FOLDER_CONFIG = load("folderConfig");

    // --- Системные ---
    public static final Icon MD = loadSys("md");
    public static final Icon README = loadSys("readme");
    /** Панели из закрытых плагинов Ultimate — ставим через API; рядом лежат версии @20x20. */
    public static final Icon TOOL_WINDOW_DATABASE = loadSys("toolWindowDatabase");
    public static final Icon TOOL_WINDOW_ENDPOINTS = loadSys("toolWindowEndpoints");

    // --- Конфиги Spring Boot ---
    public static final Icon SPRING_APP_YAML = loadSpring("springApplicationYaml");
    public static final Icon SPRING_APP_PROPERTIES = loadSpring("springApplicationProperties");
    public static final Icon SPRING_BOOTSTRAP_YAML = loadSpring("springBootstrapYaml");
    public static final Icon SPRING_BOOTSTRAP_PROPERTIES = loadSpring("springBootstrapProperties");
    public static final Icon SPRING_YAML_DEV = loadSpring("springProfileYaml_dev");
    public static final Icon SPRING_YAML_TEST = loadSpring("springProfileYaml_test");
    public static final Icon SPRING_YAML_PROD = loadSpring("springProfileYaml_prod");
    public static final Icon SPRING_PROPERTIES_DEV = loadSpring("springProfileProperties_dev");
    public static final Icon SPRING_PROPERTIES_TEST = loadSpring("springProfileProperties_test");
    public static final Icon SPRING_PROPERTIES_PROD = loadSpring("springProfileProperties_prod");

    // --- Spring Modulith ---
    public static final Icon MODULITH_MODULE = loadModulith("modulithModule");
    public static final Icon MODULITH_OPEN_MODULE = loadModulith("modulithOpenModule");
    public static final Icon MODULITH_SHARED_MODULE = loadModulith("modulithSharedModule");
    public static final Icon MODULITH_EXPOSED = loadModulith("modulithExposedPackage");
    public static final Icon MODULITH_INTERNAL = loadModulith("modulithInternalPackage");
    public static final Icon MODULITH_NAMED_INTERFACE = loadModulith("modulithNamedInterface");

    private static Icon loadModulith(String name) {
        return IconLoader.getIcon("/icons/industrial/modulith/" + name + ".svg", IndustrialIcons.class);
    }

    private static Icon loadSpring(String name) {
        return IconLoader.getIcon("/icons/industrial/spring/" + name + ".svg", IndustrialIcons.class);
    }

    private static Icon loadSys(String name) {
        return IconLoader.getIcon("/icons/industrial/sys/" + name + ".svg", IndustrialIcons.class);
    }
}
