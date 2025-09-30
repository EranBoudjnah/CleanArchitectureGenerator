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

#### Usage and help

Usage (canonical):

```bash
cag [--new-project --name=ProjectName --package=PackageName [--no-compose] [--ktlint] [--detekt] [--ktor] [--retrofit]]... [--new-architecture [--no-compose] [--ktlint] [--detekt]]... [--new-feature --name=FeatureName [--package=PackageName]]... [--new-datasource --name=DataSourceName [--with=ktor|retrofit|ktor,retrofit]]... [--new-use-case --name=UseCaseName [--path=TargetPath]]... [--new-view-model --name=ViewModelName [--path=TargetPath]]...
```

- Full reference: `cag --help`
- Topic help: `cag --help --topic=new-feature` or `cag --help -t new-use-case`
- Man page: `man cag` (see below for generating/installing locally)

Common examples:

```bash
# Generate a new project
cag --new-project --name=MyApp --package=com.example.myapp

# Add architecture to an existing project/module
cag --new-architecture --ktlint --detekt

# Add a new feature
cag --new-feature --name=Profile --package=com.example.feature.profile

# Add a data source with Retrofit
cag --new-datasource --name=User --with=retrofit

# Add a use case
cag --new-use-case --name=FetchUser --path=architecture/domain/src/main/kotlin

# Add a ViewModel
cag --new-view-model --name=Profile
```

Manual page (optional):

```bash
# Generate man page (writes cli/build/man/cag.1)
./gradlew :cli:generateManPage

# Install to a man1 directory (may require sudo for system directories)
./gradlew :cli:installManPage

# Preview after install
man cag
```

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
