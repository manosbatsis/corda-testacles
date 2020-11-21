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
    // Postgres  driver, if needed and not in the main classpath
    testImplementation("org.postgresql:postgresql:42.2.18")
    // Add JUnit etc.
}
```

## Requirements

- You need at least Docker server version 1.6.0 installed
- For Corda 4.6+, the created container entrypoints will initialize  
the node with `run-migration-scripts`, so your cordapp must include 
                                                            [database migration scripts](https://docs.corda.net/docs/corda-enterprise/4.6/cordapps/database-management.html).