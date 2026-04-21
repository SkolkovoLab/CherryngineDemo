plugins {
    id("cherryngine-micronaut-lib")
}

dependencies {
    api(project(":impl-demo"))
    api(engine(":platform-minecraft-bedrock"))
}
