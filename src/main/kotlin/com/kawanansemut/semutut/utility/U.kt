package com.kawanansemut.semutut.utility

import java.util.UUID

class U {
    companion object {
        fun UUID() = UUID.randomUUID().toString()
        fun minUUID() = UUID().replace("-", "")
        val idGenerator: IdGenerator by lazy {
            return@lazy IdGenerator()
        }
    }
}

class IdGenerator {
    var idGeneratorFormula: IdGeneratorFormula = DefaultIdGeneratorFormula()
    fun nextId() = idGeneratorFormula.nextId()
}

interface IdGeneratorFormula {
    fun nextId(): Long
}

class DefaultIdGeneratorFormula : IdGeneratorFormula {
    val snowFlake = SnowFlake(1, 1, System.currentTimeMillis())
    override fun nextId() = snowFlake.nextId()
}