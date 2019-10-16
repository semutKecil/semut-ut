package com.kawanansemut.semutut.entity

import java.io.Serializable
import java.time.LocalDateTime
import com.kawanansemut.semutut.utility.U
import org.hibernate.annotations.NaturalId
import javax.persistence.*


@MappedSuperclass
open class Base : Serializable {
    @Id
    val id: Long = U.idGenerator.nextId()

    @Basic
    var updated: LocalDateTime = LocalDateTime.now()

    @Basic
    var created: LocalDateTime = LocalDateTime.now()


    @PreUpdate
    internal fun onPreUpdate() {
        updated = LocalDateTime.now()
    }

    @PrePersist
    internal fun onPrePersist() {
        updated = created
    }
}

