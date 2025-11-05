package com.myorg.myapp.config;

import com.myorg.myapp.model.User;
import com.myorg.myapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DevDataLoader {

  @Bean
  CommandLineRunner seedUsers(UserRepository repo) {
    return args -> {
      if (repo.count() == 0) {
        User a = new User();
        a.setName("Alice Johnson");
        a.setEmail("alice@example.com");
        a.setRole("admin");

        User b = new User();
        b.setName("Bob Smith");
        b.setEmail("bob@example.com");
        b.setRole("user");

        User c = new User();
        c.setName("Carol Lee");
        c.setEmail("carol@example.com");
        c.setRole("manager");

        repo.save(a);
        repo.save(b);
        repo.save(c);
      }
    };
  }
}
