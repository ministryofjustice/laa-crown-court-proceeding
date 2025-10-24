## Laa Crown Court Proceeding

This is a Java based Spring Boot Application which will be hosted on Cloud Platform.

[![MIT license](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## Developer setup

1. Go through with this [Java Developer On-boarding Check List](https://dsdmoj.atlassian.net/wiki/spaces/ASLST/pages/3738468667/Java+Developer+Onboarding+Check+List/) and complete all tasks.
2. Request a team member to be added to the repository.
3. This is a document to outline the general guideline [Developer Guidelines](https://dsdmoj.atlassian.net/wiki/spaces/ASLST/pages/3896049821/Developer+Guidelines).

### Pre-requisites

1. Docker
2. SSH
3. An editor/IDE of some sort
4. Gradle
5. AWS CLI
6. Kubernetes CLI
7. Helm
8. CircleCI CLI (optional)

We're using [Gradle](https://gradle.org/) to build the application. This also includes plugins for generating IntelliJ configuration.

## Documentation

### Obtaining environment variables for running locally

To run the app locally, you will need to download the appropriate environment variables from the team
vault in 1Password. These environment variables are stored as a .env file, which docker-compose uses
when starting up the service. If you don't see the team vault, speak to your tech lead to get access.

To begin with, make sure that you have the 1Password CLI installed:

```sh
op --version
```

If the command is not found, [follow the steps on the 1Password developer docs to get the CLI set-up](https://developer.1password.com/docs/cli/get-started/).

Once you're ready to run the application:

```sh
./start-local.sh
```

### Open API

OpenAPI 3 specification is available [here](http://localhost:8087/open-api/api-docs/) and Swagger UI
[here](http://localhost:8087/open-api/swagger-ui/index.html)

## 🧹 Code formatting with Spotless

This project uses [**Spotless**](https://github.com/diffplug/spotless) to enforce consistent Java code style across all modules.

### Why

Consistent formatting ensures cleaner diffs, easier reviews, and fewer style conflicts between IDEs.  
Spotless runs automatically in CI and will fail the build if any files don’t conform to the configured format.

### How it works

Spotless is configured in [`build.gradle`](build.gradle) to:

- Format Java source files under `src/*/java/**`
- Use the **Palantir Java Format** (a Google-style formatter with 4-space indentation and 120-character line width)
- Clean up imports (`removeUnusedImports`, `forbidWildcardImports`, `importOrder`)
- Trim trailing whitespace and ensure files end with a newline
- Exclude generated or build directories

In CI, the Gradle `build` task automatically depends on `spotlessCheck`, so any formatting issues will cause the build to fail before tests or SonarQube analysis run.

### Local usage

You can run Spotless locally in two ways:

| Command                   | Description                                    |
| ------------------------- | ---------------------------------------------- |
| `./gradlew spotlessCheck` | Checks formatting and reports any violations   |
| `./gradlew spotlessApply` | Automatically fixes formatting issues in place |

If you see a failure such as:

```bash
> Task :spotlessCheck FAILED
The following files had format violations:
  src/main/java/uk/gov/laa/.../MyClass.java
Run './gradlew spotlessApply' to fix these violations.
```

...simply run `./gradlew spotlessApply`, commit the changes, and re-push.

### Developer Tips:

- You don’t need to install any IDE plugin — Spotless ensures consistent results across environments.
- You can safely ignore `.git/hooks/pre-push` — formatting is enforced in CI.
- It’s good practice to run `./gradlew spotlessApply` before opening a PR to avoid unnecessary CI failures.
