# Changelog

## [0.3.0]

### Added
- Added inspections to the plugin
- Added a reference project to README for easier onboarding (#29)
- Added Homebrew installation instructions to README (#28)
- Added backwards compatibility for Android Studio Meerkat (#25)
- Added settings for custom git path (#20)
- Added version output to the CLI (#26)
- Added git automation to the CLI for staging and commits (#19)

### Changed
- Renamed generated binary from cli to cag (#27)
- Migrated UI to use JetBrains UI DSL v2 (#24)
- Deduplicated version catalog entries (#23)
- Refreshed and updated the README content (#30)

### Fixed
- Removed dexmaker from presentation-test module (#22)
- Ensured new project minimum SDK version respects IDE-specified value (#21)

## [0.2.0]

### Added
- Added optional git initialization for new projects (#16)
- Added automatic git staging option (#14)
- Added man page support and improved help documentation (#13)
- Implemented configuration file support (#12)
- Added settings panel for better user configuration (#11)
- Added ktlint and detekt options for new features (#8)
- Added app module selector for new features (#6)
- Added Gradle check task validation (#5)

### Changed
- Improved version catalog logic and fixed related bugs
- Simplified version catalog implementation (#9)
- Moved all requests to a dedicated request package
- Updated plugin name and template thumbnail
- Improved README documentation

### Fixed
- Fixed multiple generation glitches
- Fixed issue where new architecture module option was shown with existing modules (#7)
- Various bug fixes in version catalog logic

## [0.1.0] - Initial Release
- Initial release of the Clean Architecture Generator plugin
