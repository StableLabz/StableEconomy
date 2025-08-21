import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml.Load

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.2"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

setGroup("org.stablerpg.stableeconomy")
setVersion("1.0.0")

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.triumphteam.dev/snapshots")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")

    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("dev.jorel:commandapi-bukkit-shade-mojang-mapped:10.1.2")
    implementation("dev.triumphteam:triumph-gui-paper:4.0.0-SNAPSHOT")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("me.clip:placeholderapi:2.11.6")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    compileOnly("com.zaxxer:HikariCP:7.0.1")
    compileOnly("org.xerial:sqlite-jdbc:3.50.3.0")
    compileOnly("com.h2database:h2:2.3.232")
    compileOnly("org.mariadb.jdbc:mariadb-java-client:3.5.5")
    compileOnly("org.postgresql:postgresql:42.7.7")
    compileOnly("org.mongodb:mongodb-driver-sync:5.5.1")
}

tasks {
    val javaLanguageVersion = JavaLanguageVersion.of(21)
    val javaVersion = JavaVersion.VERSION_21
    java {
        toolchain {
            languageVersion.set(javaLanguageVersion)
        }
        targetCompatibility = javaVersion
        sourceCompatibility = javaVersion

        withJavadocJar()
        withSourcesJar()
    }
    compileJava {
        targetCompatibility = javaLanguageVersion.toString()
        sourceCompatibility = javaLanguageVersion.toString()
        options.release.set(javaLanguageVersion.asInt())

        options.encoding = Charsets.UTF_8.name()
        options.compilerArgs.plusAssign("-Xlint:deprecation")
    }
    jar {
        enabled = false
    }
    shadowJar {
        archiveFileName.set("${project.name}-${project.version}.jar")

        relocate("org.bstats", "${project.group}.libs.bstats")
        relocate("dev.jorel.commandapi", "${project.group}.libs.commandapi")
        relocate("dev.triumphteam.gui", "${project.group}.libs.triumphgui")

        minimize()
    }
    assemble {
        dependsOn(shadowJar)
    }
    paperweight {
        reobfArtifactConfiguration.set(ReobfArtifactConfiguration.MOJANG_PRODUCTION)
    }
    paperPluginYaml {
        apiVersion.set("1.21")
        name.set(rootProject.name)
        version.set(project.version.toString())
        main.set("${project.group}.${rootProject.name}")
        loader.set("${project.group}.${rootProject.name}Loader")
        description.set("An addition to the StableRPG plugin suite.")
        author.set("ImNotStable")
        website.set("https://github.com/StableLabz/StableEconomy")
        dependencies {
            server("Vault", Load.BEFORE, required = false, joinClasspath = true)
            server("PlaceholderAPI", Load.BEFORE, required = false, joinClasspath = true)
        }
    }
    runServer {
        minecraftVersion("1.21.8")
        jvmArgs("-Xms2G", "-Xmx2G")
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
        (options as CoreJavadocOptions).addBooleanOption("Xdoclint:none", true)
    }
}
