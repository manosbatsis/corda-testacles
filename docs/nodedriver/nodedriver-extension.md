
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
                SimpleNodeDriverNodesConfig (
                        // Optional, used *only* for the current  
                        // Gradle module, if a cordapp.
                        cordappProjectPackage = SampleCordapp::class.java.`package`.name,
                        // Optional; package names are used to pickup  
                        // cordapp or cordaCompile dependencies
                        cordapPackages = listOf<String>(PartitureFlow::class.java.`package`.name),
                        nodes = mapOf("partya" to ALICE_NAME.toNodeParams(), "partyb" to BOB_NAME.toNodeParams()),
                        // All bellow are optional/defaults
                        notarySpec = TestNotaryProperties(),
                        flowOverrides = emptyList(),
                        poolParams = PoolParams(),
                        minimumPlatformVersion = 5,
                        debug = false
                    )
    }

        override var cordapPackages: List<String> = mutableListOf(),
        override var cordappProjectPackage: String? = null,
        override var nodes: Map<String, NodeParams> = mutableMapOf(),
        override var bnmsServiceType: String? = null,
        override var notarySpec: TestNotaryProperties = TestNotaryProperties(),
        override var flowOverrides: List<String> = mutableListOf(),
        override var poolParams: PoolParams = PoolParams(),
        override val minimumPlatformVersion: Int = minimumPlatformVersionDefault,
        override val debug: Boolean = false


    // The extension implements a ParameterResolver
    // for NodeHandles
    @Test
    fun `Can retrieve node identity`(nodeHandles: NodeHandles) {
        val nodeA: NodeHandle = nodeHandles.getNodeByKey("partya")
        assertTrue(nodeA.nodeInfo.legalIdentities.isNotEmpty())
    }
}
```
