
# Changelog

The following sections describe major changes per version 
and can be helpful with version upgrades.

## 0.4 

- Temporarily reverted from Gradle's sensible `java-library` 
plugin and ack to using `compile` dependency variance until 
POM publishing can be properly fixed. 

- Fixed missing `corda-testacles-nodedriver` module to  
published artifacts. I feel sorry for any waste of time this 
might have caused you.

- Moved all tests to `corda-testacles-cordapp-example`.

## 0.3 

- Applied `java-library` Gradle plugin to testcontainers, 
nodedriver modules.
- `CordformNetworkExtension` will now use a clone of `nodesDir` 
by default. See also `CordformNetworkContainer.cloneNodesDir`. 

## 0.2 

- Adds a basic NodeDriver test helper and JUnit5 extension.

## 0.1 

- Initial release, adds Cordform-based testcontainers and JUnit5 extension.
