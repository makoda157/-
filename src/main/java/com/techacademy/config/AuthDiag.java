package com.techacademy.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.techacademy.entity.Employee;
import com.techacademy.repository.EmployeeRepository;

@Configuration
public class AuthDiag {

  @Bean
  CommandLineRunner checkPasswords(EmployeeRepository repo, PasswordEncoder encoder) {
    return args -> {
      repo.findById("1").ifPresent(e -> print("1/kirataro", "kirataro", e, encoder));
      repo.findById("2").ifPresent(e -> print("2/tanataro", "tanataro", e, encoder));
    };
  }

  private void print(String label, String raw, Employee e, PasswordEncoder encoder) {
    System.out.println("=== DIAG " + label + " ===");
    System.out.println("exists: true, role=" + e.getRole() + ", deleteFlg=" + e.isDeleteFlg());
    System.out.println("hash: " + e.getPassword());
    System.out.println("matches: " + encoder.matches(raw, e.getPassword()));
  }
}
