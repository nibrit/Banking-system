package edu.jsp.Banking_app.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import edu.jsp.Banking_app.entity.Account;
import edu.jsp.Banking_app.service.AccountService;

@RestController
public class AccountController {
	@Autowired
	private AccountService accountService;
	
	@PostMapping("/user/{id}/account")
	public ResponseEntity<Account> addAccount(@PathVariable int id, @RequestBody Account a)
	{
		return new ResponseEntity<Account>(accountService.addAccount(id, a),HttpStatus.CREATED);
	}
	@DeleteMapping("/user/{id}/account/{acc_id}")
	public ResponseEntity<String> deleteAccount(@PathVariable long id,@PathVariable int acc_id) {
		return new ResponseEntity<String>(accountService.deleteAccount(id, acc_id),HttpStatus.NO_CONTENT);
	}
	@GetMapping("/user/{id}/account")
	public ResponseEntity<List<Account>> fetchAllByUserId(@PathVariable long id){
		return new ResponseEntity<List<Account>>(
		        accountService.fetchAllByUserId(id),
		        HttpStatus.OK);
	}
	@GetMapping("/account/{acc_id}")
	public ResponseEntity<Optional<Account>> fetchByAccountId(@PathVariable ("acc_id") int acc_id){
		return new ResponseEntity<Optional<Account>>(accountService.fetchByAccountId(acc_id),HttpStatus.OK);
	}
	@DeleteMapping("/account/{accountId}/close")
	public ResponseEntity<Account> closeAccount(
	        @PathVariable int accountId) {

	    return ResponseEntity.ok(
	            accountService.closeAccount(accountId));
	}
	
}
