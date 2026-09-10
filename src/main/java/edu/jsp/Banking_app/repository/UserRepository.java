package edu.jsp.Banking_app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.jsp.Banking_app.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}