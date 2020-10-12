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


import com.github.manosbatsis.partiture.flow.PartitureFlow
import com.github.manosbatsis.partiture.flow.io.output.TypedOutputSingleStateConverter
import com.github.manosbatsis.partiture.flow.io.output.TypedOutputStatesConverter
import mypackage.cordapp.contract.YoContract
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC


/**
 * Create a Yo! transaction/state for the input recipient/party.
 */
@InitiatingFlow
@StartableByRPC
class YoFlow1(input: YoDto) : PartitureFlow<YoDto, YoContract.YoState>(
        input = input, // Input can be anything
        inputConverter = YoInputConverter(),// Our custom IN converter
        // OUT build-in converter
        outputConverter = TypedOutputSingleStateConverter(YoContract.YoState::class.java))


/**
 * A basic responder for countersigning and listening for finality
 */
@InitiatedBy(YoFlow1::class)
class YoFlow1Responder(otherPartySession: FlowSession) : BaseYoFlowResponder(otherPartySession)


/**
 * Create a Yo! transaction/state for each input recipient/party.
 * Tests TypedOutputStatesConverter
 */
@InitiatingFlow
@StartableByRPC
class YoFlow2(input: YoDto) : PartitureFlow<YoDto, List<YoContract.YoState>>(
        input = input, // Input can be anything
        inputConverter = YoInputConverter(),// Our custom IN converter
        // OUT build-in converter
        outputConverter = TypedOutputStatesConverter(YoContract.YoState::class.java))

/**
 * A basic responder for countersigning and listening for finality
 */
@InitiatedBy(YoFlow2::class)
class YoFlow2Responder(otherPartySession: FlowSession) : BaseYoFlowResponder(otherPartySession)
