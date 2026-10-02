package com.tanwar.market_pilot.security

import org.springframework.stereotype.Component
import java.util.UUID

data class CurrentUser( val userId: UUID )

@Component
class UserContext {

    /*
     * Temporary implementation.
     *
     * Later this will come from Spring Security / JWT.
     */
    fun currentUser(): CurrentUser {
        return CurrentUser(
            userId = UUID.fromString(
                "11111111-1111-1111-1111-111111111111"
            )
        )
    }
}
