package com.kawanansemut.semutut.utility

import org.apache.commons.validator.routines.EmailValidator

class Validate {
    private var valid = false
    fun isValid(): Boolean = valid
    fun orThrow(ex: Exception) {
        if (!valid) {
            throw ex
        }
    }

    companion object {
        fun isTrue(value: Boolean): Validate {
            val v = Validate()
            v.valid = value
            return v
        }

        fun stringNotEmpty(value: String): Validate {
            return isTrue(value.isNotEmpty())
        }

        fun stringLengthMin(value: String, min: Int): Validate {
            return isTrue(value.length >= min)
        }

        fun stringLengthMax(value: String, max: Int): Validate {
            return isTrue(value.length <= max)
        }

        fun stringLengthInBetween(value: String, min: Int, max: Int): Validate {
            return isTrue(value.length in min..max)
        }

        fun emailFormat(value: String): Validate {
            return isTrue(EmailValidator.getInstance().isValid(value))
        }

        fun usernameFormat(value: String): Validate {
            return isTrue(value.matches(Regex("^[a-zA-Z0-9_]*$")))
        }
    }
}