	package edu.jsp.Banking_app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import edu.jsp.Banking_app.entity.User;
import edu.jsp.Banking_app.service.UserService;
import jakarta.validation.Valid;

import edu.jsp.Banking_app.dto.RegisterRequest;
import edu.jsp.Banking_app.dto.UserResponse;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // CREATE
    @PostMapping("/post")
    public UserResponse createUser(
            @Valid @RequestBody RegisterRequest request) {

        return userService.createUser(request);
    }
    // FETCH ALL
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return new ResponseEntity<List<User>>(userService.getAllUsers(),HttpStatus.FOUND);
    }

    // FETCH BY ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return new ResponseEntity<User>(userService.getUserById(id),HttpStatus.FOUND);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody User user) {

        return new ResponseEntity<User>(userService.updateUser(id, user),HttpStatus.ACCEPTED);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);

        return "User deleted successfully";
    }
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {

        return ResponseEntity.ok(
                userService.getCurrentUser()
        );
    }
}