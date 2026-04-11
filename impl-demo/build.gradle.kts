plugins {
    id("cherryngine-micronaut-app")
}

dependencies {
    api(engine(":engine-core"))
    api(engine(":engine-ecs"))
    api(engine(":engine-physics"))
    api(engine(":lib-math"))

    runtimeOnly(project(":impl-demo:minecraft"))

//    api(engine(":engine-integration:viaversion"))
//    api(engine(":engine-integration:grim"))
}
