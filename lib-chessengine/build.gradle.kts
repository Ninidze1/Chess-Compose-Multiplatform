plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib-common:1.8.20")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.20")
}