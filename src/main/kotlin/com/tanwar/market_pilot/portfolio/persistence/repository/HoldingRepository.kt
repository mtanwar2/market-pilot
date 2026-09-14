package com.tanwar.market_pilot.portfolio.persistence.repository

import com.tanwar.market_pilot.portfolio.persistence.HoldingEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface HoldingRepository : JpaRepository<HoldingEntity, UUID>