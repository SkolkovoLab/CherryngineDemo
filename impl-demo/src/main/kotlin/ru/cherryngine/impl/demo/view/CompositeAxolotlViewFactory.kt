package ru.cherryngine.impl.demo.view

class CompositeAxolotlViewFactory(
    private val factories: List<AxolotlViewFactory>
) : AxolotlViewFactory {
    override fun create(): AxolotlView =
        CompositeAxolotlView(factories.map { it.create() })
}
