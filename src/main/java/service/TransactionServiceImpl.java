package service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import DTO.TransactionDTO;
import entities.Transaction;
import mapper.TransactionMapper;
import repositories.TransactionRepository;

import repositories.TransactionRepository;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionMapper transactionMapper;

    @Override
    public List<TransactionDTO> getAllTransactionsByAccountNumber(String accountNumber) {
        List<Transaction> transactions = transactionRepository.findBySourceAccount_AccountNumberOrTargetAccount_AccountNumber(accountNumber, accountNumber);

        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(transactionMapper::toDto)
                .sorted((t1, t2) -> t2.getTransaction_date().compareTo(t1.getTransaction_date()))
                .collect(Collectors.toList());

        return transactionDTOs;
    }


}