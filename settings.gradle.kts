rootProject.name = "lightweight-protobuf"
include("compiler")
include("benchmark")
include("file-size-test")
include("file-size-test:wire")
include("file-size-test:wire-no-kt")
include("file-size-test:google")
include("file-size-test:google-lite")
include("file-size-test:lightweight")

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.squareup.wire") {
                useModule("com.squareup.wire:wire-gradle-plugin:${requested.version}")
            }
        }
    }
}
