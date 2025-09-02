## Clean Architecture Generator

A CLI and Android Studio plugin for generating Clean Architecture boilerplate including features, data sources, use cases, and architecture packages.

### CLI usage

- **Run via Gradle (no install):**

```bash
./gradlew :cli:run --args="--new-feature --name=MyFeature"
```

- **Run via installed script:**

```bash
./gradlew :cli:installDist
"./cli/build/install/cli/bin/cli" --new-feature --name=MyFeature
```

- **Run the fat jar:**

```bash
./gradlew :cli:shadowJar
java -jar "cli/build/libs/cli-all.jar" --new-feature --name=MyFeature
```

#### Options

Usage:

```bash
cag [--new-architecture [--no-compose]]... [--new-feature --name=FeatureName [--package=PackageName]]... [--new-datasource --name=DataSourceName [--with=ktor|retrofit|ktor,retrofit]]... [--new-use-case --name=UseCaseName [--path=TargetPath]]...
```

##### New Architecture Options
```bash
  --new-architecture | -na
    Generate a new Clean Architecture package with domain, presentation, and UI layers
  --no-compose | -nc
    Disable Compose support for the preceding architecture package
```

##### New Feature Options
```bash
  --new-feature --name=<FeatureName> | --new-feature --name <FeatureName> | -nf --name=<FeatureName> | -nf --name <FeatureName>
    Generate a new feature named <FeatureName>
  --package=<PackageName> | --package <PackageName> | -p=<PackageName> | -p <PackageName> | -p<PackageName>
    (Optional) Override the feature package for the preceding feature
```

##### New DataSource Options
```bash
  --new-datasource --name=<DataSourceName> | --new-datasource --name <Name> | -nds --name=<Name> | -nds --name <Name>
    Generate a new DataSource named <DataSourceName>DataSource
  --with=ktor|retrofit|ktor,retrofit | -w=ktor|retrofit|ktor,retrofit
    Attach dependencies to the preceding new data source
```

##### New UseCase Options
```bash
  --new-use-case --name=<UseCaseName> | --new-use-case --name <UseCaseName> | -nuc --name=<UseCaseName> | -nuc --name <UseCaseName>
    Generate a new use case named <UseCaseName>UseCase.
  --path=<TargetPath> | --path <TargetPath> | -p=<TargetPath> | -p <TargetPath> | -p<TargetPath>
    (Optional) Specify the target directory for the preceding use case
    By default, the target path is determined by the current location
  --input-type=<InputType> | --input-type <InputType> | -it=<InputType> | -it <InputType> | -it<InputType>
    (Optional) Specify the input data type for the preceding use case
    By default, Unit is used
  --output-type=<OutputType> | --output-type <OutputType> | -ot=<OutputType> | -ot <OutputType> | -ot<OutputType>
    (Optional) Specify the output data type for the preceding use case
    By default, Unit is used
```

##### Other Options
```bash
  --help, -h
    Show the help document for cag
```

When run without arguments, the command prints a short usage and suggests using `--help` or `-h` for more options.

### Android Studio plugin

- New menu items:
  - New architecture package
  - New feature
  - New data source

- Context menu items:
  - New use case in domain modules
