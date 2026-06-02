package com.team.multiversaltcg.modules.auth;

import com.team.multiversaltcg.game.model.RegraInvalidaException;
import com.team.multiversaltcg.game.service.PlayerProfileService;
import com.team.multiversaltcg.modules.user.User;
import com.team.multiversaltcg.modules.user.UserRepository;
import com.team.multiversaltcg.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {

    private static final String USERNAME_PATTERN = "^[a-z0-9_.-]{3,32}$";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PlayerProfileService playerProfileService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil,
                       PlayerProfileService playerProfileService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.playerProfileService = playerProfileService;
    }

    public AuthResponse register(AuthRequest request) {
        String username = normalizeUsername(request == null ? null : request.getUsername());
        String password = validatePassword(request == null ? null : request.getPassword());
        if (userRepository.existsByUsername(username)) {
            throw new RegraInvalidaException("Username ja cadastrado.");
        }

        userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build());
        playerProfileService.ensureProfile(username);

        return tokenResponse(username);
    }

    public AuthResponse login(AuthRequest request) {
        String username = normalizeUsername(request == null ? null : request.getUsername());
        String password = request == null ? null : request.getPassword();
        if (password == null || password.isBlank()) {
            throw new BadCredentialsException("Credenciais invalidas.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Credenciais invalidas.", ex);
        }

        playerProfileService.ensureProfile(username);
        return tokenResponse(username);
    }

    public AuthResponse me(String username) {
        return new AuthResponse(null, normalizeUsername(username));
    }

    private AuthResponse tokenResponse(String username) {
        return new AuthResponse(jwtUtil.gerarToken(username), username);
    }

    private String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new RegraInvalidaException("Username e obrigatorio.");
        }
        String normalized = username.trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches(USERNAME_PATTERN)) {
            throw new RegraInvalidaException("Username deve ter 3 a 32 caracteres e usar apenas letras, numeros, ponto, hifen ou underline.");
        }
        return normalized;
    }

    private String validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new RegraInvalidaException("Senha deve ter pelo menos 6 caracteres.");
        }
        return password;
    }
}
