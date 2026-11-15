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

intellijPlatform {
    pluginConfiguration {
        version = project.version.toString()
        ideaVersion {
            sinceBuild = "233"
            untilBuild = provider { null }
        }
    }
    publishing {
        token = providers.gradleProperty("intellijPlatformPublishingToken")
    }
    buildSearchableOptions = false
}
