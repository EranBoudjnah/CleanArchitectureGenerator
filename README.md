## Clean Architecture Generator

A CLI and Android Studio plugin for generating Clean Architecture boilerplate.

### CLI usage

- **Run via Gradle (no install):**

```bash
./gradlew :cli:run --args="--new-feature=MyFeature"
```

- **Run via installed script:**

```bash
./gradlew :cli:installDist
"./cli/build/install/cli/bin/cli" --new-feature=MyFeature
```

- **Run the fat jar:**

```bash
./gradlew :cli:shadowJar
java -jar "cli/build/libs/cli-all.jar" --new-feature=MyFeature
```

#### Options

```bash
usage: cag [--new-feature=FeatureName [--package=PackageName]]... [--new-datasource=DataSourceName]...

Options:
  --new-feature=FeatureName, -nf=FeatureName    Generate a new feature named FeatureName
  --package=PackageName, -p=PackageName         Override the feature package for the preceding feature
  --new-datasource=Name, -nds=Name              Generate a new data source named NameDataSource
  --help, -h                                    Show this help message and exit
```

When run without arguments, the command prints a short usage and suggests using `--help` or `-h` for more options.
