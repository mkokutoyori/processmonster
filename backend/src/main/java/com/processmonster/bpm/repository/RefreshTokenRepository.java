package com.processmonster.bpm.repository;

import com.processmonster.bpm.entity.RefreshToken;
import com.processmonster.bpm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Refresh Token Repository
 *
 * Data access layer for RefreshToken entity.
 *
 * @author ProcessMonster Team
 * @version 1.0.0
 * @since 2025-11-07
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find refresh token by token string
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find valid refresh token by user
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user " +
           "AND rt.revoked = false AND rt.expiresAt > :now")
    Optional<RefreshToken> findValidTokenByUser(
        @Param("user") User user,
        @Param("now") LocalDateTime now
    );

    /**
     * Delete all tokens for a user
     */
    void deleteByUser(User user);

    /**
     * Delete all expired tokens
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Revoke all user tokens
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = :now " +
           "WHERE rt.user = :user AND rt.revoked = false")
    int revokeUserTokens(@Param("user") User user, @Param("now") LocalDateTime now);
}
