# Nefele

[![Build Status](https://travis-ci.com/nefele-org/nefele-desktop.svg?branch=master)](https://travis-ci.com/nefele-org/nefele-desktop)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/nefele-org/nefele-desktop.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/nefele-org/nefele-desktop/alerts/)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/nefele-org/nefele-desktop.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/nefele-org/nefele-desktop/context:java)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](/LICENSE)

## Details
Description bla-bla...

![Diagram](docs/images/diagram.png)

## How to build from sources?

First, download and unzip **JDK 11.0.x** ([Windows](https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_windows-x64_bin.zip), [Mac](https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_osx-x64_bin.tar.gz), [Linux](https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_linux-x64_bin.tar.gz))

```shell script
# Clone repository
$> git clone https://github.com/nefele-org/nefele-desktop --depth=1
$> cd nefele-desktop

# Set JAVA_HOME Environment variable pointing your JDK 11
$> export JAVA_HOME=/path_to_jdk11

# Run Gradle
$> ./gradlew run --console=rich
``` 