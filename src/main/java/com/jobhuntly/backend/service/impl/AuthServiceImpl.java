package com.jobhuntly.backend.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.jobhuntly.backend.dto.auth.request.GoogleLoginRequest;
import com.jobhuntly.backend.dto.auth.request.LoginRequest;
import com.jobhuntly.backend.dto.auth.request.RegisterRequest;
import com.jobhuntly.backend.dto.auth.response.LoginResponse;
import com.jobhuntly.backend.dto.auth.response.MeResponse;
import com.jobhuntly.backend.dto.auth.response.RegisterResponse;
import com.jobhuntly.backend.entity.Role;
import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.entity.enums.Status;
import com.jobhuntly.backend.repository.RoleRepository;
import com.jobhuntly.backend.repository.UserRepository;
import com.jobhuntly.backend.security.jwt.JwtUtil;
import com.jobhuntly.backend.service.AuthService;
import com.jobhuntly.backend.service.email.EmailSender;
import com.jobhuntly.backend.service.email.EmailValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final EmailValidator emailValidator;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authManager;
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;
    @Value("${google.client-id}")
    private String GOOGLE_CLIENT_ID;
    @Value("${backend.host}")
    private String BACKEND_HOST;
    @Value("${backend.prefix}")
    private String BACKEND_PREFIX;


    @Override
    public ResponseEntity<RegisterResponse> register(RegisterRequest request) {
        if (!emailValidator.test(request.getEmail())) {
            throw new IllegalStateException("Email không hợp lệ");
        }

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new IllegalStateException("Email đã được sử dụng");
        }
        Role role = roleRepository.findByRoleName(request.getRole().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        String token = UUID.randomUUID().toString();

        User user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(role)
                .isActive(false)
                .activationToken(token)
                .build();

        user.setStatus(Status.INACTIVE);

        userRepository.save(user);

        // String activationLink = BACKEND_HOST + BACKEND_PREFIX + "/auth/activate?token=" + token;
        String activationLink = "http://18.142.226.139:8080" + BACKEND_PREFIX + "/auth/activate?token=" + token;

        String htmlContent = String.format("""
                <html>
                  <body style="font-family: Arial, sans-serif;">
                    <h2 style="color:#0a66c2;">Chào mừng bạn đến với JobHuntly!</h2>
                    <p>Nhấn vào nút bên dưới để kích hoạt tài khoản:</p>
                    <a href="%s"
                       style="display:inline-block;padding:10px 20px;background-color:#0a66c2;color:white;text-decoration:none;border-radius:5px;">
                       Kích hoạt
                    </a>
                  </body>
                </html>
                """, activationLink);
        emailSender.send(
                request.getEmail(),
                "Kích hoạt tài khoản của bạn",
                htmlContent
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse("success", "Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt."));
    }

    @Override
    public ResponseEntity<RegisterResponse> activateAccount(String token) {
        User user = userRepository.findByActivationToken(token)
                .orElseThrow(() -> new IllegalStateException("Token không hợp lệ"));

        user.setIsActive(true);
        user.setStatus(Status.ACTIVE);
        user.setActivationToken(null);
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new RegisterResponse("success", "Tài khoản đã được kích hoạt thành công!"));
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // lấy user để build payload
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String actualRole = user.getRole().getRoleName().toUpperCase();
        String requestedRole = Optional.ofNullable(request.getRole())
                .map(String::toUpperCase)
                .orElse(null);

        if (requestedRole != null && !requestedRole.equals(actualRole)) {
            throw new org.springframework.security.authentication.BadCredentialsException("Role không phù hợp");
        }

        String token = jwtUtil.generateToken(user.getEmail(), actualRole, user.getId());

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn( /* ví dụ 30 ngày (giây) */ 30L * 24 * 60 * 60)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(actualRole)
                .build();
    }

    @Override
    public LoginResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleIdToken idToken = verifyGoogleIdToken(request.getIdToken(), GOOGLE_CLIENT_ID);
        if (idToken == null) {
            throw new RuntimeException("Invalid Google ID token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String googleUserId = payload.getSubject();
        String email = payload.getEmail();
        boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
        String fullName = (String) payload.get("name");

        if (email == null || !emailVerified) {
            throw new RuntimeException("Google email is missing or not verified");
        }

        // find-or-create
        Optional<User> byGoogle = userRepository.findByGoogleId(googleUserId);
        User user = byGoogle.orElseGet(() -> userRepository.findByEmail(email).orElse(null));

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setFullName(fullName);
            user.setGoogleId(googleUserId);
            user.setStatus(Status.ACTIVE);
            user.setIsActive(true);
            user.setActivationToken(null);
            user.setPasswordHash(null);

            Role role = roleRepository.findByRoleName("CANDIDATE")
                    .orElseThrow(() -> new RuntimeException("Default role not found"));
            user.setRole(role);

            user = userRepository.save(user);
        } else {
            if (user.getGoogleId() == null) user.setGoogleId(googleUserId);
            if (user.getStatus() != Status.ACTIVE) user.setStatus(Status.ACTIVE);
            user.setIsActive(true);
            userRepository.save(user);
        }

        String roleName = user.getRole() != null ? user.getRole().getRoleName().toUpperCase() : "CANDIDATE";
        String token = jwtUtil.generateToken(user.getEmail(), roleName, user.getId());

        String tokenType = "Bearer";
        long expiresIn = jwtUtil.getExpirationSeconds();
        Long userId = (user.getId() == null) ? null : user.getId();

        return new LoginResponse(
                token,
                tokenType,
                expiresIn,
                userId,
                user.getEmail(),
                user.getFullName(),
                roleName
        );
    }

    @Override
    public MeResponse getUserMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        return new MeResponse(
                user.getId(), user.getEmail(), user.getFullName(),
                user.getRole().getRoleName().toUpperCase()
        );
    }


    private GoogleIdToken verifyGoogleIdToken(String idTokenString, String clientId) {
        try {
            HttpTransport transport = Utils.getDefaultTransport();
            JsonFactory jsonFactory = Utils.getDefaultJsonFactory();

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            return verifier.verify(idTokenString);
        } catch (Exception ex) {
            return null;
        }
    }
}
