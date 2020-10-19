
# Changelog

The following sections describe major changes per version 
and can be helpful with version upgrades.

## 0.3 

- Applied `java-library` Gradle plugin to testcontainers, 
nodedriver modules.
- `CordformNetworkExtension` will now use a clone of `nodesDir` 
by default. See also `CordformNetworkContainer.cloneNodesDir`. 

## 0.2 

- Adds a basic NodeDriver test helper and JUnit5 extension.

## 0.1 

- Initial release, adds Cordform-based testcontainers and JUnit5 extension.
