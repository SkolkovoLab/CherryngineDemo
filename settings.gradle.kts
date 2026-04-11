pluginManagement {
    includeBuild("engine/build-logic")
}

rootProject.name = "CherryngineDemo"


include(
    "impl-demo",
    "impl-demo:minecraft",
    "impl-demo:mcprotocollib",
)

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("engine/gradle/libs.versions.toml"))
        }
    }
}

includeBuild("engine")