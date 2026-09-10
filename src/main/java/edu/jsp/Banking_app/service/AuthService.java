	package edu.jsp.Banking_app.service;
	
	import org.springframework.security.crypto.password.PasswordEncoder;
	import org.springframework.stereotype.Service;
	
	import edu.jsp.Banking_app.dto.LoginRequest;
	import edu.jsp.Banking_app.dto.LoginResponse;
	import edu.jsp.Banking_app.entity.User;
	import edu.jsp.Banking_app.repository.UserRepository;
	import edu.jsp.Banking_app.security.JwtService;
	import edu.jsp.Banking_app.exception.InvalidCredentialsException;
	
	@Service
	public class AuthService {
	
	    private final UserRepository userRepository;
	    private final PasswordEncoder passwordEncoder;
	    private final JwtService jwtService;
	
	    public AuthService(
	            UserRepository userRepository,
	            PasswordEncoder passwordEncoder,
	            JwtService jwtService) {
	
	        this.userRepository = userRepository;
	        this.passwordEncoder = passwordEncoder;
	        this.jwtService = jwtService;
	    }
	
	    public LoginResponse login(LoginRequest request) {
	
	        User user = userRepository
	                .findByEmail(request.getEmail())
	                .orElseThrow(() ->
	                        new InvalidCredentialsException(
	                                "Invalid email or password"
	                        ));
	
	        boolean passwordMatches =
	                passwordEncoder.matches(
	                        request.getPassword(),
	                        user.getPassword()
	                );
	
	        if (!passwordMatches) {
	
	        	throw new InvalidCredentialsException(
	        	        "Invalid email or password"
	        	);
	        }
	
	        String token =
	                jwtService.generateToken(user);
	
	        return new LoginResponse(
	                "Login successful",
	                token
	        );
	    }
	}