# Clean Architecture Generator ✨

A CLI and Android Studio plugin for generating Clean Architecture boilerplate.

## Table of Contents
<hr />

- [Key features](#key-features)
- [Android Studio plugin](#android-studio-plugin)
    - [Usage](#usage)
- [CLI](#cli)
  - [Installation](#installation)
  - [Usage](#usage-1)
  - [Common examples](#common-examples)
  - [Manual page (optional)](#manual-page-optional)
  - [Configuration (.cagrc)](#configuration-cagrc)
- [Contributing](#contributing)
- [Support](#support)
- [Sponsor](#sponsor)
- [License](#license)

## Key features

|                                | Android Studio Plugin | CLI |
|--------------------------------|:---------------------:|:---:|
| New Clean Architecture project |          ✔️           | ✔️  |
| Generate Architecture package  |          ✔️           | ✔️  |
| Generate a new feature         |          ✔️           | ✔️  |
| Generate a use case            |          ✔️           | ✔️  |
| Generate a ViewModel           |          ✔️           | ✔️  |
| Generate a data source         |          ✔️           | ✔️  |
| Automatic git staging          |          ✔️           | ✔️  |
| Configurable                   |          ✔️           | ✔️  |

**Android Studio Plugin** is available on the IDE Plugins Marketplace.

**Terminal command** is available via Homebrew.

## Android Studio plugin

Adds multiple time-saving code generation shortcuts to Android Studio. 

### Usage

#### New Clean Architecture project
Navigate to `File` > `New` > `New Project...` and select the **Clean Architecture** template.

#### Plugin shortcuts
Right-click on relevant directories and expand the `New` menu item.

#### Settings
Settings are available under `Tools` > `Clean Architecture`.

For a working project example, visit [Clean Architecture For Android](https://github.com/EranBoudjnah/CleanArchitectureForAndroid).

## CLI

Generates Clean Architecture Android code from your terminal.

### Installation

- **Install via Homebrew:**

```bash
brew tap EranBoudjnah/cag
brew install EranBoudjnah/cag/cag
```

- **Run via installed script:**

```bash
./gradlew :cli:installDist
"./cli/build/install/cag/bin/cag" --new-feature --name=MyFeature
"./cli/build/install/cag/bin/cag" --new-view-model --name=MyViewModel
```

- **Run via Gradle (no install):**

```bash
./gradlew :cli:run --args="--new-feature --name=MyFeature"
./gradlew :cli:run --args="--new-view-model --name=MyViewModel"
```

### Usage

Usage (canonical):

```bash
cag [--new-project --name=ProjectName --package=PackageName [--no-compose] [--ktlint] [--detekt] [--ktor] [--retrofit]]... [--new-architecture [--no-compose] [--ktlint] [--detekt]]... [--new-feature --name=FeatureName [--package=PackageName]]... [--new-datasource --name=DataSourceName [--with=ktor|retrofit|ktor,retrofit]]... [--new-use-case --name=UseCaseName [--path=TargetPath]]... [--new-view-model --name=ViewModelName [--path=TargetPath]]...
```

- Full reference: `cag --help`
- Topic help: `cag --help --topic=new-feature` or `cag --help -t new-use-case`
- Man page: `man cag` (see [Manual page (optional)](#Manual-page-optional) below for generating/installing locally)

### Common examples

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

### Manual page (optional)

If you prefer to use `man` to read your documentation, this section is for you.

```bash
# Generate man page (writes cli/build/man/cag.1)
./gradlew :cli:generateManPage

# Install to a man1 directory (may require sudo for system directories)
./gradlew :cli:installManPage

# Preview after install
man cag
```

### Configuration (.cagrc)

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

## Contributing
Contributions to this project are welcome. Learn about [contributing](https://github.com/ArmynC/ArminC-AutoExec/blob/master/.github/CONTRIBUTING.md).

## Support
Reach out to me via my **[profile page](https://github.com/EranBoudjnah)**.

## Sponsor
[![Donation](https://img.shields.io/badge/Buy%20Me%20a%20Coffee-%5E%5E-green?style=flat&logo=undertale&logoColor=red&color=white)](https://github.com/sponsors/EranBoudjnah)

## License
[![License: MIT](https://img.shields.io/badge/License-MIT-lightgrey.svg)](https://www.tldrlegal.com/license/mit-license)
