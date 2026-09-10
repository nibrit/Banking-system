package edu.jsp.Banking_app.dto;

import edu.jsp.Banking_app.entity.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {

    private long id;
    private String name;
    private String email;
    private Role role;
}