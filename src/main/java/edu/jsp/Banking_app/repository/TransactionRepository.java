package edu.jsp.Banking_app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import edu.jsp.Banking_app.entity.Transaction;

public interface TransactionRepository
        extends JpaRepository<Transaction, Integer> {

    @Query("""
           SELECT t
           FROM Transaction t
           WHERE t.fromAccount.acc_id = ?1
              OR t.toAccount.acc_id = ?1
           ORDER BY t.transactionDate DESC
           """)
    List<Transaction> findTransactionsByAccountId(int accountId);
}