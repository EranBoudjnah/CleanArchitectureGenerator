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

Usage:

```bash
cag [--new-feature=FeatureName [--package=PackageName]]... [--new-datasource=DataSourceName [--with=ktor|retrofit|ktor,retrofit]]...
```

##### New Feature Options
```bash
  --new-feature=<FeatureName> | --new-feature <FeatureName> | -nf=<FeatureName> | -nf <FeatureName> | -nf<FeatureName>
    Generate a new feature named FeatureName
  --package=<PackageName> | --package <PackageName> | -p=<PackageName> | -p <PackageName> | -p<PackageName>
    (Optional) Override the feature package for the preceding feature
```

##### New DataSource Options
```bash
  --new-datasource=<Name> | --new-datasource <Name> | -nds=<Name> | -nds <Name> | -nds<Name>
    Generate a new DataDource named NameDataSource
  --with=ktor|retrofit|ktor,retrofit | -w=ktor|retrofit|ktor,retrofit
    Attach dependencies to the preceding new data source
```

##### Other Options
```bash
  --help, -h
    Show the help document for cag
```

When run without arguments, the command prints a short usage and suggests using `--help` or `-h` for more options.

### Android Studio plugin

- New data source dialog now includes two checkboxes:
  - Add Ktor dependencies
  - Add Retrofit dependencies

These control whether the generated `datasource/implementation` module `build.gradle.kts` includes Ktor and/or Retrofit dependencies.
