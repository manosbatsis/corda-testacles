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
            NodeDriverHelper(myCustomNodeDriverConfig())
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
                .getNodeByKey("partya")
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
                .getNodeByKey("partya")
        assertTrue(nodeA.nodeInfo.legalIdentities.isNotEmpty())
    }
}
```