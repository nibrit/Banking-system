package edu.jsp.Banking_app.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import edu.jsp.Banking_app.exception.LoanBusinessException;

import edu.jsp.Banking_app.entity.Account;
import edu.jsp.Banking_app.entity.AccountStatus;
import edu.jsp.Banking_app.entity.Loan;
import edu.jsp.Banking_app.entity.LoanStatus;
import edu.jsp.Banking_app.entity.Transaction;
import edu.jsp.Banking_app.entity.TransactionType;
import edu.jsp.Banking_app.entity.User;
import edu.jsp.Banking_app.exception.InsufficientBalanceException;
import edu.jsp.Banking_app.exception.NotFoundException;
import edu.jsp.Banking_app.repository.AccountRepository;
import edu.jsp.Banking_app.repository.LoanRepository;
import edu.jsp.Banking_app.repository.TransactionRepository;
import edu.jsp.Banking_app.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import edu.jsp.Banking_app.exception.AccessDeniedException;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public LoanService(
            LoanRepository loanRepository,
            UserRepository userRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository) {

        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }



    public Loan applyLoan(Long userId, Loan loan) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "User not found",
                                "userId",
                                userId));
        verifyUserAccess(userId);
        validateLoanApplication(loan);

        loan.setUser(user);
        loan.setApplyDate(LocalDateTime.now());

        loan.setStatus(LoanStatus.PENDING);

        loan.setBalance(loan.getAmount());

        loan.setEmi(
                calculateEMI(
                        loan.getAmount(),
                        loan.getInterestRate(),
                        loan.getTenureMonths()
                )
        );

        return loanRepository.save(loan);
    }

   
    // APPROVE LOAN
  

    public Loan approveLoan(long loanId) {

        Loan loan = getLoan(loanId);

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new LoanBusinessException(
                    "Only pending loans can be approved");
        }

        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovalDate(LocalDateTime.now());

        return loanRepository.save(loan);
    }

   
    // REJECT LOAN


    public Loan rejectLoan(long loanId) {

        Loan loan = getLoan(loanId);

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new LoanBusinessException(
                    "Only pending loans can be rejected");
        }

        loan.setStatus(LoanStatus.REJECTED);

        return loanRepository.save(loan);
    }

   
    // DISBURSE LOAN
   

    @Transactional
    public Loan disburseLoan(
            long loanId,
            int accountId) {

        Loan loan = getLoan(loanId);

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new LoanBusinessException(
                    "Only approved loans can be disbursed");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Account not found",
                                "accountId",
                                accountId));

        // Make sure the account belongs to the loan applicants
        if (loan.getUser().getId() != account.getUser().getId()) {
            throw new IllegalArgumentException(
                    "Account does not belong to loan applicant");
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new LoanBusinessException(
                    "Loan cannot be disbursed to a closed account");
        }

        // Add loan amount to account
        account.setBalance(
                account.getBalance().add(loan.getAmount())
        );

        accountRepository.save(account);

        loan.setAccount(account);
        loan.setDisbursementDate(LocalDateTime.now());
        loan.setStatus(LoanStatus.ACTIVE);

        Loan savedLoan = loanRepository.save(loan);

        // Record disbursement as a deposit transaction
        Transaction transaction = new Transaction();

        transaction.setAmount(loan.getAmount());
        transaction.setTransactionType(
                TransactionType.DEPOSIT
        );
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus("SUCCESS");
        transaction.setDescription("Loan disbursement");
        transaction.setToAccount(account);

        transactionRepository.save(transaction);

        return savedLoan;
    }

   
    // PAY EMI
   

    @Transactional
    public Loan payEMI(long loanId) {

        Loan loan = getLoan(loanId);
        verifyUserAccess(loan.getUser().getId());

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new LoanBusinessException(
                    "Loan is not active");
        }

        Account account = loan.getAccount();

        if (account == null) {
            throw new LoanBusinessException(
                    "Loan is not linked to an account");
        }

        BigDecimal payment = loan.getEmi();

        // If final payment is smaller than EMI,
        // pay only the remaining balance.
        if (loan.getBalance().compareTo(payment) < 0) {
            payment = loan.getBalance();
        }

        if (account.getBalance().compareTo(payment) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient account balance to pay EMI");
        }

        // Deduct money from account
        account.setBalance(
                account.getBalance().subtract(payment)
        );

        accountRepository.save(account);

        // Reduce loan balance
        loan.setBalance(
                loan.getBalance().subtract(payment)
        );

        if (loan.getBalance()
                .compareTo(BigDecimal.ZERO) == 0) {

            loan.setStatus(LoanStatus.CLOSED);
        }

        Loan savedLoan = loanRepository.save(loan);

        // Record loan payment
        Transaction transaction = new Transaction();

        transaction.setAmount(payment);
        transaction.setTransactionType(
                TransactionType.LOAN_PAYMENT
        );
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus("SUCCESS");
        transaction.setDescription(
                "Loan EMI payment"
        );
        transaction.setFromAccount(account);

        transactionRepository.save(transaction);

        return savedLoan;
    }

    
    // CALCULATE EMI
    

    public BigDecimal calculateEMI(
            BigDecimal principal,
            BigDecimal annualInterestRate,
            int tenureMonths) {

        BigDecimal monthlyRate =
                annualInterestRate
                        .divide(
                                BigDecimal.valueOf(1200),
                                10,
                                RoundingMode.HALF_UP
                        );

        // Zero-interest loan
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {

            return principal
                    .divide(
                            BigDecimal.valueOf(tenureMonths),
                            2,
                            RoundingMode.HALF_UP
                    );
        }

        double p = principal.doubleValue();
        double r = monthlyRate.doubleValue();
        double n = tenureMonths;

        double emi =
                (p * r * Math.pow(1 + r, n))
                / (Math.pow(1 + r, n) - 1);

        return BigDecimal.valueOf(emi)
                .setScale(2, RoundingMode.HALF_UP);
    }

   
    // GET USER LOANS
    

    public List<Loan> fetchAllByUserId(long userId) {

        verifyUserAccess(userId);

        return loanRepository.fetchAllByUserId(userId);
    }

   
    // GET LOAN
    

    public Loan fetchByLoanId(long loanId) {

        return getLoan(loanId);
    }

    private Loan getLoan(long loanId) {

        return loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Loan not found",
                                "loanId",
                                loanId));
    }

   
    // VALIDATION
  

    private void validateLoanApplication(Loan loan) {

        if (loan.getAmount() == null ||
                loan.getAmount()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Loan amount must be greater than zero");
        }

        if (loan.getInterestRate() == null ||
                loan.getInterestRate()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Interest rate cannot be negative");
        }

        if (loan.getTenureMonths() <= 0) {

            throw new IllegalArgumentException(
                    "Tenure must be greater than zero");
        }
    }
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

        // ADMIN can access any user's data
        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_ADMIN"));

        if (isAdmin) {
            return;
        }

        // Our JWT subject is the user's email
        String email = authentication.getName();

        User user = userRepository
                .findById(requestedUserId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "User not found",
                                "userId",
                                requestedUserId));

        if (!user.getEmail().equalsIgnoreCase(email)) {

            throw new AccessDeniedException(
                    "You are not authorized to access this user's loans"
            );
        }
    }
}
