package org.example.controllers.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.example.dto.auth.CreateUserRequest;
import org.example.dto.auth.LoginRequest;
import org.example.dto.auth.UserResponseDto;
import org.example.entities.user.User;
import org.example.repositories.UserRepository;
import org.example.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );

            SecurityContext sc = SecurityContextHolder.getContext();
            sc.setAuthentication(authentication);

            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

            userService.updateLastLogin(req.getUsername());

            User user = userRepository.findByUsername(req.getUsername()).orElseThrow();
            return ResponseEntity.ok(new UserResponseDto(user));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Nom d'utilisateur ou mot de passe incorrect"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Erreur d'authentification"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody CreateUserRequest req) {
        if (req.getUsername() == null || req.getUsername().isBlank()
                || req.getEmail() == null || req.getEmail().isBlank()
                || req.getPassword() == null || req.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tous les champs sont obligatoires"));
        }
        if (req.getPassword().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le mot de passe doit contenir au moins 6 caractères"));
        }
        if (userService.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ce nom d'utilisateur est déjà pris"));
        }
        if (userService.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cette adresse email est déjà utilisée"));
        }
        User user = userService.createUser(req.getUsername(), req.getEmail(), req.getPassword(), "USER");
        return ResponseEntity.ok(new UserResponseDto(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Déconnecté avec succès"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .map(user -> ResponseEntity.ok((Object) new UserResponseDto(user)))
                .orElse(ResponseEntity.status(404).body(Map.of("error", "Utilisateur introuvable")));
    }
}
