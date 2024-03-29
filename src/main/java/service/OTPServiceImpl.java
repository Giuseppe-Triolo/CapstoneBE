package service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import entities.OtpInfo;
import entities.User;
import exception.AccountDoesNotExist;
import exception.InvalidOTPException;
import exception.OtpRetryLimitExceededException;
import repositories.otpInfoRepository;


@Service
public class OTPServiceImpl implements OTPService {

    private static final int MAX_OTP_ATTEMPTS = 3;
    private static final int MAX_OTP_ATTEMPTS_WINDOW_MINUTES = 15;
    private static final int OTP_EXPIRY_MINUTES = 5;



    @Autowired
    private otpInfoRepository otpInfoRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CacheManager cacheManager;

    @Override
    public String generateOTP(String accountNumber) {
        User user = userService.getUserByAccountNumber(accountNumber);
        if (user == null) {
            throw new AccountDoesNotExist("Numero d'account errato");
        }

        OtpInfo existingOtpInfo = otpInfoRepository.findByAccountNumber(accountNumber);

        if (existingOtpInfo != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastRequestTime = existingOtpInfo.getGeneratedAt();

            if (getOtpAttempts(accountNumber) >= MAX_OTP_ATTEMPTS) {
                if (lastRequestTime.isAfter(now.minusMinutes(MAX_OTP_ATTEMPTS_WINDOW_MINUTES))) {
                    // Se il numero di richieste OTP è superato all'interno dell'intervallo di tempo garantito, ritorniamo l'errore
                    throw new OtpRetryLimitExceededException(
                            "OTP generation limit exceeded. Please try again after some time.");

                } else {
                    // Altrimenti, il resettiamo il conto e l'utente può ottenere un nuovo OTP

                    resetOtpAttempts(accountNumber);
                }
            }

        }

        String otp = null;
        if (existingOtpInfo != null) {
            if (isOtpExpired(existingOtpInfo.getGeneratedAt())) {
                otpInfoRepository.delete(existingOtpInfo);
                otp = generateNewOTP(accountNumber);
            } else {
                //OTP valido, ritorniamo lo stesso OTP ma resettiamo il tempo
                existingOtpInfo.setGeneratedAt(LocalDateTime.now());
                otp = existingOtpInfo.getOtp();
            }
        } else {
            otp = generateNewOTP(accountNumber);
        }

        // Incrementiamo il conto di richiesta OTP per l'utente
        incrementOtpAttempts(accountNumber);

        return otp;
    }



    private void incrementOtpAttempts(String accountNumber) {
        Cache cache = cacheManager.getCache("otpAttempts");
        Integer attempts = cache.get(accountNumber, Integer.class);
        if (attempts == null) {
            attempts = 1;
        } else {
            attempts++;
        }
        cache.put(accountNumber, attempts);
    }

    private void resetOtpAttempts(String accountNumber) {
        Cache cache = cacheManager.getCache("otpAttempts");
        cache.evict(accountNumber);
    }

    private int getOtpAttempts(String accountNumber) {
        Cache cache = cacheManager.getCache("otpAttempts");
        Integer attempts = cache.get(accountNumber, Integer.class);
        return attempts != null ? attempts : 0;
    }

    private String generateNewOTP(String accountNumber) {
        Random random = new Random();
        int otpValue = 100_000 + random.nextInt(900_000);
        String otp = String.valueOf(otpValue);

        // Salviamo la nuova informazione dell'OTP nel DB
        OtpInfo otpInfo = new OtpInfo();
        otpInfo.setAccountNumber(accountNumber);
        otpInfo.setOtp(otp);
        otpInfo.setGeneratedAt(LocalDateTime.now());
        otpInfoRepository.save(otpInfo);
        return otp;
    }



    @Override
    public boolean validateOTP(String accountNumber, String otp) {
        OtpInfo otpInfo = otpInfoRepository.findByAccountNumberAndOtp(accountNumber, otp);

        if (otpInfo != null) {
            // Controlliamo se l'OTP non è scaduto (5 minuti)

            if (isOtpExpired(otpInfo.getGeneratedAt())) {
                // OTP scaduto, quindi procediamo al cancellarlo dal DB
                otpInfoRepository.delete(otpInfo);
                return false;
            } else {
                // OTP Valido, quindi lo cancelliamo dal DB
                otpInfoRepository.delete(otpInfo);
                return true;
            }
        } else {
            // Gestione della casualità in cui l'OTP è o invalido o non trovato
            throw new InvalidOTPException("Invalid OTP");
        }
    }

    private boolean isOtpExpired(LocalDateTime otpGeneratedAt) {
        LocalDateTime now = LocalDateTime.now();
        return otpGeneratedAt.isBefore(now.minusMinutes(OTP_EXPIRY_MINUTES));
    }
}