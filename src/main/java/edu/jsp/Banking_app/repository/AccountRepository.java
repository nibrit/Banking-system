package edu.jsp.Banking_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import edu.jsp.Banking_app.entity.Account;
import jakarta.persistence.LockModeType;

public interface AccountRepository
        extends JpaRepository<Account, Integer> {

    @Query("SELECT a FROM Account a WHERE a.user.id = ?1")
    List<Account> fetchAllByUserId(long id);

    boolean existsByAccountNo(long accountNo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.acc_id = ?1")
    Optional<Account> findByIdForUpdate(int accountId);
}