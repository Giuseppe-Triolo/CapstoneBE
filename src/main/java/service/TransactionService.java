package service;

import java.util.List;

import DTO.TransactionDTO;

public interface TransactionService {

    List<TransactionDTO> getAllTransactionsByAccountNumber(String accountNumber);

}
