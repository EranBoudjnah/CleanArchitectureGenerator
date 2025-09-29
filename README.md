## Clean Architecture Generator

A CLI and Android Studio plugin for generating Clean Architecture boilerplate including features, data sources, use cases, ViewModels, and architecture packages.

### Android Studio plugin

Adds multiple time-saving code generation shortcuts to Android Studio. 

Key features:

- New Clean Architecture project template

- New menu items:
    - New architecture package
    - New feature
    - New use case in domain modules
    - New ViewModel in presentation modules
    - New data source

### CLI usage

Helps generate Android Clean Architecture code from the terminal.

- **Run via Gradle (no install):**

```bash
./gradlew :cli:run --args="--new-feature --name=MyFeature"
./gradlew :cli:run --args="--new-view-model --name=MyViewModel"
```

- **Run via installed script:**

```bash
./gradlew :cli:installDist
"./cli/build/install/cli/bin/cli" --new-feature --name=MyFeature
"./cli/build/install/cli/bin/cli" --new-view-model --name=MyViewModel
```

- **Run the fat jar:**

```bash
./gradlew :cli:shadowJar
java -jar "cli/build/libs/cli-all.jar" --new-feature --name=MyFeature
java -jar "cli/build/libs/cli-all.jar" --new-view-model --name=MyViewModel
```

#### Options

Usage:

```bash
cag [--new-architecture [--no-compose] [--ktlint] [--detekt]]... [--new-feature --name=FeatureName [--package=PackageName]]... [--new-datasource --name=DataSourceName [--with=ktor|retrofit|ktor,retrofit]]... [--new-use-case --name=UseCaseName [--path=TargetPath]]... [--new-view-model --name=ViewModelName [--path=TargetPath]]...
```

##### New Architecture Options
```bash
  --new-architecture | -na
    Generate a new Clean Architecture package with domain, presentation, and UI layers
  --no-compose | -nc
    Disable Compose support for the preceding architecture package
  --ktlint | -kl
    Enable ktlint for the preceding architecture package
  --detekt | -d
    Enable detekt for the preceding architecture package
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

##### New ViewModel Options
```bash
  --new-view-model --name=<ViewModelName> | --new-view-model --name <ViewModelName> | -nvm --name=<ViewModelName> | -nvm --name <ViewModelName>
    Generate a new ViewModel named <ViewModelName>ViewModel.
  --path=<TargetPath> | --path <TargetPath> | -p=<TargetPath> | -p <TargetPath> | -p<TargetPath>
    (Optional) Specify the target directory for the preceding ViewModel
    By default, the target path is determined by the current location
```

##### Other Options
```bash
  --help, -h
    Show the help document for cag
```

When run without arguments, the command prints a short usage and suggests using `--help` or `-h` for more options.

### CLI configuration (.cagrc)

You can configure library and plugin versions used by the CLI via a simple INI-style config file named `.cagrc`.

- Locations:
  - Project root: `./.cagrc`
  - User home: `~/.cagrc`

- Precedence:
  - Values in the project `.cagrc` override values in `~/.cagrc`.

- Sections:
  - `[new.versions]` — applied when generating new projects (e.g., `--new-project`).
  - `[existing.versions]` — applied when generating into an existing project (e.g., new architecture, feature, data source, use case, or view model).

- Keys correspond to version keys used by the generator, for example: `kotlin`, `androidGradlePlugin`, `composeBom`, `composeNavigation`, `retrofit`, `ktor`, `okhttp3`, etc.

Example `~/.cagrc`:

```
[new.versions]
kotlin=2.2.10
composeBom=2025.08.01

[existing.versions]
retrofit=2.11.0
ktor=3.0.3
```

Example `./.cagrc` (project overrides):

```
[new.versions]
composeBom=2025.09.01

[existing.versions]
okhttp3=4.12.0
```

With the above, new projects will use `composeBom=2025.09.01` (from project), `kotlin=2.2.10` (from home). For operations on existing projects, `retrofit=2.11.0` (home) and `okhttp3=4.12.0` (project) will be applied.
