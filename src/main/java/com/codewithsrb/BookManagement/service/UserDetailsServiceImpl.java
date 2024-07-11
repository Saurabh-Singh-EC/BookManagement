package com.codewithsrb.BookManagement.service;

import com.codewithsrb.BookManagement.exception.ApiException;
import com.codewithsrb.BookManagement.model.UserDetailsImpl;
import com.codewithsrb.BookManagement.model.UserInfo;
import com.codewithsrb.BookManagement.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * This class implements UserDetailsService in order to provide the implementation for the UserDetails
 * from the database.
 */
@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserInfo userInfo = findUserByEmail(email);

        if (userInfo == null) {
            throw new UsernameNotFoundException("User not found in the database");
        } else {
            return new UserDetailsImpl(userInfo);
        }
    }

    public UserInfo findUserByEmail(String email) {
        try {
            return userRepository.findByEmail(email);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ApiException("An error occurred. Please try again");
        }
    }

    public UserInfo registerNewUser(UserInfo userInfo) {
        if (isEmailAlreadyExists(userInfo.getEmail()))
            throw new ApiException("Email already used. Please use new email and try again.");
        userInfo.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        userInfo.setRole("ROLE_USER");
        try {
            return userRepository.save(userInfo);
        } catch (Exception e) {
            log.error("error: " + e.getMessage());
            throw new ApiException("An error occurred while account creation. Please try again.");
        }
    }

    private boolean isEmailAlreadyExists(String email) {
        UserInfo userInfo = userRepository.findByEmail(email);
        return Objects.nonNull(userInfo);
    }
}
