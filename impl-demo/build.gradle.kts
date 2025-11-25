plugins {
    id("cherryngine-micronaut-app")
}

dependencies {
    api(engine(":engine-core"))

    api(engine(":lib-polar"))

    api(engine(":engine-integration:viaversion"))
//    api(engine(":engine-integration:grim"))

    api("io.github.quillraven.fleks:Fleks:2.12")
}
