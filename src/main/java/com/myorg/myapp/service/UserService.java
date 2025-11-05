package com.myorg.myapp.service;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.myorg.myapp.model.User;
import com.myorg.myapp.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class UserService {
  private final UserRepository repo;

  public UserService(UserRepository repo) {
    this.repo = repo;
  }

  @Transactional(readOnly = true)
  public List<User> list() {
    return repo.findAll();
  }

  @Transactional(readOnly = true)
  public User get(Long id) {
    return repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
  }

  public User create(User input) {
    if (repo.existsByEmail(input.getEmail())) {
      throw new ResponseStatusException(CONFLICT, "Email already exists");
    }
    input.setId(null);
    return repo.save(input);
  }

  public User update(Long id, User input) {
    User existing = get(id);
    if (!existing.getEmail().equalsIgnoreCase(input.getEmail())
        && repo.existsByEmail(input.getEmail())) {
      throw new ResponseStatusException(CONFLICT, "Email already exists");
    }
    existing.setName(input.getName());
    existing.setEmail(input.getEmail());
    existing.setRole(input.getRole());
    return repo.save(existing);
  }

  public void delete(Long id) {
    if (!repo.existsById(id)) {
      throw new ResponseStatusException(NOT_FOUND, "User not found");
    }
    repo.deleteById(id);
  }
}
