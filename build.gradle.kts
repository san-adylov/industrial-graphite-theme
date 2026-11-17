plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "dev.industrial"
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.3.5")
        bundledPlugin("com.intellij.java")
    }
}

java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }

// Исходники в UTF-8 (русские комментарии); без этого javac на Windows читает их как windows-1251
tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8" }

intellijPlatform {
    pluginConfiguration {
        version = project.version.toString()
        ideaVersion {
            sinceBuild = "233"
            untilBuild = provider { null }
        }
        changeNotes = """
            <p><b>1.5.3</b></p>
            <ul>
              <li>Fixed: Database / Endpoints tool window icons are applied after the IDE frame is fully built;
                  a failing icon can no longer interrupt UI initialization (fixes missing toolbar widgets and
                  "Width (0) and height (0)" on IntelliJ IDEA 2025.3).</li>
            </ul>
            <p><b>1.5.2</b></p>
            <ul>
              <li>Fixed: tool window icons (Database, Endpoints) are now set on the EDT — no more
                  "must be called on EDT" / "Width (0) and height (0)" errors on startup.</li>
              <li>Fixed: bundled font is applied on the EDT — no more "UISettings should only be changed on the EDT".</li>
              <li>Fixed: main toolbar widgets (e.g. Ultimate trial widget) are no longer lost on first start.</li>
            </ul>
        """.trimIndent()
    }
    pluginVerification {
        // recommended() в 2.5.0 ищет несуществующий ideaIC-2025.3 (с 2025.3 IDEA — единый дистрибутив)
        ides {
            ide(org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.IntellijIdeaCommunity, "2023.3.8")
            ide(org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.IntellijIdeaCommunity, "2024.3.5")
            ide(org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.IntellijIdeaUltimate, "2025.3")
        }
    }
    publishing {
        token = providers.gradleProperty("intellijPlatformPublishingToken")
    }
    buildSearchableOptions = false
}
