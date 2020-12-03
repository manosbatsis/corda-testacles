# Corda Testacles [![Maven Central](https://img.shields.io/maven-central/v/com.github.manosbatsis.corda.testacles/corda-testacles-testcontainers.svg)](https://repo1.maven.org/maven2/com/github/manosbatsis/corda/testacles/) 

Simple but practical conveniences for Corda Test Suites; 
because who doesn't need to grow some more of those. 

The latest release includes both a helper class and 
[JUnit5](https://junit.org/junit5) 
extension for creating and testing against a Corda network using  
the following test approaches:

- For API Testing using a Corda [MockNetwork](https://docs.corda.net/docs/corda-os/4.6/api-testing.html#flow-testing), 
see the [Mock Network](mocknetwork/) section.
- For Integration(-ish) Testing using the Corda [Node Driver](https://docs.corda.net/docs/corda-os/4.6/tutorial-integration-testing.html), 
see the [Node Driver](nodedriver/) section.
- For real Integration Testing using docker via [Testcontainers](https://www.testcontainers.org/), 
based on the output produced by [Cordform](https://docs.corda.net/docs/corda-os/4.6/generating-a-node.html) 
see the [Test Containers](testcontainers/) section.
 
Corda Testacles is tested against both Corda Open Source 
and Enterprise, versions 4.5 or later. 

Checkout the documentation at https://manosbatsis.github.io/corda-testacles for more info.

Please note this is a work in progress, with unstable messy 
parts and so on. A sandbox really, something to help focus while trying 
to pickup a thing or two around the bits involved and make my 
day-to-day easier.



