package edu.jsp.Banking_app.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.jsp.Banking_app.entity.Account;
import edu.jsp.Banking_app.entity.AccountStatus;
import edu.jsp.Banking_app.entity.Transaction;
import edu.jsp.Banking_app.entity.TransactionType;
import edu.jsp.Banking_app.exception.AccessDeniedException;
import edu.jsp.Banking_app.exception.InsufficientBalanceException;
import edu.jsp.Banking_app.exception.NotFoundException;
import edu.jsp.Banking_app.repository.AccountRepository;
import edu.jsp.Banking_app.repository.TransactionRepository;
import edu.jsp.Banking_app.exception.AccountNotActiveException;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository) {

        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    // =========================================================
    // DEPOSIT
    // =========================================================

    @Transactional
    public Transaction deposit(
            int accountId,
            BigDecimal amount) {

        // Validate amount
        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Deposit amount must be greater than zero");
        }

        // Find account
        Account account =
                accountRepository.findById(accountId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Account not found",
                                        "accountId",
                                        accountId));

        // Security check
        verifyAccountOwnership(account);

        // Account status check
        verifyAccountIsActive(account);

        // Add money
        account.setBalance(
                account.getBalance().add(amount));

        accountRepository.save(account);

        // Create transaction
        Transaction transaction = new Transaction();

        transaction.setAmount(amount);
        transaction.setTransactionType(
                TransactionType.DEPOSIT);
        transaction.setTransactionDate(
                LocalDateTime.now());
        transaction.setStatus("SUCCESS");
        transaction.setDescription(
                "Cash deposit");
        transaction.setToAccount(account);

        return transactionRepository.save(transaction);
    }

    // =========================================================
    // WITHDRAW
    // =========================================================

    @Transactional
    public Transaction withdraw(
            int accountId,
            BigDecimal amount) {

        // Validate amount
        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Withdrawal amount must be greater than zero");
        }

        // Find account
        Account account =
                accountRepository.findById(accountId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Account not found",
                                        "accountId",
                                        accountId));

        // Security check
        verifyAccountOwnership(account);

        // Account status check
        verifyAccountIsActive(account);

        // Balance check
        if (account.getBalance()
                .compareTo(amount) < 0) {

            throw new InsufficientBalanceException(
                    "Insufficient balance");
        }

        // Remove money
        account.setBalance(
                account.getBalance().subtract(amount));

        accountRepository.save(account);

        // Create transaction
        Transaction transaction = new Transaction();

        transaction.setAmount(amount);
        transaction.setTransactionType(
                TransactionType.WITHDRAWAL);
        transaction.setTransactionDate(
                LocalDateTime.now());
        transaction.setStatus("SUCCESS");
        transaction.setDescription(
                "Cash withdrawal");
        transaction.setFromAccount(account);

        return transactionRepository.save(transaction);
    }

    // =========================================================
    // TRANSFER
    // =========================================================

    @Transactional
    public Transaction transfer(
            int fromAccountId,
            int toAccountId,
            BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Transfer amount must be greater than zero");
        }

        if (fromAccountId == toAccountId) {
            throw new IllegalArgumentException(
                    "Cannot transfer to the same account");
        }

        /*
         * Always lock accounts in ascending ID order.
         * This prevents two opposite transfers from locking
         * the accounts in different orders and causing a deadlock.
         */
        int firstAccountId = Math.min(fromAccountId, toAccountId);
        int secondAccountId = Math.max(fromAccountId, toAccountId);

        Account firstAccount =
                accountRepository.findByIdForUpdate(firstAccountId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Account not found",
                                        "accountId",
                                        firstAccountId));

        Account secondAccount =
                accountRepository.findByIdForUpdate(secondAccountId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Account not found",
                                        "accountId",
                                        secondAccountId));

        Account fromAccount;
        Account toAccount;

        if (fromAccountId == firstAccountId) {
            fromAccount = firstAccount;
            toAccount = secondAccount;
        } else {
            fromAccount = secondAccount;
            toAccount = firstAccount;
        }

        // Security check: only the sender must belong to the current user.
        verifyAccountOwnership(fromAccount);

        // Both accounts must be active.
        verifyAccountIsActive(fromAccount);
        verifyAccountIsActive(toAccount);

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance");
        }

        fromAccount.setBalance(
                fromAccount.getBalance().subtract(amount));

        toAccount.setBalance(
                toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus("SUCCESS");
        transaction.setDescription("Account transfer");
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);

        return transactionRepository.save(transaction);
    }

    // =========================================================
    // GET ACCOUNT TRANSACTIONS
    // =========================================================

    public List<Transaction> getAccountTransactions(
            int accountId) {

        Account account =
                accountRepository.findById(accountId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Account not found",
                                        "accountId",
                                        accountId));

        // Security check
        verifyAccountOwnership(account);

        /*
         * We intentionally DO NOT check whether the account
         * is ACTIVE here.
         *
         * A CLOSED account should still retain its
         * transaction history.
         */

        return transactionRepository
                .findTransactionsByAccountId(accountId);
    }

    // =========================================================
    // GET SINGLE TRANSACTION
    // =========================================================

    public Transaction getTransaction(
            int transactionId) {

        Transaction transaction =
                transactionRepository.findById(transactionId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Transaction not found",
                                        "transactionId",
                                        transactionId));

        verifyTransactionOwnership(transaction);

        return transaction;
    }

    // =========================================================
    // VERIFY ACCOUNT OWNERSHIP
    // =========================================================

    private void verifyAccountOwnership(
            Account account) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "Authentication required");
        }

        // ADMIN can access any account.
        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_ADMIN"));

        if (isAdmin) {
            return;
        }

        // Account must have an owner.
        if (account.getUser() == null ||
                account.getUser().getEmail() == null) {

            throw new AccessDeniedException(
                    "Account has no valid owner");
        }

        String authenticatedEmail =
                authentication.getName();

        String accountOwnerEmail =
                account.getUser().getEmail();

        if (!accountOwnerEmail.equalsIgnoreCase(
                authenticatedEmail)) {

            throw new AccessDeniedException(
                    "You are not authorized to access this account");
        }
    }

    // =========================================================
    // VERIFY ACCOUNT IS ACTIVE
    // =========================================================

    private void verifyAccountIsActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    "Account is not active");
        }
    }

    // =========================================================
    // VERIFY TRANSACTION OWNERSHIP
    // =========================================================

    private void verifyTransactionOwnership(
            Transaction transaction) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "Authentication required");
        }

        // ADMIN can access any transaction.
        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_ADMIN"));

        if (isAdmin) {
            return;
        }

        String authenticatedEmail =
                authentication.getName();

        boolean ownsFromAccount = false;
        boolean ownsToAccount = false;

        // Check FROM account
        if (transaction.getFromAccount() != null &&
                transaction.getFromAccount().getUser() != null &&
                transaction.getFromAccount()
                        .getUser()
                        .getEmail() != null) {

            ownsFromAccount =
                    transaction.getFromAccount()
                            .getUser()
                            .getEmail()
                            .equalsIgnoreCase(
                                    authenticatedEmail);
        }

        // Check TO account
        if (transaction.getToAccount() != null &&
                transaction.getToAccount().getUser() != null &&
                transaction.getToAccount()
                        .getUser()
                        .getEmail() != null) {

            ownsToAccount =
                    transaction.getToAccount()
                            .getUser()
                            .getEmail()
                            .equalsIgnoreCase(
                                    authenticatedEmail);
        }

        if (!ownsFromAccount && !ownsToAccount) {

            throw new AccessDeniedException(
                    "You are not authorized to access this transaction");
        }
    }
}