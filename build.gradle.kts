plugins {
    id("java")
    id("com.gradleup.shadow") version "9.1.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven("https://jitpack.io")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.fancyinnovations.com/releases")
}

dependencies {
    compileOnly("de.oliver:FancyHolograms:2.9.1")
    compileOnly("io.github.miniplaceholders:miniplaceholders-api:3.0.1")
    implementation(platform("com.intellectualsites.bom:bom-newest:1.55")) // Ref: https://github.com/IntellectualSites/bom
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") { isTransitive = false }
    implementation("com.github.SlimifiedxD:quartz:adeedd89e8")
    annotationProcessor("com.github.SlimifiedxD:quartz:66fb749254")
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21.11")
    }
}

tasks.test {
    useJUnitPlatform()
}