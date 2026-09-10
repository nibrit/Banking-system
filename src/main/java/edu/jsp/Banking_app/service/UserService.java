package edu.jsp.Banking_app.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.jsp.Banking_app.dto.RegisterRequest;
import edu.jsp.Banking_app.entity.Role;
import edu.jsp.Banking_app.entity.User;
import edu.jsp.Banking_app.repository.UserRepository;
import edu.jsp.Banking_app.dto.UserResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import edu.jsp.Banking_app.entity.User;
import edu.jsp.Banking_app.exception.NotFoundException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // CREATE
    public UserResponse createUser(RegisterRequest request) {

    	  User user = new User();

    	    user.setName(request.getName());
    	    user.setEmail(request.getEmail());

    	    user.setPassword(
    	            passwordEncoder.encode(request.getPassword())
    	    );

    	    user.setRole(Role.USER);

    	    User savedUser = userRepository.save(user);

    	    UserResponse response = new UserResponse();

    	    response.setId(savedUser.getId());
    	    response.setName(savedUser.getName());
    	    response.setEmail(savedUser.getEmail());
    	    response.setRole(savedUser.getRole());

    	    return response;
    	}
    public User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new NotFoundException(
                    "Authenticated user not found",
                    "user",
                    "current"
            );
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException(
                                "User not found",
                                "email",
                                email
                        ));
    }

    // FETCH ALL
    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // FETCH BY ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    // UPDATE
    public User updateUser(Long id, User user) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());

        return userRepository.save(existingUser);
    }

    // DELETE
    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }

        userRepository.deleteById(id);
    }
}