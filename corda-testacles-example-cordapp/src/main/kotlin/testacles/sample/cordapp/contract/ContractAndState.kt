/*
 * Corda Testacles: Simple conveniences for your Corda Test Suites;
 * because who doesn't need to grow some more of those.
 *
 * Copyright (C) 2020 Manos Batsis
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package testacles.sample.cordapp.contract

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.transactions.LedgerTransaction
import testacles.sample.cordapp.contract.YoContract.YoState.YoSchemaV1.PersistentYoState
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table


/** Contract and state.*/
class YoContract : Contract {
    companion object{
        val ID = YoContract::class.qualifiedName!!
    }

    // Command.
    class Send : TypeOnlyCommandData()

    // Contract code.
    override fun verify(tx: LedgerTransaction) = requireThat {
        val command = tx.commands.requireSingleCommand<Send>()
        "There can be no inputs when Yo'ing other parties." using (tx.inputs.isEmpty())
        "There must be one output: The Yo!" using (tx.outputs.size == 1)
        val yo = tx.outputsOfType<YoState>().single()
        "No sending Yo's to yourself!" using (yo.recipient != yo.sender)
        "The Yo! must be signed by the sender." using (command.signers.contains(yo.sender.owningKey))
        //"The Yo! must be signed by the recipient." using (command.signers.contains(yo.recipient.owningKey))
    }

    // State.
    @BelongsToContract(YoContract::class)
    data class YoState(val sender: Party,
                       val recipient: Party,
                       val yo: String = "Yo!") : ContractState, QueryableState {
        override val participants get() = listOf(sender, recipient)
        //override fun toString() = "${sender.name}: $yo"
        override fun supportedSchemas() = listOf(YoSchemaV1)

        override fun generateMappedObject(schema: MappedSchema) = PersistentYoState(
                sender.name.toString(), recipient.name.toString(), yo)

        object YoSchema

        object YoSchemaV1 : MappedSchema(YoSchema.javaClass, 1, listOf(PersistentYoState::class.java)) {

            override val migrationResource: String = "yo-state-schema-v1.changelog-init"

            @Entity
            @Table(name = "yo_state")
            class PersistentYoState(
                    @Column(name = "sender")
                    var origin: String = "",
                    @Column(name = "recipient")
                    var target: String = "",
                    @Column(name = "yo")
                    var yo: String = ""
            ) : PersistentState()
        }
    }
}


