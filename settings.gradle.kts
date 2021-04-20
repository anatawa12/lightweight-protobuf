rootProject.name = "lightweight-protobuf"
include("compiler")
include("generated")
include("benchmark")

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.squareup.wire") {
                useModule("com.squareup.wire:wire-gradle-plugin:${requested.version}")
            }
        }
    }
}
