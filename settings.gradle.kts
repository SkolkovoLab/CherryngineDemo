pluginManagement {
    includeBuild("engine/build-logic")
}

rootProject.name = "CherryngineDemo"


include(
    "impl-demo",
)

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("engine/gradle/libs.versions.toml"))
        }
    }
}

includeBuild("engine")