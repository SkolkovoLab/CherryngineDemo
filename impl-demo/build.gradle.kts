plugins {
    id("cherryngine-micronaut-app")
}

dependencies {
    api(engine(":engine-core"))
    api(engine(":engine-ecs"))
    api(engine(":engine-physics"))
    api(engine(":lib-math"))
    api(engine(":lib-polar"))

    runtimeOnly(project(":impl-demo:minecraft"))
    runtimeOnly(project(":impl-demo:bedrock"))

    api(engine(":engine-integration:viaversion"))
    api(engine(":engine-integration:grim"))
}
