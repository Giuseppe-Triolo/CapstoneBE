package service;

import DTO.AccountResponse;
import DTO.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import entities.Account;
import entities.User;
import exception.NotFoundException;
import repositories.AccountRepository;
import repositories.UserRepository;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public UserResponse getUserDetails(String accountNumber) {
        User user = userRepository.findByAccountAccountNumber(accountNumber);
        // Controlliamo se l'user esiste ed è associato al numero d'account fornito
        if (user == null) {
            throw new NotFoundException("Utente non trovato tramite l'identificativo fornito'.");
        }

        // Mappiamo l'entità User all'interno dello UserResponse DTO
        UserResponse userResponse = new UserResponse();
        userResponse.setName(user.getName());
        userResponse.setEmail(user.getEmail());
        userResponse.setAddress(user.getAddress());
        userResponse.setPhone_number(user.getPhone_number());
        userResponse.setAccountNumber(user.getAccount().getAccountNumber());

        return userResponse;
    }

    @Override
    public AccountResponse getAccountDetails(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        // Controlliamo se l'account esiste con il numero d'account fornito
        if (account == null) {
            throw new NotFoundException("Account non trovato tramite l'identificativo fornito.");
        }

        // Mappiamo l'entità Account nel DTO AccountResponse
        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setAccountNumber(account.getAccountNumber());
        accountResponse.setAccountType(account.getAccount_type());
        accountResponse.setBalance(account.getBalance());
        accountResponse.setBranch(account.getBranch());
        accountResponse.setIFSCCode(account.getIFSC_code());

        return accountResponse;
    }
}