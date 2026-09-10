package edu.jsp.Banking_app.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import edu.jsp.Banking_app.entity.Account;
import edu.jsp.Banking_app.entity.User;
import edu.jsp.Banking_app.exception.AccessDeniedException;
import edu.jsp.Banking_app.exception.NotFoundException;
import edu.jsp.Banking_app.repository.AccountRepository;
import edu.jsp.Banking_app.repository.UserRepository;
import java.math.BigDecimal;

import org.springframework.transaction.annotation.Transactional;

import edu.jsp.Banking_app.entity.AccountStatus;
import edu.jsp.Banking_app.exception.AccountCannotBeClosedException;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(
            AccountRepository accountRepository,
            UserRepository userRepository) {

        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // CREATE ACCOUNT
    // =========================================================

    public Account addAccount(long userId, Account account) {

        // Make sure the logged-in user is allowed
        // to create an account for this userId.
        verifyUserAccess(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "User not found",
                                "id",
                                userId
                        ));

        // Explicitly associate the account with the user.
        account.setUser(user);

        return accountRepository.save(account);
    }

    // =========================================================
    // DELETE ACCOUNT
    // =========================================================

    public String deleteAccount(long userId, int accountId) {

        // Check that the requested userId itself is accessible.
        verifyUserAccess(userId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Account not found",
                                "acc_id",
                                accountId
                        ));

        // Also verify that THIS account belongs to the user.
        verifyAccountOwnership(accountId);

        accountRepository.delete(account);

        return "Account Deleted";
    }

    // =========================================================
    // FETCH ALL ACCOUNTS OF USER
    // =========================================================

    public List<Account> fetchAllByUserId(long userId) {

        verifyUserAccess(userId);

        // Make sure the user exists.
        userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "User not found",
                                "id",
                                userId
                        ));

        return accountRepository.fetchAllByUserId(userId);
    }

    // =========================================================
    // FETCH ACCOUNT BY ACCOUNT ID
    // =========================================================

    public Optional<Account> fetchByAccountId(int accountId) {

        // First verify ownership.
        verifyAccountOwnership(accountId);

        return accountRepository.findById(accountId);
    }

    // =========================================================
    // VERIFY USER ACCESS
    // =========================================================

    private void verifyUserAccess(long requestedUserId) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "Authentication required"
            );
        }

        // ADMIN can access any user's accounts.
        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_ADMIN")
                        );

        if (isAdmin) {
            return;
        }

        // Our JWT subject contains the user's email.
        String authenticatedEmail =
                authentication.getName();

        User requestedUser =
                userRepository.findById(requestedUserId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "User not found",
                                        "id",
                                        requestedUserId
                                ));

        // Compare authenticated user with requested user.
        if (!requestedUser.getEmail()
                .equalsIgnoreCase(authenticatedEmail)) {

            throw new AccessDeniedException(
                    "You are not authorized to access this user's accounts"
            );
        }
    }

    // =========================================================
    // VERIFY ACCOUNT OWNERSHIP
    // =========================================================

    private void verifyAccountOwnership(int accountId) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "Authentication required"
            );
        }

        // ADMIN can access any account.
        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_ADMIN")
                        );

        if (isAdmin) {
            return;
        }

        Account account =
                accountRepository.findById(accountId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Account not found",
                                        "acc_id",
                                        accountId
                                ));

        User owner = account.getUser();

        if (owner == null ||
                owner.getEmail() == null ||
                !owner.getEmail()
                        .equalsIgnoreCase(
                                authentication.getName()
                        )) {

            throw new AccessDeniedException(
                    "You are not authorized to access this account"
            );
        }
    }
    @Transactional
    public Account closeAccount(int accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Account not found",
                                "accountId",
                                accountId));

        verifyAccountOwnership(accountId);

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountCannotBeClosedException(
                    "Account is already closed");
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new AccountCannotBeClosedException(
                    "Account cannot be closed while balance is not zero");
        }

        account.setStatus(AccountStatus.CLOSED);

        return accountRepository.save(account);
    }
}