pluginManagement {
    includeBuild("engine/build-logic")
}

rootProject.name = "CherryngineDemo"


include(
    "impl-demo",
    "impl-demo:minecraft",
)

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("engine/gradle/libs.versions.toml"))
        }
    }
}

includeBuild("engine")