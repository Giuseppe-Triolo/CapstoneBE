package service;



import java.util.concurrent.CompletableFuture;

public interface OTPService {

    String generateOTP(String accountNumber);

    public boolean validateOTP(String accountNumber, String otp);

}