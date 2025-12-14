package com.taskmanager.auth.repository;

import com.taskmanager.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с пользователями
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = :attempts WHERE u.username = :username")
    void updateFailedLoginAttempts(@Param("attempts") int attempts, @Param("username") String username);

    @Modifying
    @Query("UPDATE User u SET u.accountNonLocked = :locked, u.lockTime = CURRENT_TIMESTAMP WHERE u.username = :username")
    void lockUser(@Param("locked") boolean locked, @Param("username") String username);
}
