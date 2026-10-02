package com.tanwar.market_pilot.rag.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "document_chunks")
open class DocumentChunkEntity(

    @Id
    val id: UUID,

    @Column(name = "document_id", nullable = false)
    val documentId: UUID,

    @Column(name = "chunk_text", nullable = false, columnDefinition = "TEXT")
    val chunkText: String,

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(name = "embedding", nullable = false)
    val embedding: FloatArray,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime

) {
    protected constructor() : this(
        id = UUID.randomUUID(),
        documentId = UUID.randomUUID(),
        chunkText = "",
        embedding = FloatArray(768),
        createdAt = LocalDateTime.MIN
    )
}