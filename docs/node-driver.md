
# Node Driver Networks 

If your project uses the [Cordform](https://docs.corda.net/docs/corda-os/4.5/generating-a-node.html#tasks-using-the-cordform-plug-in) 
Gradle task (usually named `deployNodes`), you may find convinient the 
use of it's output as the source for your Corda network during testing. 

The `corda-testacles-testcontainers` 
module provides two ways to launch a complete Corda network 
based on the _nodes_ directory created by `Cordform`:

- `CordformNetworkExtension`: a convenient JUnit5 extension 
- `CordformNetworkContainer`: a container type that can be used directly 
in conjunction with Testcontainers' `@Testcontainers` and `@Container` annotations. 

## Gradle Config

Optional: To simplify your Gradle builds, make testing tasks dependent to `deployNodes`:

```groovy
project.afterEvaluate {
    check.dependsOn(":deployNodes")
}
```  


## Cordform Network Extension

An example of using `CordformNetworkExtension` in a simple 
RPC test:

```kotlin
@ExtendWith(CordformNetworkExtension::class)
class CordformNetworkExtensionTest {

    companion object {
        // Optional, defaults to corda/corda-zulu-java1.8-4.5
        @NodesImageName
        @JvmStatic
        val nodesImageName = DEFAULT_CORDA_IMAGE_NAME_4_5
    
        // Optional, defaults to new network
        @NodesNetwork
        @JvmStatic
        val nodesNetwork = Network.newNetwork()
    
        // Optional, defaults to auto-lookup (build/nodes, ../build/nodes)
        @NodesDir
        @JvmStatic
        val nodesDir = File(System.getProperty("user.dir"))
                .resolve("build/nodes")
    }
    
    // The extension implements a ParameterResolver 
    // for CordformNetworkContainer 
    @Test
    fun `Can send a yo`(nodesContainer: CordformNetworkContainer) {
        val rpcOps = nodesContainer.getNode("partya")
            .getRpc(/* optional user or username */)
        // Do something...
    }
}
```

## Cordform Network Container

When `CordformNodesExtension` does not provide the right trade-off between 
abstraction and API access, using `CordformNetworkContainer` directly might 
be a better alternative. Have a look at `CordformContainersSpringBootTest` 
for a sample  involving Spring Boot's `@DynamicPropertySource` annotation.

A simpler example of a `CordformNetworkContainer`-based RPC test is shown bellow:

```kotlin
@Testcontainers
class CordformNetworkContainerRpcTest {

    companion object {
        @Container 
        @JvmStatic
        val nodesContainer = CordformNetworkContainer(
                File(System.getProperty("user.dir"))
                    .parentFile.resolve("build/nodes"))
    }

    @Test
    fun `Can send a yo`() {
        val rpcOps = nodesContainer.getNode("partya")
            .getRpc(/* optional user or username */)
        // Do something...

    }
}
```