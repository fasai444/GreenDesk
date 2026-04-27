package org.example.controllers.auth;

import org.example.dto.auth.CreateUserRequest;
import org.example.dto.auth.ResetPasswordRequest;
import org.example.dto.auth.UpdateUserRequest;
import org.example.dto.auth.UserResponseDto;
import org.example.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // ---------- Stats ----------

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(Map.of(
                "total", userService.countAll(),
                "admins", userService.countByRole("ADMIN"),
                "users", userService.countByRole("USER"),
                "active", userService.countActive()
        ));
    }

    // ---------- CRUD utilisateurs ----------

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers()
                .stream()
                .map(UserResponseDto::new)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(u -> ResponseEntity.ok((Object) new UserResponseDto(u)))
                .orElse(ResponseEntity.status(404).body(Map.of("error", "Utilisateur introuvable")));
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest req) {
        if (userService.existsByUsername(req.getUsername())) {
            return ResponseEntity.status(409).body(Map.of("error", "Ce nom d'utilisateur est déjà pris"));
        }
        if (userService.existsByEmail(req.getEmail())) {
            return ResponseEntity.status(409).body(Map.of("error", "Cette adresse email est déjà utilisée"));
        }
        String role = (req.getRole() != null && !req.getRole().isBlank()) ? req.getRole() : "USER";
        var user = userService.createUser(req.getUsername(), req.getEmail(), req.getPassword(), role);
        return ResponseEntity.status(201).body(new UserResponseDto(user));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody UpdateUserRequest req) {
        try {
            var user = userService.updateUser(id, req.getUsername(), req.getEmail(), req.getRole(), req.isActive());
            return ResponseEntity.ok(new UserResponseDto(user));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable String id, @RequestBody ResetPasswordRequest req) {
        try {
            userService.resetPassword(id, req.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "Utilisateur supprimé avec succès"));
    }
}
