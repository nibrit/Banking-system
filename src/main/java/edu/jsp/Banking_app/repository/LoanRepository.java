package edu.jsp.Banking_app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import edu.jsp.Banking_app.entity.Loan;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query("SELECT l FROM Loan l WHERE l.user.id = ?1")
    List<Loan> fetchAllByUserId(long id);
}