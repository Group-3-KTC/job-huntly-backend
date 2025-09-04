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
import com.jobhuntly.backend.entity.enums.Status;
import com.jobhuntly.backend.repository.CandidateProfileRepository;
import com.jobhuntly.backend.repository.RoleRepository;
import com.jobhuntly.backend.repository.UserRepository;
import com.jobhuntly.backend.security.jwt.JwtUtil;
import com.jobhuntly.backend.service.AuthService;
import com.jobhuntly.backend.service.auth.AuthCookieService;
import com.jobhuntly.backend.service.email.EmailSender;
import com.jobhuntly.backend.service.email.EmailValidator;
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
    private final AuthCookieService authCookieService;
    private final SpringTemplateEngine templateEngine;
    @Value("${google.client-id}")
    private String GOOGLE_CLIENT_ID;
    @Value("${backend.host}")
    private String BACKEND_HOST;
    @Value("${backend.prefix}")
    private String BACKEND_PREFIX;
    @Value("${frontend.host}")
    private String frontendBaseUrl;

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired link"));

        Instant now = Instant.now();
        Instant exp = user.getActivationTokenExpiresAt();
        if (exp == null || !exp.isAfter(now)) {
            user.setActivationToken(null);
            user.setActivationTokenExpiresAt(null);
            userRepository.save(user);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired link");
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

        String avatar = candidateProfileRepo.findByUser_Id(user.getId())
                .map(CandidateProfile::getAvatar)
                .orElse(null);

        long accessTtlSeconds = Duration.ofDays(30).toSeconds();
        String token = jwtUtil.generateToken(user.getEmail(), actualRole.toUpperCase(), user.getId());

        authCookieService.setAuthCookie(req, res, token, accessTtlSeconds);

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

        authCookieService.setAuthCookie(req, res, token, accessTtlSeconds);

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

        long ttlMinutes = activationTtl.toMinutes();
        String ttlText = ttlMinutes < 60
                ? ttlMinutes + " minutes"
                : activationTtl.toHours() + " hours";

        String activationLink = frontendBaseUrl + "/activate?token=" + raw;

        Context context = new Context();
        context.setVariable("activationLink", activationLink);
        context.setVariable("ttlText", ttlText);
        context.setVariable("appName", "JobHuntly");
        context.setVariable("year", java.time.Year.now().toString());
        context.setVariable("supportEmail", "support@jobhuntly.com");
        context.setVariable("logoUrl", "https://your-cdn.com/jobhuntly-logo.png");

        String htmlContent = templateEngine.process("activation-email", context);

        emailSender.send(
                user.getEmail(),
                "[JobHuntly] Activate your account",
                htmlContent
        );
    }

}
