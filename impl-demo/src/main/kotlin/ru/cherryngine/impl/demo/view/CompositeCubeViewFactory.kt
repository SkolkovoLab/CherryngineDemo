package ru.cherryngine.impl.demo.view

class CompositeCubeViewFactory(
    private val factories: List<CubeViewFactory>
) : CubeViewFactory {
    override fun create(): CubeView =
        CompositeCubeView(factories.map { it.create() })
}
