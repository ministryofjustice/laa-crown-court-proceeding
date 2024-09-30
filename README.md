## Laa Crown Court Proceeding

This is a Java based Spring Boot Application which will be hosted on Cloud Platform.

[![MIT license](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## Developer setup

1. Go through with this [Java Developer On-boarding Check List](https://dsdmoj.atlassian.net/wiki/spaces/ASLST/pages/3738468667/Java+Developer+Onboarding+Check+List/) and complete all tasks.
2. Request a team member to be added to the repository.
3. Create a GPG (more detail further down on the page) key and create a PR. Someone from the team will approve the PR.
4. This is a document to outline the general guideline [Developer Guidelines](https://dsdmoj.atlassian.net/wiki/spaces/ASLST/pages/3896049821/Developer+Guidelines).

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
op version
```

If the command is not found, [follow the steps on the 1Password developer docs to get the CLI set-up](https://developer.1password.com/docs/cli/get-started/).

Once you're ready to run the application:

```sh
./startup-local.sh
```

### Decrypting values files

The values YAML files are encrypted using [git-crypt](https://github.com/AGWA/git-crypt).

To be able to view and/or edit these files, you will need to decrypt them first.

You will first need to create a GPG key. See [Create a GPG Key](https://docs.publishing.service.gov.uk/manual/create-a-gpg-key.html) for details on how to do this with `GPGTools` (GUI) or `gpg` (command line).
You can install either from a terminal or just download the UI version.

```
brew update
brew install gpg
brew install git-crypt
```

Once you have done this, a team member who already has access can add your key by running `git-crypt add-gpg-user USER_ID`\* and creating a pull request to this repo.

Once this has been merged you can decrypt your local copy of the repository by running `git-crypt unlock`.

\*`USER_ID` can be your key ID, a full fingerprint, an email address, or anything else that uniquely identifies a public key to GPG (see "HOW TO SPECIFY A USER ID" in the gpg man page).

### Open API

OpenAPI 3 specification is available [here](http://localhost:8087/open-api/api-docs/) and Swagger UI
[here](http://localhost:8087/open-api/swagger-ui/index.html)
