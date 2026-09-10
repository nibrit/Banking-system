package edu.jsp.Banking_app.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.*;

import edu.jsp.Banking_app.entity.Transaction;
import edu.jsp.Banking_app.service.TransactionService;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

	private final TransactionService transactionService;

	public TransactionController(TransactionService transactionService) {

		this.transactionService = transactionService;
	}

	@PostMapping("/deposit/{accountId}")
	public Transaction deposit(@PathVariable int accountId, @RequestParam BigDecimal amount) {

		return transactionService.deposit(accountId, amount);
	}

	@PostMapping("/withdraw/{accountId}")
	public Transaction withdraw(@PathVariable int accountId, @RequestParam BigDecimal amount) {

		return transactionService.withdraw(accountId, amount);
	}

	@PostMapping("/transfer")
	public Transaction transfer(@RequestParam int fromAccountId, @RequestParam int toAccountId,@RequestParam BigDecimal amount) {

		return transactionService.transfer(fromAccountId, toAccountId, amount);
	}

	@GetMapping("/account/{accountId}")
	public List<Transaction> getAccountTransactions(@PathVariable int accountId) {

		return transactionService.getAccountTransactions(accountId);
	}

	@GetMapping("/{transactionId}")
	public Transaction getTransaction(@PathVariable int transactionId) {

		return transactionService.getTransaction(transactionId);
	}
}