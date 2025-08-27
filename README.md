## Clean Architecture Generator

A CLI and Android Studio plugin for generating Clean Architecture boilerplate.

### CLI usage

- **Run via Gradle (no install):**

```bash
./gradlew :cli:run --args="MyFeature"
```

- **Run via installed script:**

```bash
./gradlew :cli:installDist
"./cli/build/install/cli/bin/cli" MyFeature
```

- **Run the fat jar:**

```bash
./gradlew :cli:shadowJar
java -jar "cli/build/libs/cli-all.jar" MyFeature
```
