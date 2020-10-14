
# NodeDriver Extension for JUnit5

Sample RPC test using `NodeDriverNetworkExtension`:

```kotlin
/** Sample test using [NodeDriverNetworkExtension] */
@ExtendWith(NodeDriverNetworkExtension::class)
class NodeDriverNetworkExtensionTest {

    companion object {

        // Marks the field
        // as a config for the extension
        @NodeDriverExtensionConfig
        @JvmStatic
        val nodeDriverConfig: NodeDriverNodesConfig =
                TestConfigUtil.myCustomNodeDriverConfig()
    }

    // The extension implements a ParameterResolver
    // for NodeHandles
    @Test
    fun `Can retrieve node identity`(nodeHandles: NodeHandles) {
        val nodeA: NodeHandle = nodeHandles.getNodeByKey("partya")
        assertTrue(nodeA.nodeInfo.legalIdentities.isNotEmpty())
    }
}
```
