package edu.jsp.Banking_app;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        String hash =
                encoder.encode("Arun@2026");

        System.out.println(hash);
    }
}