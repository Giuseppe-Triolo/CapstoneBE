package service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import entities.User;
import repositories.UserRepository;

@Service
public class JWTUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String accountNumber) throws UsernameNotFoundException {
        User user = userRepository.findByAccountAccountNumber(accountNumber);
        if (user == null) {
            throw new UsernameNotFoundException("numero account non valido ");
        }

        // Ritorniamo un oggetto UserDetails che wrappa l'entità User
        return new org.springframework.security.core.userdetails.User(
                user.getAccount().getAccountNumber(),  // Utilizziamo il numero d'account come Username
                user.getPassword(),
                Collections.emptyList()
        );
    }
}
