package com.minhhai.newsmanagement.repository;

import com.minhhai.newsmanagement.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
    boolean existsByUsername(String username);
}
