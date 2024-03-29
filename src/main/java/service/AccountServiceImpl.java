package service;



import java.util.Date;
import java.util.UUID;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import entities.Account;
import entities.Transaction;
import entities.TransactionType;
import entities.User;
import exception.InsufficientBalanceException;
import exception.NotFoundException;
import exception.UnauthorizedException;
import repositories.AccountRepository;
import repositories.TransactionRepository;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private  AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Account getAccount(Long id) {
        return accountRepository.getById(id);
    }
    public Account deposit (Long id, Double amount) {
        Account accountManaged = accountRepository.getById(id);
        accountManaged.setBalance(accountManaged.getBalance() + amount);
         return accountRepository.save(accountManaged);
    }

    @Override
    public Account withdraw(Long id, Double amount) {
        Account accountManaged = accountRepository.getById(id);
        accountManaged.setBalance(accountManaged.getBalance() - amount);
        return accountRepository.save(accountManaged);
    }

    @Override
    public Account createAccount(User user) {
        String accountNumber = generateUniqueAccountNumber();
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(0.0);
        return accountRepository.save(account);
    }

    @Override
    public boolean isPinCreated(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new NotFoundException("Account non trovato");
        }

        return account.getPin()!=null;
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {

            accountNumber = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 6);
        } while (accountRepository.findByAccountNumber(accountNumber) != null);

        return accountNumber;
    }



    @Override
    public void createPIN(String accountNumber, String password, String pin) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new NotFoundException("Account non trovato");
        }

        if (!passwordEncoder.matches(password, account.getUser().getPassword())) {
            throw new UnauthorizedException("password sbagliata");
        }

        account.setPin(passwordEncoder.encode(pin));
        accountRepository.save(account);
    }

    @Override
    public void updatePIN(String accountNumber, String oldPIN, String password, String newPIN) {
        System.out.println(accountNumber+"  "+oldPIN+" "+newPIN+"  "+password);

        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new NotFoundException("account non trovato");
        }

        if (!passwordEncoder.matches(oldPIN, account.getPin())) {
            throw new UnauthorizedException("pin errato");
        }

        if (!passwordEncoder.matches(password, account.getUser().getPassword())) {
            throw new UnauthorizedException("password sbagliata");
        }

        account.setPin(passwordEncoder.encode(newPIN));
        accountRepository.save(account);
    }

    @Override
    public void cashDeposit(String accountNumber, String pin, double amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new NotFoundException("accounto non trovato");
        }

        if (!passwordEncoder.matches(pin, account.getPin())) {
            throw new UnauthorizedException("pin errato");
        }

        double currentBalance = account.getBalance();
        double newBalance = currentBalance + amount;
        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.CASH_DEPOSIT);;
        transaction.setTransaction_date(new Date());
        transaction.setSourceAccount(account);
        transactionRepository.save(transaction);
    }

    @Override
    public void cashWithdrawal(String accountNumber, String pin, double amount) {

        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new NotFoundException("account non trovato");
        }

        if (!passwordEncoder.matches(pin, account.getPin())) {
            throw new UnauthorizedException("pin errato");
        }

        double currentBalance = account.getBalance();
        if (currentBalance < amount) {
            throw new InsufficientBalanceException("saldo non sufficiente");
        }

        double newBalance = currentBalance - amount;
        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.CASH_WITHDRAWAL);
        transaction.setTransaction_date(new Date());
        transaction.setSourceAccount(account);
        transactionRepository.save(transaction);
    }

    @Override
    public void fundTransfer(String sourceAccountNumber, String targetAccountNumber, String pin, double amount) {
        Account sourceAccount = accountRepository.findByAccountNumber(sourceAccountNumber);
        if (sourceAccount == null) {
            throw new NotFoundException("account mittente non trovato");
        }

        Account targetAccount = accountRepository.findByAccountNumber(targetAccountNumber);
        if (targetAccount == null) {
            throw new NotFoundException("account destinatario non trovato");
        }

        if (!passwordEncoder.matches(pin, sourceAccount.getPin())) {
            throw new UnauthorizedException("pin errato");
        }

        double sourceBalance = sourceAccount.getBalance();
        if (sourceBalance < amount) {
            throw new InsufficientBalanceException("saldo non sufficiente");
        }

        double newSourceBalance = sourceBalance - amount;
        sourceAccount.setBalance(newSourceBalance);
        accountRepository.save(sourceAccount);

        double targetBalance = targetAccount.getBalance();
        double newTargetBalance = targetBalance + amount;
        targetAccount.setBalance(newTargetBalance);
        accountRepository.save(targetAccount);

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.CASH_TRANSFER);
        transaction.setTransaction_date(new Date());
        transaction.setSourceAccount(sourceAccount);
        transaction.setTargetAccount(targetAccount);
        transactionRepository.save(transaction);
    }
}