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
package com.github.manosbatsis.corda.testacles.containers.boot.components

import com.github.manosbatsis.corbeans.spring.boot.corda.web.CordaPathFragmentNodeController
import io.swagger.v3.oas.annotations.Operation
import mypackage.cordapp.contract.YoContract
import mypackage.cordapp.workflow.YoDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.Optional

@RestController
class Controller : CordaPathFragmentNodeController() {

    @Autowired
    lateinit var yoService: YoService

    @PostMapping("yo")
    @Operation(summary = "Create/send a Yo!")
    fun createYo(
            @PathVariable nodeName: Optional<String>,
            @RequestBody input: YoDto
    ): ResponseEntity<YoContract.YoState> {
        return ResponseEntity(yoService.createYo(
                nodeName.get(),
                input),
                CREATED)
    }
}
