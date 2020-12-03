
# NodeDriver Extension for JUnit5

Sample flow test using `MockNetworkExtension`:

```kotlin
/** Sample test using [MockNetworkExtension] */
@ExtendWith(MockNetworkExtension::class)
@Tag("mocknetwork")
class MockNetworkExtensionTest {

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(MockNetworkExtensionTest::class.java)

        // Marks the field
        // as a config for the extension
        @MockNetworkExtensionConfig
        @JvmStatic
        val mockNetworkConfig: MockNetworkConfig =
                MockNetworkConfig (
                    // Config params, see "NodeDriver Helper"  
                    // section for details
                )
    }

    @Test
    fun `Can send a yo`(nodeHandles: NodeHandles) {
        val nodeA = nodeHandles.getNode("partya")
        val nodeB = nodeHandles.getNode("partyb")
        val yoDto = YoDto(
                recipient = nodeB.info.legalIdentities.first().name,
                message = "Yo from A to B!")
        val yoState = nodeA.startFlow(YoFlow1(yoDto)).getOrThrow()
        assertEquals(yoDto.message, yoState.yo)
        assertEquals(yoDto.recipient, yoState.recipient.name)

    }
}
```
