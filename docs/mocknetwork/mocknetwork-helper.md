# NodeDriver Helper

When `NodeDriverNetworkExtension` does not provide the right trade-off between 
abstraction and API access, using `MockNetworkHelper` directly might 
be a better alternative. 

## Configuration

`MockNetworkHelper`'s constructor accepts a `MockNetworkConfig` as follows:

```kotlin
MockNetworkHelper(
    MockNetworkConfig (/* config params */)
)
```

The `MockNetworkConfig`'s alternative constructors provide some flexibility 
in terms of defining the set of nodes. The primary constructor for example, 
accepts a list of `MockNodeParameters`:

```kotlin
MockNetworkConfig (
    // The nodes to build as a List<MockNodeParameters>
    names = listOf(
        MockNodeParameters(legalName = CordaX500Name.parse("O=PartyA, L=Athens, C=GR")),
        MockNodeParameters(legalName = CordaX500Name.parse("O=PartyB, L=Athens, C=GR"))),
    // Optional, used *only* for the current
    // Gradle module, if a cordapp.
    cordappProjectPackage = SampleCordapp::class.java.`package`.name,
    // Optional; package names are used to pickup
    // cordapp or cordaCompile dependencies
    cordappPackages = listOf<String>(PartitureFlow::class.java.`package`.name),
    // Optional, default
    threadPerNode = true,
    // Optional, default
    networkParameters = testNetworkParameters(
            minimumPlatformVersion = 1))
```

An alternative is a `CordaX500Names` object, essentially 
a wrapper for a `CordaX500Name` list:

```kotlin
MockNetworkConfig (
    // The nodes to build as a CordaX500Names instance
    names = CordaX500Names(listOf(
            CordaX500Name.parse("O=PartyA, L=Athens, C=GR"),
            CordaX500Name.parse("O=PartyB, L=Athens, C=GR"))),
    // Other params as previously shown
    )
```

Another alternative is a `OrgNames` object that wraps a list of one or more 
strings, with each entry being either an organization or X500 name. 

For entries that are simple organization names, a random locality/country 
will be selected during conversion to `CordaX500Name`:

```kotlin
MockNetworkConfig (
    // The nodes to build as an OrgNames instance
    names = OrgNames(listOf("PartyA", "PartyB")),
    // Other params as previously shown
    )
```

Finally, the last alternative is also the simplest:  
a `numberOfNodes` parameter as the `Int` number of nodes. 
In this case, the identity (i.e. `CordaX500Name`) for each node will 
be created with an organization name as "Party1", "Party2".. "PartyN", 
along with a random locality/country:

```kotlin
MockNetworkConfig (
    // The number of nodes to build
    numberOfNodes = 2,
    // Other params as previously shown
    )
```


## Test Samples

Here's an RPC test using a static `MockNetworkHelper` etc. members:

```kotlin
/** Sample class lifecycle test using the [MockNetworkHelper] directly */
@TestInstance(PER_CLASS)
@Tag("mocknetwork")
class MockNetworkHelperClassTest {

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(MockNetworkHelperClassTest::class.java)
    }

    val nodesHelper: MockNetworkHelper by lazy {
        MockNetworkHelper(myCustomMockNetworkConfig())
    }

    /** Start the Corda MockNetwork network */
    @BeforeAll
    fun beforeAll() { nodesHelper.start() }

    /** Stop the Corda network */
    @AfterAll
    fun afterAll() { nodesHelper.stop() }

    @Test
    fun `Can send a yo`() {
        val nodeA = nodesHelper.nodeHandles.getNode("partya")
        val nodeB = nodesHelper.nodeHandles.getNode("partyb")
        val yoDto = YoDto(
                recipient = nodeB.info.legalIdentities.first().name,
                message = "Yo from A to B!")
        val yoState = nodeA.startFlow(YoFlow1(yoDto)).getOrThrow()
        // Wait for the TX
        nodeHandles.network.waitQuiescent()
        assertEquals(yoDto.message, yoState.yo)
        assertEquals(yoDto.recipient, yoState.recipient.name)

    }
}

```

Here's the same test with a per-class lifecycle 
and non-static `MockNetworkHelper` etc. members:

```kotlin
/** Sample test using the [MockNetworkHelper] directly */
@Tag("mocknetwork")
class MockNetworkHelperStaticTest {

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(MockNetworkHelperStaticTest::class.java)

        @JvmStatic
        val nodesHelper: MockNetworkHelper by lazy {
            MockNetworkHelper(myCustomMockNetworkConfig())
        }

        /** Start the Corda MockNetwork network */
        @JvmStatic
        @BeforeAll
        fun beforeAll() { nodesHelper.start() }

        /** Stop the Corda network */
        @JvmStatic
        @AfterAll
        fun afterAll() { nodesHelper.stop() }

    }

    @Test
    fun `Can send a yo`() {
        // Same as with the MockNetworkHelperClassTest above

    }
}
```