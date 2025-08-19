package com.Alura.Foro_HUB.auth;

import com.Alura.Foro_HUB.security.JwtService;
import com.Alura.Foro_HUB.user.UserRepository;
import com.Alura.Foro_HUB.user.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    public AuthController(UserRepository userRepo, BCryptPasswordEncoder encoder,
                          AuthenticationManager authManager, JwtService jwt) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest req) {
        if (userRepo.existsByUsername(req.username())) return "El usuario ya existe.";
        userRepo.save(new User(req.username(), encoder.encode(req.password()), "ROLE_USER"));
        return "Usuario registrado.";
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        var principal = (User) auth.getPrincipal();
        String token = jwt.generate(principal.getUsername(), Map.of("role", principal.getRole()));
        return new TokenResponse("Bearer", token);
    }

    public record RegisterRequest(@NotBlank String username, @NotBlank String password) {}
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record TokenResponse(String type, String token) {}
}
