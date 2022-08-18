# Material-UI-Swing HACKING guide

## Table of Content

- Introduction
- How run the Demo
- Code Style
- Commit Style
- How to make the release

## Introduction

Welcome to the HACKING guide and let's peek into how a day in the life of a Material-UI-Swing maintainer looks like.

After reading this you should be ready to contribute to the repository and also be one of
the next maintainers in the future if you would like!

Let's begin

## How run the Demo

Material-UI-Swing is a library without any main file, so in order to make manual test you need to run the `MaterialUISwingDemo.java` file or
`DemoGUITest.java` with one of your preferred editor, but how to configure the editor is outside of the scope of this guide.

In addition, we provide a Gradle task to run the main demo of the repository (`DemoGUITest.java`) and the command is `./gradlew runDemo`.
If you are using intelliJ you need just to run the file with any button run that you can see around the IDE.

Remember while you are running the gradle script to disable the release mode by setting `RELEASE_ENABLE=false` inside the file `gradle.properties`.

N.B: We do all with gradle, so you should take some time to explore your main gradle scrip that are `build.gradle` and `local-deploy.gradle`.

P.S: If you are looking some issue where to work, please checkout the issue with the label `good first issue` and `mentor`

## Code style

To ensure consistency throughout the source code, these rules are to be kept in mind:

- All public API methods **must be documented**. (Details TBC).
- Call `./gradlew googleJavaFormat` before committing
- If you can, GPG-sign at least your top commit when filing a PR

### If You Don’t Know The Right Thing, Do The Simplest Thing
Sometimes the right way is unclear, so it’s best not to spend time on it. It’s far easier to rewrite simple code than complex code, too.

### Use of `FIXME`

There are two cases in which you should use a `/* FIXME: */`
comment: one is where an optimization seems possible, but it’s unclear if it’s yet worthwhile,
and the second one is in the case of an ugly corner case which could be improved (and may be in a following patch).

There are always compromises in code: eventually, it needs to ship. `FIXME` is grep-fodder for yourself and others,
as well as useful warning signs if we later encounter an issue in some part of the code.

### Write For Today: Unused Code Is Buggy Code

Don’t overdesign: complexity is a killer. If you need a fancy data structure, start with a brute force linked list. Once that’s working,
perhaps consider your fancy structure, but don’t implement a generic thing. Use `/* FIXME: ...*/` to salve your conscience.

### Keep Your Patches Reviewable
Try to make a single change at a time. It’s tempting to do “drive-by” fixes as you see other things, and a minimal amount is unavoidable,
but you can end up shaving infinite yaks. This is a good time to drop a `/* FIXME: ...*/` comment and move on.


## Commit Style

The commit style is one of the more important concepts when managing a repository like Material-UI-Swing, and in particular,
the commit style is used to generate the changelog for the next release.

Each commit message consists of a **header**, a **body** and a **footer**. The header has a special
format that includes a **type**, a **scope** and a **subject**:

```
<type>(<scope>): <subject>
<BLANK LINE>
<body>
<BLANK LINE>
<footer>
```

The **header** is mandatory while the **scope** of the header is optional.

All lines in a commit message should be at most 100 characters! This ensures better readability on GitHub as well as in various git tools.

The footer should contain a [closing reference to an issue](https://help.github.com/articles/closing-issues-via-commit-messages/) if any.

Some couple of examples are:

```
docs(changelog): update changelog to beta.5
```

```
fix(release): need to depend on the latest rxjs and zone.js

The version in our package.json gets copied to the one we publish, and users need the latest of these.
```

### Types

- **feat**: A new feature
- **fix**: A bug fix
- **deprecate**: Deprecate a feature and start to the removing process (3 official release or 1 major release)
- **remove**: End of life for the feature.

### Scopes

In this repo there is no scope! Lucky you :)


### Subject

The subject contains a succinct description of the change:

- use the imperative, present tense: "change" not "changed" nor "changes"
- don't capitalize the first letter
- no dot (.) at the end

### Body

You are free to put all the content you want inside the body, but if you are fixing
an exception or some wrong behavior you must put the details or stacktrace inside the body.

An example of commit body is the following one

```
checker: fixes overloading operation when the type is optimized

The stacktrace is the following one

} expected `Foo` not `Foo` - both operands must be the same type for operator overloading
   11 | }
   12 |
   13 | fn (_ Foo) == (_ Foo) bool {
      |                  ~~~
   14 |     return true
   15 | }---
description: "`Rust core lightning Rust framework` HACKING guide"
---
```

## How to make the release

This is the most fun part and also is the most difficult one in auto release repository.

To prepare for the release, these steps must be followed:

- Bump the version number in the package before the release, and the version inside the `changelog.json` in the package root;
    - Use a Personal Access Token or generate a [new one](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token).
- Generate the changelog related to the package:
    - `export GITHUB_TOKEN="your_token"`
    - Run `changelog-cli` in the root of the directory
    - You should ping @vincenzopalazzo to accept the release in the Maven page

N.B: Part of this document is stolen from [core lightning](https://github.com/ElementsProject/lightning/blob/master/doc/HACKING.md) docs made with from @rustyrussell 's experience.

>Programs must be written for people to read, and only incidentally for machines to execute.
>                                                                            - Someone

Cheers!

[Vincent](https://github.com/vincenzopalazzo)
