# Contributing code to ODK JavaRosa

This is a living document. If you see something that could be improved, edit this document and submit a pull request following the instructions below!

## Beware of Java APIs not available in Android
JavaRosa is used primarily as a dependency for Android apps, and its main client is [ODK Collect](http://github.com/opendatakit/collect). That means [only the subset of Java 8](https://developer.android.com/studio/write/java8-support) available in Android can be used. Furthermore, many Java 8 APIs are only available for apps that have a minimum API level of 24 or 26. Collect's minimum API level is 16.

If you want to make sure that your project will comply with Collect's requirements, you can run the `androidLint` Gradle task. This task is automatically run by our CircleCI continuous integration project and will prevent unsupported code merged to the upstream git repo. 

## Submitting a pull request
To contribute code to ODK JavaRosa, you will need to open a [pull request](https://help.github.com/articles/about-pull-requests/) which will be reviewed by the community and then merged into the core project.

1. [Set up your development environment](https://github.com/opendatakit/javarosa#setting-up-your-development-environment). 

1. To make sure you have the latest version of the code, set up this repository as [a remote for your fork](https://help.github.com/articles/configuring-a-remote-for-a-fork/) and then [sync your fork](https://help.github.com/articles/syncing-a-fork/).

1. Create a branch for the code you will be writing:

        git checkout -b NAME_OF_YOUR_BRANCH

1. If there is an [issue](https://github.com/opendatakit/javarosa/issues) corresponding to what you will work on, put `@opendatakit-bot claim` as a comment on issue to say you are claiming it. If this is your first time contributing to the repo, the bot will send you an invite. Once you accept this invite, the bot will assign you to the issue. If there is no issue yet, create one to provide background on the problem you are solving.

1. Once you've made incremental progress towards you goal, commit your changes with a meaningful commit message. Use [keywords for closing issues](https://help.github.com/articles/closing-issues-via-commit-messages/) to refer to issues and have them automatically close when your changes are merged.

        git commit -m "Do a thing. Fix #1."

1. Push changes to your fork to make them publicly available:

        git push

1. When your changes are ready to be added to the core ODK JavaRosa project, [open a pull request](https://help.github.com/articles/creating-a-pull-request/). Make sure to set the base fork to `opendatakit/javarosa`. Describe your changes in the comment, refer to any relevant issues using [keywords for closing issues](https://help.github.com/articles/closing-issues-via-commit-messages/) and tag any person you think might need to know about the changes.

1. Pull requests will be reviewed when committers have time. If you haven't received a review in 10 days, you may notify committers by putting `@opendatakit/javarosa` in a comment.

## Making sure your pull request is accepted
1. Confirm that your code compiles.

1. Verify the functionality. Ideally, include automated tests with each pull request. If that's not possible, describe in the pull request comment which cases you tried manually to confirm that your code works as expected.

1. Make sure that there is an issue that corresponds to the pull request and that it has been discussed by the community as necessary.

1. Keep your pull request focused on one narrow goal. This could mean addressing an issue with multiple, smaller pull requests. Small pull requests are easier to review and less likely to introduce bugs. If you would like to make stylistic changes to the code, create a separate pull request.

1. Run `./gradlew check` and fix any errors.

1. Write clear code. Use descriptive names and create meaningful abstractions (methods, classes).

1. Document your reasoning. Your commit messages should make it clear why each change has been made.

1. Follow the guidelines below.

## The review process
Bug fixes, pull requests corresponding to issues with a clearly stated goal and pull requests with clear tests and/or process for manual verification are given priority. Pull requests that are unclear or controversial may be tagged as `needs discussion` and/or may take longer to review.

We try to have at least two people review every pull request and we encourage everyone to participate in the review process to get familiar with the code base and help ensure higher quality. Reviewers should ask themselves some or all of the following questions:
- Was this change adequately discussed prior to implementation?
- Is the intended behavior clear under all conditions?
- What interesting cases should be verified?
- Is the behavior as intended in all cases?
- What other functionality could this PR affect? Does that functionality still work as intended?
- Was the change verified with several different devices and Android versions?
- Is the code easy to understand and to maintain?

When a pull request is first created, @opendatakit-bot tags it as `needs review` to indicate that code review is needed. Community members review the code and leave their comments, verifying that the changes included are relevant and properly address the issue. A maintainer does a thorough code review and when satisfied with the code, tags the pull request as `needs testing` to indicate the need for a manual [black-box testing](https://en.wikipedia.org/wiki/Black-box_testing) pass. A pull request may go back and forth between `needs testing` and `needs review` until the behavior is thoroughly verified. Once the behavior has been thoroughly verified, the pull request is tagged as `behavior verified`. A maintainer then merges the changes. Pull requests that need more complete reviews including review of approach and/or appropriateness are tagged with `reviews wanted`. Any community member is encouraged to participate in the review process!

JavaRosa is a library and so there is a chance that a change might unnecessarily be tested twice (once in the library, once in the downstream tool). For this reason, we only test pull requests that are large or risky. For smaller pull requests, we release snapshot builds periodically so unrelated regressions in downstream tools (e.g., Collect) are caught quickly. Once a regression is caught, we file issues for behavior changes on the downstream repo and tag those issues as `needs testing`. We also inform maintainers (e.g., in #collect-code) when a snapshot is updated to give a hint that might be the cause of new problems.

Small fixes that target very particular bugs may occasionally be merged without a second review.

## Style guidelines
For now, match the style of the code in the file you are editing.

## Code from external sources
ODK JavaRosa is released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0). Please make sure that any code you include is an OSI-approved [permissive license](https://opensource.org/faq#permissive). **Please note that if no license is specified for a piece of code or if it has an incompatible license such as GPL, using it puts the project at legal risk**.

Sites with compatible licenses (including [StackOverflow](http://stackoverflow.com/)) will sometimes provide exactly the code snippet needed to solve a problem. You are encouraged to use such snippets in ODK JavaRosa as long as you attribute them by including a direct link to the source. In addition to complying with the content license, this provides useful context for anyone reading the code. 
