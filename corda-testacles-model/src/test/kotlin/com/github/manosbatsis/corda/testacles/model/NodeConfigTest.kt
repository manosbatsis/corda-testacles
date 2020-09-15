package com.github.manosbatsis.corda.testacles.model

import net.corda.core.identity.CordaX500Name
import org.junit.jupiter.api.Test
import java.io.File

class NodeConfigTest {

    @Test
    fun `Can build simple node config`(){
        nodeConf{
            baseDirectory = File(System.getProperty("user.dir"), "build/node1").toPath()
            myLegalName = CordaX500Name.parse("O=PartyA,L=London,C=GB")
        }
    }
}