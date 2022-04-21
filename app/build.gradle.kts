/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.3/userguide/building_java_projects.html
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.slf4j:slf4j-nop:2.0.0-alpha7")
    implementation("com.oracle.database.jdbc:ojdbc11:21.5.0.0")
    implementation("mysql:mysql-connector-java:8.0.28")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("commons-codec:commons-codec:1.15")
}

application {
    // Define the main class for the application.
    mainClass.set("com.picromedia.App")
}

sourceSets {
    main {
        resources {
            setSrcDirs(listOf("src/main/website", "src/main/resources"))
        }
    }
}