package com.tanwar.market_pilot.rag.repository

import com.tanwar.market_pilot.rag.entity.DocumentChunkEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface DocumentChunkRepository :
    JpaRepository<DocumentChunkEntity, UUID>{
    @Query(
        value = """
            SELECT *
            FROM document_chunks
            ORDER BY embedding <=> CAST(:queryVector AS vector)
            LIMIT :limit
        """,
        nativeQuery = true
    )
    fun findSimilar(
        @Param("queryVector") queryVector: String,
        @Param("limit") limit: Int
    ): List<DocumentChunkEntity>
    }