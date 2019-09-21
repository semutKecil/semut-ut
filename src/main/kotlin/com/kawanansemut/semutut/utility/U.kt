package com.kawanansemut.semutut.utility

import java.util.UUID

class U {
    companion object {
        fun UUID() = UUID.randomUUID().toString()
        fun minUUID() = UUID().replace("-", "")
    }
}