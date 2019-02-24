# Can I drop Jetifier?

Checks whether there are any dependencies using support library instead of AndroidX artifacts.

If you migrated to AndroidX, you probably have Jetifier tool enabled that converts dependencies that still depend on old artifacts to operate on AndroidX classes. Since more and more libraries are migrated to AndroidX, at some point there will be no need to have this tool enabled. This plugin can be used to identify which of the libraries you are using need to be migrated to AndroidX or bumped if the new version is already there.

## Setup

Build script snippet for plugins DSL for Gradle 2.1 and later:

``` groovy
plugins {
  id "com.github.plnice.canidropjetifier" version "0.4"
}
```

Build script snippet for use in older Gradle versions or where dynamic configuration is required:

``` groovy
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "com.github.plnice:canidropjetifier:0.4"
  }
}

apply plugin: "com.github.plnice.canidropjetifier"
```

For multi-module projects, you can apply the plugin in the top-level `build.gradle` file. It will analyze all the modules found in the project.

## Usage

Jetifier tool must be temporarily disabled to make this plugin work correctly. It can be done when calling the plugin's task:

``` bash
./gradlew -Pandroid.enableJetifier=false canIDropJetifier
```

Example output:

``` bash
========================================
Project sample
========================================

Cannot drop Jetifier due to following dependencies:

* com.android.support:cardview-v7:28.0.0

* com.squareup.leakcanary:leakcanary-android:1.6.3
  \-- com.squareup.leakcanary:leakcanary-analyzer:1.6.3
   \-- com.android.support:support-annotations:28.0.0
  \-- com.android.support:support-core-utils:26.0.0
```

## Configuration

``` groovy
canIDropJetifier {
  verbose = true // Default: false, set to true to print the dependencies tree down to the old artifact
  analyzeOnlyAndroidModules = false // Default: true, analyze only modules that use com.android.application or com.android.library plugins
  configurationRegex = ".*RuntimeClasspath" // Performance optimization: checks only configurations that match provided regex
  parallelMode = true // Default: false, experimental: run analysis of modules in parallel
  parallelModePoolSize = 4 // Default: max available processors - 1, experimental: pool size for analysis in parallel
}
```

## License

```
Copyright 2019 Miłosz Lewandowski

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
