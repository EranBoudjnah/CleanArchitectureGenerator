# Contributing to Clean Architecture Generator

Thank you for your interest in contributing to the Clean Architecture Generator! This project is open for contributions in the form of Pull Requests (PRs). This document provides guidelines and information to help you get started.

## Table of Contents

- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Code Style and Standards](#code-style-and-standards)
- [Testing](#testing)
- [Submitting Changes](#submitting-changes)
- [Code Review Process](#code-review-process)
- [Reporting Issues](#reporting-issues)
- [Questions and Discussion](#questions-and-discussion)

## Getting Started

Before contributing, please ensure you have:

1. **Forked the repository** to your GitHub account
2. **Cloned your fork** to your local machine
3. **Set up the development environment** (see [Development Setup](#development-setup))
4. **Created a feature branch** for your changes

## Development Setup

### Prerequisites

- **Java 21** or later
- **Gradle 8.0** or later
- **IntelliJ IDEA** or **Android Studio** (for plugin development)
- **Git**

### Initial Setup

1. **Clone your fork:**
   ```bash
   git clone https://github.com/YOUR_USERNAME/Clean-Architecture-Generator.git
   cd Clean-Architecture-Generator
   ```

2. **Add the upstream remote:**
   ```bash
   git remote add upstream https://github.com/EranBoudjnah/Clean-Architecture-Generator.git
   ```

3. **Build the project:**
   ```bash
   ./gradlew build
   ```

### Project Modules

The project consists of three main modules:

- **`cli/`** - Command-line interface for generating Clean Architecture code
- **`core/`** - Core business logic and code generation templates
- **`plugin/`** - IntelliJ Platform plugin for Android Studio integration

## Project Structure

```
├── cli/                    # Command-line interface
├── core/                   # Core business logic and templates
├── plugin/                 # IntelliJ Platform plugin
├── automation/             # Build and development automation
├── gradle/                 # Gradle configuration
├── build.gradle.kts        # Root build configuration
└── README.md               # Project documentation
```

## Code Style and Standards

### Kotlin Coding Standards

- **Function and class size matters** - Keep functions and classes focused and concise
- **No abbreviations or acronyms** - Use full words (e.g., "directory" not "dir", "arguments" not "args")
- **No comments except in tests** - Code should be self-documenting
- **Test comments should mark sections** - Use "Given", "When", "Then" comments in tests

### Code Quality Tools

The project uses several tools to maintain code quality:

- **ktlint** - Kotlin code style enforcement
- **Pre-commit hooks** - Automatic code quality checks before commits

### Running Code Quality Checks

```bash
# Format code with ktlint
./gradlew ktlintFormat

# Check code style
./gradlew ktlintCheck

# Run all checks
./gradlew check
```

## Testing

### Test Requirements

- **Write tests for new functionality** - All new features must have corresponding tests
- **Update existing tests** - When changing behavior, update or add tests accordingly
- **Test coverage** - Aim for comprehensive test coverage of new code

### Running Tests

```bash
# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :core:test
./gradlew :cli:test
./gradlew :plugin:test
```

### Test Structure

Tests should follow the Given-When-Then pattern:

```kotlin
@Test
fun `Given valid input provided when generate then generates use case`() {
    // Given
    val useCaseName = "GetUserData"
    val inputType = "UserId"
    val outputType = "User"
    val expectedName = "GetUserDataUseCase"

    // When
    val actual = useCaseGenerator.generate(useCaseName, inputType, outputType)

    // Then
    assertNotNull(actual)
    assertThat(actual.name).isEqualTo(expectedName)
}
```

## Submitting Changes

### Before Submitting

1. **Ensure all tests pass:**
   ```bash
   ./gradlew test
   ```

2. **Run code quality checks:**
   ```bash
   ./gradlew ktlintCheck
   ```

3. **Update documentation** if your changes affect:
   - CLI usage or options
   - Plugin functionality

4. **Consider updating README** if your changes add new features or change behavior

### Creating a Pull Request

1. **Push your changes** to your fork:
   ```bash
   git push origin your-feature-branch
   ```

2. **Create a Pull Request** on GitHub with:
   - **Clear title** describing the change
   - **Detailed description** of what was changed and why
   - **Reference to any related issues**
   - **Screenshots** for UI changes (if applicable)

3. **PR Title Format:**
   ```
   Type: Brief description of change
   
   Examples:
   - Feature: Add support for custom use case templates
   - Fix: Resolve issue with data source generation
   - Refactor: Improve code generation performance
   - Docs: Update CLI help documentation
   ```

### Commit Message Guidelines

Follow conventional commit format:

```
Type(scope): description

Examples:
- feat(cli): added new --template option for custom generation
- fix(core): resolved issue with package name validation
- refactor(plugin): improved dialog validation logic
- test(core): added tests for use case generator
```

## Code Review Process

### Review Checklist

All PRs will be reviewed for:

- **Code quality** - Follows project coding standards
- **Test coverage** - Adequate tests for new functionality
- **Documentation** - Updated where necessary
- **Performance** - No performance regressions
- **Security** - No security vulnerabilities
- **Backward compatibility** - Changes don't break existing functionality

### Review Timeline

- **Initial review** - Usually within 2-3 business days
- **Follow-up reviews** - Usually within 1-2 business days after changes
- **Final approval** - After all feedback is addressed

### Addressing Review Feedback

1. **Respond to all comments** - Acknowledge feedback and explain changes
2. **Make requested changes** - Update code based on reviewer suggestions
3. **Request re-review** - When ready for another review
4. **Resolve conversations** - Mark resolved comments appropriately

## Reporting Issues

### Bug Reports

When reporting bugs, please include:

- **Clear description** of the problem
- **Steps to reproduce** the issue
- **Expected vs. actual behavior**
- **Environment details** (OS, Java version, etc.)
- **Screenshots or logs** if applicable

### Feature Requests

For feature requests, please describe:

- **Use case** for the feature
- **Expected behavior** and interface
- **Benefits** to users
- **Implementation suggestions** (if any)

## Questions and Discussion

### Getting Help

- **GitHub Issues** - For bugs, feature requests, and questions
- **GitHub Discussions** - For general questions and community discussion
- **Pull Request comments** - For specific implementation questions

### Contributing Guidelines

- **Be respectful** and constructive in all interactions
- **Help others** by reviewing their PRs and answering questions
- **Follow the project's coding standards** and conventions
- **Ask questions** if you're unsure about anything

## Additional Resources

- **Project README** - Overview and usage instructions
- **Build configuration** - `build.gradle.kts` files for module details
- **Existing code** - Study existing implementations for patterns and style
- **Test files** - Examples of testing approaches and conventions

## Thank You

Thank you for contributing to the Clean Architecture Generator! Your contributions help make this tool more powerful and useful for developers around the world.

---

*This document is a living guide. If you notice any issues or have suggestions for improvement, please submit a PR to update it.*
