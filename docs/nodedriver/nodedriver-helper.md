# NodeDriver Helper

When `NodeDriverNetworkExtension` does not provide the right trade-off between 
abstraction and API access, using `NodeDriverHelper` directly might 
be a better alternative. 

## Test Samples

Here's an RPC test using a static `NodeDriverHelper` etc. members:

```kotlin
class NodeDriverHelperStaticTest {

    companion object {

        @JvmStatic
        val nodesHelper: NodeDriverHelper by lazy {
            NodeDriverHelper(
                SimpleNodeDriverNodesConfig (
                        // Optional, used *only* for the current  
                        // Gradle module, if a cordapp.
                        cordappProjectPackage = SampleCordapp::class.java.`package`.name,
                        // Optional; package names are used to pickup  
                        // cordapp or cordaCompile dependencies
                        cordappPackages = listOf<String>(PartitureFlow::class.java.`package`.name),
                        nodes = mapOf("partya" to ALICE_NAME.toNodeParams(), "partyb" to BOB_NAME.toNodeParams()),
                        // All bellow are optional/defaults
                        notarySpec = TestNotaryProperties(),
                        flowOverrides = emptyList(),
                        poolParams = PoolParams(),
                        minimumPlatformVersion = 5,
                        debug = false
                    ))
        }

        /** Start the Corda NodeDriver network */
        @JvmStatic
        @BeforeAll
        fun beforeAll() { nodesHelper.start() }

        /** Stop the Corda network */
        @JvmStatic
        @AfterAll
        fun afterAll() { nodesHelper.stop() }

    }

    @Test
    fun `Can retrieve node identity`() {
        val nodeA: NodeHandle = nodesHelper.nodeHandles
                .getNode("partya")
        assertTrue(nodeA.nodeInfo.legalIdentities.isNotEmpty())
    }
}
```

Here's the same test with a per-class lifecycle 
and non-static `NodeDriverHelper` etc. members:

```kotlin
@TestInstance(PER_CLASS)
class NodeDriverHelperClassLfTest {

    val nodesHelper: NodeDriverHelper by lazy {
        NodeDriverHelper(myCustomNodeDriverConfig())
    }

    /** Start the Corda NodeDriver network */
    @BeforeAll
    fun beforeAll() { nodesHelper.start() }

    /** Stop the Corda network */
    @AfterAll
    fun afterAll() { nodesHelper.stop() }

    @Test
    fun `Can retrieve node identity`() {
        val nodeA: NodeHandle = nodesHelper.nodeHandles
                .getNode("partya")
        assertTrue(nodeA.nodeInfo.legalIdentities.isNotEmpty())
    }
}
```