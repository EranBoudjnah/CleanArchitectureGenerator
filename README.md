## Clean Architecture Generator

A CLI and Android Studio plugin for generating Clean Architecture boilerplate including features, data sources, and use cases.

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
cag [--new-feature=FeatureName [--package=PackageName]]... [--new-datasource=DataSourceName [--with=ktor|retrofit|ktor,retrofit]]... [--new-use-case=UseCaseName [--path=TargetPath]]...
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

##### New UseCase Options
```bash
  --new-use-case=<UseCaseName> | --new-use-case <UseCaseName> | -nuc=<UseCaseName> | -nuc <UseCaseName> | -nuc<UseCaseName>
    Generate a new use case named UseCaseName. By default, the target path is determined by the current location.
  --path=<TargetPath> | --path <TargetPath> | -p=<TargetPath> | -p <TargetPath> | -p<TargetPath>
    (Optional) Specify the target directory for the preceding use case
  --input-type=<InputType> | --input-type <InputType> | -it=<InputType> | -it <InputType> | -it<InputType>
    (Optional) Specify the input data type for the preceding use case
  --output-type=<OutputType> | --output-type <OutputType> | -ot=<OutputType> | -ot <OutputType> | -ot<OutputType>
    (Optional) Specify the output data type for the preceding use case
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

- New use case dialog now includes two optional fields:
  - Input data type: Specify the input type for the use case (e.g., String, Int, Boolean)
  - Output data type: Specify the output type for the use case (e.g., String, Int, Boolean)

These fields allow you to customize the input and output types of the generated use case instead of using the default domain model.
