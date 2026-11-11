# ODK JavaRosa

![Platform](https://img.shields.io/badge/platform-Java-blue.svg)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build](https://github.com/getodk/javarosa/actions/workflows/build.yml/badge.svg)](https://github.com/getodk/javarosa/actions/workflows/build.yml)

JavaRosa is a Java library for rendering forms that are compliant with [ODK XForms spec](http://getodk.github.io/xforms-spec). It is at the heart of many of the ODK tools. ODK JavaRosa is a fork of [JavaRosa](https://bitbucket.org/javarosa/javarosa/wiki/Home) 1.0 that has been modified to NOT run on J2ME devices. The key differences are:

* Regularly updated to ensure spec compliance
* KoBo additional instance defn. and filter paths
* remember all bind attributes and any additional attributes on `<input>`, `<select>`, `<group>`, etc. statements
* numerous enhancements and contributions from SurveyCTO and others.
* J2ME sub-projects removed

ODK JavaRosa is part of ODK, a free and open-source set of tools which help organizations author, field, and manage mobile data collection solutions. Learn more about the ODK project and its history [here](https://getodk.org/about/) and read about example ODK deployments [here](https://getodk.org/about/deployments/).

* ODK website: [https://getodk.org](https://getodk.org)
* ODK forum: [https://forum.getodk.org](https://forum.getodk.org)
* ODK developer Slack chat: [https://slack.getodk.org](https://slack.getodk.org)
* ODK developer wiki: [https://github.com/getodk/getodk/wiki](https://github.com/getodk/getodk/wiki)

## Releases

Releases are available from [Maven Central](https://central.sonatype.com/artifact/org.getodk/javarosa).

Snapshots are published for each commit to `master` and are available [here](https://central.sonatype.com/service/rest/repository/browse/maven-snapshots/org/getodk/javarosa).

## Requirements

JavaRosa works on Android API level 26+ (with desugaring enabled) and Java 17+.

## Extending

JavaRosa is intended be extended to support extra features through a "plugin" approach documented [here](PLUGINS.md) .

## Development

### Setting up your development environment

1. Fork the javarosa project ([why and how to fork](https://help.github.com/articles/fork-a-repo/))

1. Clone your fork of the project locally. At the command line:

        git clone https://github.com/YOUR-GITHUB-USERNAME/javarosa

We recommend using [IntelliJ IDEA](https://www.jetbrains.com/idea/) for development. On the welcome screen, click `Import Project`, navigate to your javarosa folder, and select the `build.gradle` file. Use the defaults through the wizard. Once the project is imported, IntelliJ may ask you to update your remote Maven repositories. Follow the instructions to do so. 
 
### Building the project
 
To build the project, go to the `View` menu, then `Tool Windows > Gradle`. `build` will be in `javarosa > Tasks > build > build`. Double-click `build` to package the application. This Gradle task will now be the default action in your `Run` menu. 

To package a jar, use the `jar` Gradle task.

### Running benchmarks

JavaRosa can be used to parse and fill very large forms on inexpensive devices and so it's important to keep an eye on performance. Benchmarks using [JMH](http://openjdk.java.net/projects/code-tools/jmh/) and the [JMH Gradle plugin](https://github.com/melix/jmh-gradle-plugin) have been introduced to compare relative performance as code changes are made. We have found that running these benchmarks with the `jmh` Gradle task or through IntelliJ can produce inconsistent results. The most reliable way we have found to run them is to first build a `jar` with the `jmhJar` Gradle task and then to run the jar:

        java -jar build/libs/javarosa-jmh.jar ChildVaccination

This also makes it easy to selectively run a subset of benchmarks by including a regular expression as an argument. In the example above, only benchmarks that include the text "ChildVaccination" will be executed. Run `java -jar build/libs/javarosa-jmh.jar -h` to see other JMH configuration options.

While benchmarks can help identify relative performance improvements or regressions, they are not always a reliable proxy for how code will perform in a realistic context. Profiling is helpful for identifying parts of the code that are worth analyzing and optimizing as well as to validate that any performance changes have the intended effects.

### Contributing code

Any and all contributions to the project are welcome. ODK JavaRosa is used across the world primarily by organizations with a social purpose so you can have real impact!

If you're ready to contribute code, see [the contribution guide](CONTRIBUTING.md).

### Downloading builds

JARs from each build are available as artifacts in [GitHub Actions](https://github.com/getodk/javarosa/actions/workflows/build.yml). Select a successful workflow run and download the `gradle-jar` artifact.

### Publishing

Releases are published to [Maven Central](https://central.sonatype.com/artifact/org.getodk/javarosa) when a commit is tagged with a version such as `v23.1.3`. Snapshots are published for each commit to `master` and are available in the [Sonatype snapshot repository](https://central.sonatype.com/repository/maven-snapshots/org/getodk/javarosa/).

Publishing is handled automatically by [GitHub Actions](https://github.com/getodk/javarosa/actions/workflows/publish.yml).

To publish a release:

1. Ensure the build passes on `master`.
2. Create and publish a Github release with a tag corresponding to the version with a `v` prefix (e.g. `v23.1.3`)

#### Manual publishing

Maintainers can also publish directly using Gradle. This requires Sonatype Central Portal credentials and a GPG private key.

Configure the following Gradle properties, either in `~/.gradle/gradle.properties` or as environment variables prefixed with `ORG_GRADLE_PROJECT_`:

- `mavenCentralUsername`
- `mavenCentralPassword`
- `signingInMemoryKey`
- `signingInMemoryKeyPassword`

For `signingInMemoryKey`, use an ASCII-armored export of the GPG private key.

Verify the version before publishing:

```bash
./gradlew properties -q | grep '^version:'
```

Then publish:

```bash
./gradlew publishToMavenCentral
```

The version comes from `git`. Tagged commits publish releases, while untagged commits publish snapshots.

After publishing, update downstream tools such as ODK Collect and ODK Validate to use the new version.
