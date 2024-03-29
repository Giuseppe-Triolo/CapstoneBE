package Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import DTO.OtpVerificationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import DTO.LoginRequest;
import DTO.OtpRequest;

import DTO.UserResponse;
import entities.User;
import Security.JwtTokenUtil;
import service.OTPService;
import service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final OTPService otpService;

    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil,
                          UserDetailsService userDetailsService, OTPService otpService) {
        this.userService =  userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.otpService = otpService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody User user) {
        User registeredUser = userService.registerUser(user);

        UserResponse userResponse = new UserResponse();
        userResponse.setName(registeredUser.getName());
        userResponse.setEmail(registeredUser.getEmail());
        userResponse.setAccountNumber(registeredUser.getAccount().getAccountNumber());
        userResponse.setIFSC_code(registeredUser.getAccount().getIFSC_code());
        userResponse.setBranch(registeredUser.getAccount().getBranch());
        userResponse.setAccount_type(registeredUser.getAccount().getAccount_type());
userService.temporaneo(); //test funzionalità DB

        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Autentica l'utente attraverso il numero d'account e la password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getAccountNumber(), loginRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            // Nel caso di credenziali errate, ritorna un 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid account number or password");
        }

        // Se, invece ,l'autenticazione va a buon fine, genera il JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getAccountNumber());
        System.out.println(userDetails);
        String token = jwtTokenUtil.generateToken(userDetails);
        Map<String, String> result =  new HashMap<>();
        result.put("token", token);
        // Qui invece viene tornato il jwt token nella response
        return new ResponseEntity<>(result , HttpStatus.OK);
    }


    @PostMapping("/generate-otp")
    public ResponseEntity<?> generateOtp(@RequestBody OtpRequest otpRequest) {

        String accountNumber = otpRequest.getAccountNumber();

        // Trova l'utente attraverso il numero d'account per trovare l'email associata
        User user = userService.getUserByAccountNumber(accountNumber);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found for the given account number");
        }

        // Generiamo l'OTP salvandolo sul DB
        String otp = otpService.generateOTP(accountNumber);



            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"message\": \"Failed to send OTP\"}");
        }



    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtpAndLogin(@RequestBody OtpVerificationRequest otpVerificationRequest) {
        String accountNumber = otpVerificationRequest.getAccountNumber();
        String otp = otpVerificationRequest.getOtp();

        System.out.println(accountNumber+"  "+otp);

        // Validiamo l'OTP contro quello salvato precedentemente nel DB
        boolean isValidOtp = otpService.validateOTP(accountNumber, otp);
        System.out.println(isValidOtp);

        if (isValidOtp) {
            // Se l'OTP è valido generiamo il JWT token ed effettuiamo il login dell'utente
            UserDetails userDetails = userDetailsService.loadUserByUsername(accountNumber);
            String token = jwtTokenUtil.generateToken(userDetails);
            Map<String, String> result = new HashMap<>();
            result.put("token", token);
            // E lo ritorniamo nella response
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            // Nel caso contrario, quindi OTP invalido, ritorna l'errore 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\": \"Invalid OTP\"}");
        }
    }

    @PostMapping("/update")
    public ResponseEntity<UserResponse> updateUser(@RequestBody User user) {
        User updateUser = userService.updateUser(user);

        UserResponse userResponse = new UserResponse();
        userResponse.setName(updateUser.getName());
        userResponse.setEmail(updateUser.getEmail());
        userResponse.setAccountNumber(updateUser.getAccount().getAccountNumber());
        userResponse.setIFSC_code(updateUser.getAccount().getIFSC_code());
        userResponse.setBranch(updateUser.getAccount().getBranch());
        userResponse.setAccount_type(updateUser.getAccount().getAccount_type());


        return ResponseEntity.ok(userResponse);
    }

}