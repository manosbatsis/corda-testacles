
# Mock Networks 

The `corda-testacles-mocknetwork` 
module provides two ways to more easily configure and launch 
a Corda [MockNetwork](https://docs.corda.net/docs/corda-os/4.6/api-testing.html#flow-testing):

- `MockNetworkExtension`: a convenient JUnit5 extension 
- `MockNetworkHelper`: a helper that can be used directly in your tests 
in conjunction with JUnit5's `@BeforeAll` and `@AfterAll` etc. annotations. 

## Gradle Setup

Add the module dependency to your Gradle build:

```groovy
dependencies {
    //...
    // Add Corda Testacles Mock Network 
    testImplementation("com.github.manosbatsis.corda.testacles:corda-testacles-mocknetwork:$testacles_version")
    // Add JUnit etc.
}
```
