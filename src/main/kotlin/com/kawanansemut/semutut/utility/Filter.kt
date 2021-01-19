package com.kawanansemut.semutut.utility

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.persistence.criteria.*

enum class FILTEROP {
    EQ,
    NEQ,
    GT,
    LT,
    GE,
    LE,
    LIKE,
    EQD,
    NEQD,
    EQT,
    NEQT,
    GTD,
    GTT,
    LTD,
    LTT,
    GED,
    GET,
    LED,
    LET,
    ISNULL,
    ISNOTNULL
}

class PredicateNumber<T : Number, X>(private val field: Expression<T>, private val operator: FILTEROP, private val v: T, private val cb: CriteriaBuilder, private val root: Root<X>) {

    fun build(): Predicate? {
        return when (operator) {
            FILTEROP.GT, FILTEROP.GTT, FILTEROP.GTD -> cb.gt(field, v)
            FILTEROP.LT, FILTEROP.LTD, FILTEROP.LTT -> cb.lt(field, v)
            FILTEROP.GE, FILTEROP.GED, FILTEROP.GET -> cb.ge(field, v)
            FILTEROP.LE, FILTEROP.LED, FILTEROP.LET -> cb.le(field, v)
            FILTEROP.EQ, FILTEROP.EQD, FILTEROP.EQT -> cb.equal(field, v)
            FILTEROP.NEQ, FILTEROP.NEQD, FILTEROP.NEQT -> cb.notEqual(field, v)
            FILTEROP.ISNULL -> cb.isNull(field)
            FILTEROP.ISNOTNULL -> cb.isNotNull(field)
            else -> null
        }
    }
}


class FilterDataBuilder<T>(private val fd: FilterData, private val cob: Class<T>) {

    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    private fun buildLocalDatetimePredicate(fd: FilterData, root: Root<T>, cb: CriteriaBuilder): Predicate? {

        /**
         *  Expression<Integer> year = cb.function("year", Integer.class, root.get<LocalDateTime>(fd.fi));
        Expression<Integer> month = cb.function("month", Integer.class, root.get<LocalDateTime>(fd.fi));
        Expression<Integer> day = cb.function("day", Integer.class, root.get<LocalDateTime>(fd.fi));

        // Create expressions that extract time parts:
        Expression<Integer> hour = cb.function("hour", Integer.class, time);
        Expression<Integer> minute = cb.function("minute", Integer.class, time);
        Expression<Integer> second = cb.function("second", Integer.class, ts);
         */

        val intDateExp = cb.sum(cb.sum(cb.prod(cb.function("year", Integer::class.java, root.get<LocalDateTime>(fd.fi)), 10000), cb.prod(cb.function("month", Integer::class.java, root.get<LocalDateTime>(fd.fi)), 100)), cb.function("day", Integer::class.java, root.get<LocalDateTime>(fd.fi)))
        val intTimeExp = cb.sum(
                cb.sum(cb.prod(cb.function("hour", Integer::class.java, root.get<LocalDateTime>(fd.fi)), 3600),
                        cb.prod(cb.function("minute", Integer::class.java, root.get<LocalDateTime>(fd.fi)), 60)
                ),
                cb.function("second", Integer::class.java, root.get<LocalDateTime>(fd.fi)))



        return when (fd.o!!) {
            FILTEROP.EQ -> cb.equal(root.get<LocalDateTime>(fd.fi), LocalDateTime.parse(fd.v!!, dateTimeFormatter))
            FILTEROP.NEQ -> cb.notEqual(root.get<LocalDateTime>(fd.fi), LocalDateTime.parse(fd.v!!, dateTimeFormatter))
            FILTEROP.GT -> cb.greaterThan(root.get<LocalDateTime>(fd.fi), LocalDateTime.parse(fd.v!!, dateTimeFormatter))
            FILTEROP.LT -> cb.lessThan(root.get<LocalDateTime>(fd.fi), LocalDateTime.parse(fd.v!!, dateTimeFormatter))
            FILTEROP.GE -> cb.greaterThanOrEqualTo(root.get<LocalDateTime>(fd.fi), LocalDateTime.parse(fd.v!!, dateTimeFormatter))
            FILTEROP.LE -> cb.lessThanOrEqualTo(root.get<LocalDateTime>(fd.fi), LocalDateTime.parse(fd.v!!, dateTimeFormatter))
            FILTEROP.EQD, FILTEROP.LED, FILTEROP.GED, FILTEROP.LTD, FILTEROP.GTD, FILTEROP.NEQD -> PredicateNumber(
                    intDateExp, fd.o!!, fd.v!!.replace("-", "").toInt(), cb, root
            ).build()

            FILTEROP.EQT, FILTEROP.LET, FILTEROP.GET, FILTEROP.LTT, FILTEROP.GTT, FILTEROP.NEQT -> {
                val stv = fd.v!!.split(':').map { it.toInt() }
                val tVal = (stv[0] * 3600) + (stv[1] * 60) + stv[2]
                PredicateNumber(intTimeExp, fd.o!!, tVal, cb, root).build()
            }

            FILTEROP.ISNULL -> cb.isNull(root.get<LocalDateTime>(fd.fi))
            FILTEROP.ISNOTNULL -> cb.isNotNull(root.get<LocalDateTime>(fd.fi))
            else -> null
        }
    }

