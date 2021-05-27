
# Changelog

The following sections describe major changes per version 
and can be helpful with version upgrades.


## 0.19

- Added Cordapp Config support to MockNetworkConfig
- Added NodeParamsHelper to use alongside NodeDriverHelper

## 0.18

- Support Corda OS/CE 4.7

## 0.17

- Dependency updates

## 0.16

- Fixed testcontainers custom endpoint for 4.6+

## 0.15

- Updated testcontainers to 1.15.1

## 0.14

- Added `network` property to mock network `NodeHandles`

## 0.13

- Added options and fixes for corner case scenarios 
on some CI envs, i.e. running mock, node driver and 
test containers for the same build on Circle CI. 
- Added some small delay within `RpcWaitStrategy`, 
seems Circle CI needs that for some reason.
- Refactored example to two separate module, for cordapp 
and (Spring Boot) client.

## 0.12

- Dropped Corda 4.4 support from node driver utilities.
- The API for `NodeHandles` instances provided by   
`MockNetworkHelper` and `NodeDriverHelper` has been simplified 
and merged into `AbstractNodesMap`.
- Every `cordapPackages` typo in the node driver module has 
been renamed to `cordappPackages`.
- Updated, documented and added tests for 
`MockNetworkExtension` and `MockNetworkHelper`. 
- Removed corda-testacles-thirdparty-clone and 
corda-testacles-processor modules
- Merged corda-testacles-model and corda-testacles-model-api 
modules into corda-testacles-common. You may need to 
update your imports. 

## 0.11

- Fixed the node.conf (`dataSource`) JDBC URL generated 
for PostgreSQL database containers when running on Gitlab CI.
- Added experimental mock network utilities. 

## 0.10

- Project now supports and is tested against Corda OS/CE 4.4.x to 4.6.x. 
- Containers and their config classes now require a string-based image name, 
see `NodeImageNameConfig` constants. 
- Node driver configuration, i.e. `NodeDriverNodesConfig` implementations 
like `SimpleNodeDriverNodesConfig`, now accept a `cordappProjectPackage` name. 
The property can be used to define a cordapp JAR specifically for the current Gradle module. 
For out-of-module cordapps, the `cordapPackages` property should be used.   
- Some package rearrangements have taken place, 
you may need to update your imports (e.g. `CordformDatabaseSettingsFactory`)
- Cordform node containers now use `CordformNodeContainer.logger` 
as the log consumer by default, with a log-level of "debug".

## 0.9

- Implemented proper, RPC-based waiting strategy for node containers.


## 0.8

- Added `NodeDriverNodesConfig.minimumPlatformVersion` property.
- Default `minimumPlatformVersion` is now 5.

## 0.7 

- Upgrade to and provide improved support for Corda 4.6. 
__Note__: Your cordapps must include 
[database migration scripts](https://docs.corda.net/docs/corda-enterprise/4.6/cordapps/database-management.html) 
for using corda 4.6+.
- To avoid human error, an explicit `DockerImageName` is now 
required for all testcontainers-based usage patterns.
- Deprecated `CordformNetworkContainer`'s alternative constructor.   


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
