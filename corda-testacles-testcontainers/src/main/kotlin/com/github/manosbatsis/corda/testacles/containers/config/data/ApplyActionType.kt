package com.github.manosbatsis.corda.testacles.containers.config.data

enum class ApplyActionType {
    /** Add fragment if there is no previous value */
    ADD_MISSING,
    /** Add fragment, replacing any previous value */
    REPLACE,
    /** Do not add, but clear any previous value */
    CLEAR
}