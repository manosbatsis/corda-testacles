package com.github.manosbatsis.corda.testacles.containers.cordform.config

import com.github.manosbatsis.corda.testacles.containers.config.NodeContainerConfig
import com.github.manosbatsis.corda.testacles.containers.cordform.CordformDatabaseSettings
import org.testcontainers.containers.Network
import org.testcontainers.utility.DockerImageName
import java.io.File

interface CordaNetworkConfig{
    val nodesDir: File
    val imageName: DockerImageName
    val network: Network
    val netParamsFile: File
    val nodeInfosDir: File
    val databaseSettings: CordformDatabaseSettings
    val privilegedMode: Boolean
    val notaryNodeDirs: List<File>
    val partyNodeDirs: List<File>
    val nodeConfigs: List<NodeContainerConfig>
}