    fun buildPredicate(root: Root<T>, cq: CriteriaQuery<*>, cb: CriteriaBuilder): Predicate? {

        val fields = cob.declaredFields.toMutableList()
        fields.addAll(cob.superclass.declaredFields.toMutableList())
        val predicate: Predicate?

        if (fd.fi != null && fd.o != null && fd.v != null && fields.any { it.name == fd.fi }) {
            val field = fields.first { it.name == fd.fi }

            predicate =  when(fd.o){
                FILTEROP.EQ -> {
                    if (field.type.isEnum) {
                        cb.equal(root.get<Enum<*>>(fd.fi), field.type.enumConstants.first { any -> any.toString() == fd.v!! })
                    } else {
                        when (field.type) {
                            Boolean::class.java -> cb.equal(root.get<Boolean>(fd.fi), fd.v!!.toBoolean())
                            LocalDateTime::class.java -> cb.equal(root.get<LocalDateTime>(fd.fi), LocalDateTime.parse(fd.v!!, dateTimeFormatter))
                            String::class.java -> cb.equal(cb.lower(root.get<String>(fd.fi)), fd.v!!.toLowerCase())
                            else -> cb.equal(root.get<Any>(fd.fi), fd.v)
                        }
                    }
                }
                FILTEROP.NEQ -> {
                    if (field.type.isEnum) {
                        cb.notEqual(root.get<Enum<*>>(fd.fi), field.type.enumConstants.first { any -> any.toString() == fd.v!! })
                    } else {
                        when (field.type) {
                            Boolean::class.java -> cb.notEqual(root.get<Boolean>(fd.fi), fd.v!!.toBoolean())
                            LocalDateTime::class.java -> cb.notEqual(root.get<LocalDateTime>(fd.fi), LocalDateTime.parse(fd.v!!, dateTimeFormatter))
                            String::class.java -> cb.notEqual(cb.lower(root.get<String>(fd.fi)), fd.v!!.toLowerCase())
                            else -> cb.notEqual(root.get<Any>(fd.fi), fd.v)
                        }
                    }
                }
                FILTEROP.LIKE -> cb.like(cb.lower(root.get<String>(fd.fi).`as`(String::class.java)), fd.v!!.toLowerCase())
                FILTEROP.ISNULL -> cb.isNull(root.get<Any>(fd.fi))
                FILTEROP.ISNOTNULL -> cb.isNotNull(root.get<Any>(fd.fi))
                else -> {
                    when (field.type) {
                        Int::class.java -> PredicateNumber<Int, T>(root.get(fd.fi!!), fd.o!!, fd.v!!.toInt(), cb, root).build()
                        Float::class.java -> PredicateNumber<Float, T>(root.get(fd.fi!!), fd.o!!, fd.v!!.toFloat(), cb, root).build()
                        Long::class.java -> PredicateNumber<Long, T>(root.get(fd.fi!!), fd.o!!, fd.v!!.toLong(), cb, root).build()
                        Double::class.java -> PredicateNumber<Double, T>(root.get(fd.fi!!), fd.o!!, fd.v!!.toDouble(), cb, root).build()
                        LocalDateTime::class.java -> buildLocalDatetimePredicate(fd, root, cb)
                        else -> null
                    }
                }
            }
        } else if (fd.and != null && fd.and!!.isNotEmpty()) {
            predicate = cb.and(*this.fd.and!!.mapNotNull { FilterDataBuilder(it, cob).buildPredicate(root, cq, cb) }.toTypedArray())
        } else if (fd.or != null && fd.or!!.isNotEmpty()) {
            predicate = cb.or(*this.fd.or!!.mapNotNull { FilterDataBuilder(it, cob).buildPredicate(root, cq, cb) }.toTypedArray())
        }
        
        return predicate
    }
}

class FilterData : Serializable {
    var fi: String? = null
    var v: String? = null
    var o: FILTEROP? = null
    var f: Array<String>? = null
        set(value) {
            if (value != null && value.size == 3) {
                this.fi = value[0]
                this.o = FILTEROP.valueOf(value[1])
                this.v = value[2]
            }
            field = value
        }
    var and: Array<FilterData>? = null
    var or: Array<FilterData>? = null

    fun toJson(): String {
        val jsonMapper = jacksonObjectMapper()
        return jsonMapper.writeValueAsString(this)
    }

    fun <T> toPredicate(root: Root<T>, cq: CriteriaQuery<*>, cb: CriteriaBuilder, cls: Class<T>): Predicate? {
        return FilterDataBuilder(this, cls).buildPredicate(root, cq, cb)
    }

    companion object {
        fun fromJson(json: String): FilterData {
            val jsonMapper = jacksonObjectMapper()
            return jsonMapper.readValue(json)
        }

        fun filter(field: String, operator: FILTEROP, value: String): FilterData {
            val fd = FilterData()
            fd.f = arrayOf(field, operator.name, value)
            return fd
        }

        fun and(vararg filterData: FilterData): FilterData {
            val fd = FilterData()
            fd.and = filterData.map { it }.toTypedArray()
            return fd
        }

        fun or(vararg filterData: FilterData): FilterData {
            val fd = FilterData()
            fd.or = filterData.map { it }.toTypedArray()
            return fd
        }
    }
}
