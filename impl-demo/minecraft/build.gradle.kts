plugins {
    id("cherryngine-micronaut-lib")
}

dependencies {
    api(project(":impl-demo"))
    api(engine(":engine-minecraft"))
    api(engine(":lib-polar"))
}
