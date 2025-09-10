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
import com.jobhuntly.backend.entity.CandidateProfile;
import com.jobhuntly.backend.entity.Role;
import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.entity.enums.PasswordTokenPurpose;
import com.jobhuntly.backend.entity.enums.Status;
import com.jobhuntly.backend.repository.CandidateProfileRepository;
import com.jobhuntly.backend.repository.RoleRepository;
import com.jobhuntly.backend.repository.UserRepository;
import com.jobhuntly.backend.security.jwt.JwtUtil;
import com.jobhuntly.backend.service.AuthService;
import com.jobhuntly.backend.service.PasswordTokenService;
import com.jobhuntly.backend.service.email.EmailSender;
import com.jobhuntly.backend.service.email.EmailValidator;
import com.jobhuntly.backend.service.email.MailTemplateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static com.jobhuntly.backend.util.TokenUtil.newUrlSafeToken;
import static com.jobhuntly.backend.util.TokenUtil.sha256Hex;

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
    private final CandidateProfileRepository candidateProfileRepo;
    private final AuthCookieServiceImpl authCookieServiceImpl;
    private final SpringTemplateEngine templateEngine;
    private final PasswordTokenService passwordTokenService;
    private final MailTemplateService mailTemplateService;
    @Value("${google.client-id}")
    private String GOOGLE_CLIENT_ID;
    @Value("${backend.host}")
    private String BACKEND_HOST;
    @Value("${backend.prefix}")
    private String BACKEND_PREFIX;
    @Value("${frontend.host}")
    private String FRONTEND_HOST;

    @Value("${activation.ttl}")
    private Duration activationTtl;

    @Value("${activation.resend-cooldown}")
    private Duration resendCooldown;


    @Override
    @Transactional
    public ResponseEntity<RegisterResponse> register(RegisterRequest request) {
        if (!emailValidator.test(request.getEmail())) {
            throw new IllegalStateException("Invalid email address.");
        }

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new IllegalStateException("Email is already in use. Please use a different one.");
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

        issueAndEmailActivationToken(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse("success", "Registered. Please check your email to activate your account."));
    }

    @Override
    public ResponseEntity<RegisterResponse> activateAccount(String tokenRaw) {
        if (tokenRaw == null || tokenRaw.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing token");
        }

        String hash = sha256Hex(tokenRaw);

        User user = userRepository.findByActivationToken(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token invalid or expired"));

        Instant now = Instant.now();
        Instant exp = user.getActivationTokenExpiresAt();
        if (exp == null || !exp.isAfter(now)) {
            user.setActivationToken(null);
            user.setActivationTokenExpiresAt(null);
            userRepository.save(user);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token invalid or expired");
        }

        user.setActivationToken(null);
        user.setActivationTokenExpiresAt(null);

        user.setStatus(Status.ACTIVE);
        user.setIsActive(true);

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new RegisterResponse("success", "Account activated successfully."));
    }

    @Override
    @Transactional
    public ResponseEntity<Void> resendActivation(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (Boolean.TRUE.equals(user.getIsActive())) return;

            if (!resendCooldown.isZero() && user.getActivationTokenExpiresAt() != null) {
                Instant lastIssuedAt = user.getActivationTokenExpiresAt().minus(activationTtl);

                if (Instant.now().isBefore(lastIssuedAt.plus(resendCooldown))) {
                    return;
                }
            }

            issueAndEmailActivationToken(user);
        });

        return ResponseEntity.ok().build();
    }


    @Override
    public LoginResponse login(LoginRequest request, HttpServletRequest req, HttpServletResponse res) {
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getGoogleId() != null && (user.getPasswordHash() == null || user.getPasswordHash().isBlank())) {
            throw new org.springframework.security.authentication.BadCredentialsException(
                    "This account uses Google Sign-In. Please sign in with Google or set a password first."
            );
        }

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );


        String actualRole = user.getRole().getRoleName().toUpperCase();
        String requestedRole = Optional.ofNullable(request.getRole())
                .map(String::toUpperCase)
                .orElse(null);

        if (requestedRole != null && !requestedRole.equals(actualRole)) {
            throw new org.springframework.security.authentication.BadCredentialsException("Role không phù hợp");
        }

        String avatar = candidateProfileRepo.findByUser_Id(user.getId())
                .map(CandidateProfile::getAvatar)
                .orElse(null);

        long accessTtlSeconds = Duration.ofDays(30).toSeconds();
        String token = jwtUtil.generateToken(user.getEmail(), actualRole.toUpperCase(), user.getId());

        authCookieServiceImpl.setAuthCookie(req, res, token, accessTtlSeconds);

        return LoginResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(actualRole.toUpperCase())
                .avatarUrl(avatar)
                .build();
    }

    @Override
    public LoginResponse loginWithGoogle(GoogleLoginRequest request,
                                         HttpServletRequest req,
                                         HttpServletResponse res) {
        GoogleIdToken idToken = verifyGoogleIdToken(request.getIdToken(), GOOGLE_CLIENT_ID);
        if (idToken == null) {
            throw new RuntimeException("Invalid Google ID token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String googleUserId = payload.getSubject();
        String email = payload.getEmail();
        boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
        String fullName = (String) payload.get("name");
        String avatarUrl = (String) payload.get("picture");


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

            CandidateProfile profile = new CandidateProfile();
            profile.setUser(user);
            profile.setAvatar(avatarUrl);
            candidateProfileRepo.save(profile);
        } else {
            if (user.getGoogleId() == null) user.setGoogleId(googleUserId);
            if (user.getStatus() != Status.ACTIVE) user.setStatus(Status.ACTIVE);
            user.setIsActive(true);
            userRepository.save(user);
        }

        String roleName = user.getRole() != null ? user.getRole().getRoleName().toUpperCase() : "CANDIDATE";

        long accessTtlSeconds = jwtUtil.getExpirationSeconds() > 0
                ? jwtUtil.getExpirationSeconds()
                : java.time.Duration.ofDays(30).toSeconds();

        String token = jwtUtil.generateToken(user.getEmail(), roleName, user.getId());

        authCookieServiceImpl.setAuthCookie(req, res, token, accessTtlSeconds);

        String avatar = candidateProfileRepo.findByUser_Id(user.getId())
                .map(CandidateProfile::getAvatar)
                .orElse(null);

        return new LoginResponse(
                user.getEmail(),
                user.getFullName(),
                roleName,
                avatar

        );
    }

    @Override
    public MeResponse getUserMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        String avatar = candidateProfileRepo.findByUser_Id(user.getId())
                .map(CandidateProfile::getAvatar)
                .orElse(null);

        return new MeResponse(
                user.getEmail(), user.getFullName(),
                user.getRole().getRoleName().toUpperCase(), avatar
        );
    }

    @Override
    public void sendSetPasswordLink(String email) {
        User u = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.OK)); // tránh lộ email

        // Chỉ cho phép nếu là “Google account chưa có pass”
        if (u.getGoogleId() == null || (u.getPasswordHash() != null && !u.getPasswordHash().isBlank())) {
            // Không phải case đặt lần đầu
            return; // trả 204/200 để tránh lộ logic
        }

        String raw = passwordTokenService.issuePasswordToken(u, PasswordTokenPurpose.SET, activationTtl);

        String link = FRONTEND_HOST + "/auth" + "/set-password?token=" + raw;
        String html = mailTemplateService.renderSetPasswordEmail(link, ttlText(activationTtl));
        emailSender.send(u.getEmail(), "[JobHuntly] Set your password", html);
    }

    @Override
    @Transactional
    public void setPassword(String token, String newPassword) {
        User u = passwordTokenService.verifyPasswordTokenOrThrow(token, PasswordTokenPurpose.SET);

        // An toàn: đảm bảo là tài khoản Google
        if (u.getGoogleId() == null) {
            passwordTokenService.clearPasswordToken(u);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a Google account");
        }

        u.setPasswordHash(passwordEncoder.encode(newPassword));
        u.setPasswordSet(true); // bạn đã có cột này
        userRepo.save(u);

        passwordTokenService.clearPasswordToken(u);
    }

    @Override
    public void sendResetPasswordLink(String email) {
        User u = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.OK));

        // Chỉ gửi nếu user đã có password (LOCAL hoặc Google đã đặt pass)
        if (u.getPasswordHash() == null || u.getPasswordHash().isBlank()) {
            // Gợi ý FE hiển thị “Đặt mật khẩu lần đầu” thay vì reset
            return;
        }

        String raw = passwordTokenService.issuePasswordToken(u, PasswordTokenPurpose.RESET, activationTtl);

        String link = FRONTEND_HOST + "/auth" + "/reset-password?token=" + raw;
        String html = mailTemplateService.renderResetPasswordEmail(link, ttlText(activationTtl));
        emailSender.send(u.getEmail(), "[JobHuntly] Reset your password", html);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User u = passwordTokenService.verifyPasswordTokenOrThrow(token, PasswordTokenPurpose.RESET);

        // Cho cả LOCAL và Google (đã set pass)
        u.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepo.save(u);

        passwordTokenService.clearPasswordToken(u);
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

    @Transactional
    protected void issueAndEmailActivationToken(User user) {
        String raw = newUrlSafeToken(32);
        String hash = sha256Hex(raw);

        user.setActivationToken(hash);
        user.setActivationTokenExpiresAt(Instant.now().plus(activationTtl));
        userRepository.save(user);

        String ttlText = ttlText(activationTtl);

        String activationLink = FRONTEND_HOST + "/auth" + "/activate?token=" + raw;


        Context context = new Context();
        context.setVariable("activationLink", activationLink);
        context.setVariable("ttlText", ttlText);
        context.setVariable("appName", "JobHuntly");
        context.setVariable("year", java.time.Year.now().toString());
        context.setVariable("supportEmail", "contact.jobhuntly@gmail.com");
        context.setVariable("logoUrl", "https://res.cloudinary.com/dfbqhd5ht/image/upload/v1757058535/logo-title-white_yjzvvr.png");

        String htmlContent = templateEngine.process("activation-email", context);

        emailSender.send(
                user.getEmail(),
                "[JobHuntly] Activate your account",
                htmlContent
        );
    }

    private static String ttlText(Duration ttl) {
        long m = ttl.toMinutes();
        return m < 60 ? (m + " minutes") : (ttl.toHours() + " hours");
    }

}
