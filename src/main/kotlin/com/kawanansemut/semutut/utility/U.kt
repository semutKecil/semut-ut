package com.kawanansemut.semutut.utility

import java.util.UUID

class U {
    companion object {
        fun UUID() = UUID.randomUUID().toString()
        fun minUUID() = UUID().replace("-", "")
        val snowFlakeContainer: IdGenerator by lazy {
            return@lazy DefaultIdGenerator()
        }
    }
}

interface IdGenerator {
    fun generateId():Long
}

class DefaultIdGenerator:IdGenerator{
    val snowFlake = SnowFlake(1,1,System.currentTimeMillis())
    override fun generateId() = snowFlake.nextId()
}