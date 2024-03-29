package service;

import entities.Account;
import entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import repositories.AccountRepository;


public interface AccountService {
public Account getAccount(Long id);
public Account deposit (Long id, Double amount);
    public Account withdraw (Long id, Double amount);
    public Account createAccount(User user);
    public boolean isPinCreated(String accountNumber) ;
    public void createPIN(String accountNumber, String password, String pin) ;
    public void updatePIN(String accountNumber, String oldPIN, String password, String newPIN);
    public void cashDeposit(String accountNumber, String pin, double amount);
    public void cashWithdrawal(String accountNumber, String pin, double amount);
    public void fundTransfer(String sourceAccountNumber, String targetAccountNumber, String pin, double amount);



}