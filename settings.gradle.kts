rootProject.name = "Clean Architecture Generator"

include(":core")
project(":core").projectDir = File(rootDir, "core")

include(":plugin")
project(":plugin").projectDir = File(rootDir, "plugin")

include(":cli")
project(":cli").projectDir = File(rootDir, "cli")
