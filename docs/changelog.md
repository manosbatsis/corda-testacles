
# Changelog

The following sections describe major changes per version 
and can be helpful with version upgrades.


## 0.67 

- Upgraded to Corda 4.6

## 0.6 

- Refactored config of Cordform containers to support 
postgres and more ad-hoc Corda networks (the latter in future 
releases). 
- Added setting that creates a Postgres DB container per 
Cordform Node. The driver will be automatically resolved from 
either _{nodeDir}/drivers_ or the classpath and added to the 
Node container. 
- Aggregated KDocs for Github pages 

## 0.5 

- Fixed volume binding permission issues with cordform-based 
testcontainers on CI environments like Gitlab 

## 0.4 

- Temporarily reverted from Gradle's sensible `java-library` 
plugin and back to using `compile` dependency variance until 
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
