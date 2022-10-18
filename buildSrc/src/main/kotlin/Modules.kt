object Modules {
    const val DATA = ":data"
    const val NETWORK = ":network"

    object Features {
        object RoundUp {
            const val UI = ":features:roundup:ui"
            const val DOMAIN = ":features:roundup:domain"
        }
    }

    object Core {
        const val UI = ":core:ui"
        const val MODELS = ":core:models"
        const val FACTORIES = ":core:factories"
        const val TEST = ":core:test"
    }
}
