package service;



import DTO.AccountResponse;
import DTO.UserResponse;


public interface DashboardService {
    UserResponse getUserDetails(String accountNumber);
    AccountResponse getAccountDetails(String accountNumber);
}