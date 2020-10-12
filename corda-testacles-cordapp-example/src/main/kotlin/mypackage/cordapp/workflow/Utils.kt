/*
 * Corda Testacles: Test suite toolkit for Corda developers.
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
package mypackage.cordapp.workflow


import co.paralleluniverse.fibers.Suspendable
import com.github.manosbatsis.partiture.flow.PartitureResponderFlow
import com.github.manosbatsis.partiture.flow.call.CallContext
import com.github.manosbatsis.partiture.flow.call.CallContextEntry
import com.github.manosbatsis.partiture.flow.delegate.initiating.PartitureFlowDelegateBase
import com.github.manosbatsis.partiture.flow.io.input.InputConverter
import com.github.manosbatsis.partiture.flow.tx.TransactionBuilderWrapper
import com.github.manosbatsis.partiture.flow.tx.responder.SimpleTypeCheckingResponderTxStrategy
import mypackage.cordapp.contract.YO_CONTRACT_ID
import mypackage.cordapp.contract.YoContract
import net.corda.core.flows.FlowSession
import net.corda.core.identity.CordaX500Name
import net.corda.core.serialization.CordaSerializable


/** Used as flow input, to send a recipient a message */
@CordaSerializable
data class YoDto(
        val recipient: CordaX500Name,
        val message: String
)

/** Uses the input [YoDto] to create an [YoContract.YoState] TX  */
class YoInputConverter : PartitureFlowDelegateBase(), InputConverter<YoDto> {

    @Suspendable
    override fun convert(input: YoDto): CallContext {
        // Resolve the recipient
        val recipient = this.clientFlow.toWellKnownParty(input.recipient)
        // Prepare a TX builder
        val txBuilder = TransactionBuilderWrapper(clientFlow.getFirstNotary())
                .addOutputState(
                    YoContract.YoState(clientFlow.ourIdentity, recipient, input.message),
                    YO_CONTRACT_ID)
                .addCommand(YoContract.Send())
        // Return a TX context with builder and participants
        return CallContext(CallContextEntry(txBuilder))
    }
}

/** Base responder */
open class BaseYoFlowResponder(
        otherPartySession: FlowSession
) : PartitureResponderFlow(
        otherPartySession = otherPartySession,
        responderTxStrategy = SimpleTypeCheckingResponderTxStrategy(
                YoContract.YoState::class.java)
)
