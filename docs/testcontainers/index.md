# Testcontainers Overview

The `corda-testacles-testcontainers` module helps Cordapp developers 
launch container-based Corda networks for 
[Integration Testing](https://en.wikipedia.org/wiki/Integration_testing). 
The module is based on [Testcontainers](https://www.testcontainers.org/) 
and uses [Docker](https://www.docker.com/) containers behind the scenes.

## Installation 

1. Make sure you meet Testcontainers' [Prerequisites](https://www.testcontainers.org/#prerequisites)
2. Add the module dependency to your Gradle build:

```groovy
dependencies {
    //...
    // Add Corda Testacles Testcontainers 
    testImplementation("com.github.manosbatsis.corda.testacles:corda-testacles-testcontainers:$testacles_version")
    // Add JUnit etc.
}
```