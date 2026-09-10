package edu.jsp.Banking_app.entity;

import java.util.List;
import java.util.Set;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="user_table")
@Getter
@Setter
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="user_id")
private long id;
	@Length(min=3,max=25,message="enter name within range")
private String name;
	@Email(message="Enter valid email")
private String email;
	
	@JsonIgnore
private String password;
@Enumerated(EnumType.STRING)
@JsonIgnore
@Column(nullable = false)
private Role role = Role.USER;
	


	@OneToMany(cascade = CascadeType.ALL, mappedBy="user")
	private Set<Loan> loans;
	
	public void addloan(Loan l) {
		loans.add(l);
		l.setUser(this);
	}
	public void deleteLoan(Loan l) {
		loans.remove(l);
		l.setUser(null);
	}
	@OneToMany(cascade = CascadeType.ALL,mappedBy = "user")
	private Set<Account> account;
	
	public void addAccount(Account a) {
		account.add(a);
		a.setUser(this);
	}
	
	public void deleteAccount(Account a) {
		account.remove(a);
		a.setUser(null);
	}
}
