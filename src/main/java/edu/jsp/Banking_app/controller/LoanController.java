package edu.jsp.Banking_app.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import edu.jsp.Banking_app.entity.Loan;
import edu.jsp.Banking_app.service.LoanService;

@RestController
@RequestMapping("/loans")
public class LoanController {

	private final LoanService loanService;

	public LoanController(LoanService loanService) {
		this.loanService = loanService;
	}

	// APPLY

	@PostMapping("/user/{userId}")
	public Loan applyLoan(@PathVariable Long userId, @RequestBody Loan loan) {

		return loanService.applyLoan(userId, loan);
	}

	// APPROVE

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{loanId}/approve")
	public Loan approveLoan(@PathVariable long loanId) {

		return loanService.approveLoan(loanId);
	}

	// REJECT

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{loanId}/reject")
	public Loan rejectLoan(@PathVariable long loanId) {

		return loanService.rejectLoan(loanId);
	}

	// DISBURSE

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{loanId}/disburse/{accountId}")
	public Loan disburseLoan(@PathVariable long loanId, @PathVariable int accountId) {

		return loanService.disburseLoan(loanId, accountId);
	}

	// PAY EMI

	@PutMapping("/{loanId}/pay-emi")
	public Loan payEMI(@PathVariable long loanId) {

		return loanService.payEMI(loanId);
	}

	// USER LOANS

	@GetMapping("/user/{userId}")
	public List<Loan> getUserLoans(@PathVariable long userId) {

		return loanService.fetchAllByUserId(userId);
	}

	// LOAN BY ID

	@GetMapping("/{loanId}")
	public Loan getLoan(@PathVariable long loanId) {

		return loanService.fetchByLoanId(loanId);
	}
}