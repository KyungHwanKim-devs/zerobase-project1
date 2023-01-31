package com.example.accountinit.repository;

import com.example.accountinit.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AccountUserRepository extends JpaRepository<AccountUser, Long> {
}
