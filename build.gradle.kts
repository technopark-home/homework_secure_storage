plugins {
    id("com.android.application") version "8.4.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.8.3" apply false
}

tasks.register("clean").configure {
    delete(layout.buildDirectory)
}
