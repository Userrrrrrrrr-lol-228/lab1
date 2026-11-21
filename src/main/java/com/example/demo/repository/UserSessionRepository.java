package com.example.demo.repository;

import com.example.demo.model.UserSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findByRefreshToken(String refreshToken);

    /**
     * Поиск с блокировкой строки (SELECT ... FOR UPDATE) до конца транзакции:
     * второй параллельный обмен того же refresh ждёт первый и затем видит статус USED.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from UserSession s where s.refreshToken = :token")
    Optional<UserSession> findByRefreshTokenForUpdate(@Param("token") String refreshToken);
}
