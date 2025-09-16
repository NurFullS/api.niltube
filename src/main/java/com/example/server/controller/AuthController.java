package com.example.server.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.web.ErrorResponse;x
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.server.dto.AuthDto;
import com.example.server.dto.AuthRequestDto;
import com.example.server.dto.AuthResponseDto;
import com.example.server.model.Auth;
import com.example.server.repository.AuthRepository;
import com.example.server.service.CloudinaryService;
import com.example.server.service.LoginAttemptService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Value("${JWT_KEY}")
    private String jwtSecret;
    private final long jwtExpirationMs = 7 * 24 * 60 * 60 * 1000;

    @Autowired
    public CloudinaryService cloudinaryService;

    public AuthController(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthDto> register(@RequestBody AuthRequestDto requestDto) {

        String hashedPassword = passwordEncoder.encode(requestDto.getPassword());

        Auth auth = new Auth();
        auth.setUsername(requestDto.getUsername());
        auth.setEmail(requestDto.getEmail());
        auth.setPassword(hashedPassword);

        auth.setAvatar(requestDto.getAvatar());
        System.out.println("Avatar URL: " + requestDto.getAvatar());

        Auth savedUser = authRepository.save(auth);

        AuthDto response = new AuthDto(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getAvatar());

        return ResponseEntity.ok(response);
    }

    @Autowired
    private LoginAttemptService loginAttemptService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDto requestDto,
            HttpServletResponse response) {
        String email = requestDto.getEmail();

        if (loginAttemptService.isBlocked(email)) {
            return ResponseEntity
                    .status(429)
                    .body(new com.example.server.dto.ErrorResponse("Слишком много попыток. Попробуйте через минуту."));
        }

        Auth auth = authRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!passwordEncoder.matches(requestDto.getPassword(), auth.getPassword())) {
            loginAttemptService.loginFailed(email);
            return ResponseEntity
                    .status(401)
                    .body(new com.example.server.dto.ErrorResponse("Неверный пароль"));
        }

        loginAttemptService.loginSucceeded(email);

        String token = Jwts.builder()
                .setSubject(auth.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .compact();

        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setMaxAge((int) (jwtExpirationMs / 1000));
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok(new AuthResponseDto(
                auth.getId(),
                auth.getUsername(),
                auth.getEmail(),
                auth.getAvatar()));
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<AuthDto> getUserByUsername(@PathVariable String username) {
        Auth auth = authRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        AuthDto responseDto = new AuthDto(
                auth.getId(),
                auth.getUsername(),
                auth.getEmail(),
                auth.getAvatar());

        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!authRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Пользователь не найден");
        }

        authRepository.deleteById(id);
        return ResponseEntity.ok("Пользователь успешно удалён!");
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok("Вы успешно вышли из аккаунта");
    }

    @DeleteMapping("/users/me")
    public ResponseEntity<?> deleteMyAccountFromDataBase(HttpServletRequest request) {
        String token = Arrays.stream(request.getCookies())
                .filter(c -> "token".equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new RuntimeException("Не авторизован"));

        String email = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

        Auth auth = authRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        try {
            if (auth.getAvatar() != null && !auth.getAvatar().isEmpty()) {
                cloudinaryService.deleteFile(auth.getAvatar());
            }

            authRepository.deleteById(auth.getId());
            return ResponseEntity.ok("Ваш аккаунт успешно удалён!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка при удалении аккаунта: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<AuthDto>> getAllUsers() {
        List<Auth> users = authRepository.findAll();

        List<AuthDto> dtoList = users.stream()
                .map(user -> new AuthDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getAvatar()))
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthDto> getCurrentUser(HttpServletRequest request) {
        String token = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            return ResponseEntity.ok(null);
        }

        try {
            String email = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

            Auth auth = authRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Пользовователь не найден"));

            AuthDto responseDto = new AuthDto(
                    auth.getId(),
                    auth.getUsername(),
                    auth.getEmail(),
                    auth.getAvatar());

            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            return ResponseEntity.ok(null);
        }
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<String> uploadAvatar(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        try {
            String token = Arrays.stream(request.getCookies())
                    .filter(c -> "token".equals(c.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElseThrow(() -> new RuntimeException("Не авторизован"));

            String email = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

            Auth auth = authRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            String imageUrl = cloudinaryService.uploadImage(file);

            auth.setAvatar(imageUrl);
            authRepository.save(auth);

            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Ошибка загрузки: " + e.getMessage());
        }
    }
}