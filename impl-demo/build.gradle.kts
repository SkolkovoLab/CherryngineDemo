plugins {
    id("cherryngine-micronaut-app")
}

dependencies {
    api(engine(":engine-core"))
    api(engine(":engine-ecs"))
    api(engine(":platform-minecraft-java"))
    api(engine(":engine-physics"))
    api(engine(":lib-math"))

    runtimeOnly(project(":impl-demo:minecraft"))
    runtimeOnly(project(":impl-demo:bedrock"))

    api(engine(":platform-minecraft-java:integration:viaversion"))
//    api(engine(":platform-minecraft-java:integration:grim"))
}
