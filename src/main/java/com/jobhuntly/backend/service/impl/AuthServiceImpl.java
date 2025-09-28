package com.jobhuntly.backend.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.jobhuntly.backend.dto.auth.RefreshResult;
import com.jobhuntly.backend.dto.auth.StartSessionResult;
import com.jobhuntly.backend.dto.auth.request.GoogleLoginRequest;
import com.jobhuntly.backend.dto.auth.request.LoginRequest;
import com.jobhuntly.backend.dto.auth.request.RegisterRequest;
import com.jobhuntly.backend.dto.auth.response.LoginResponse;
import com.jobhuntly.backend.dto.auth.response.MeResponse;
import com.jobhuntly.backend.dto.auth.response.RegisterResponse;
import com.jobhuntly.backend.entity.CandidateProfile;
import com.jobhuntly.backend.entity.Role;
import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.entity.enums.OneTimeTokenPurpose;
import com.jobhuntly.backend.entity.enums.Status;
import com.jobhuntly.backend.exception.AccountBannedException;
import com.jobhuntly.backend.exception.GoogleAccountNeedsPasswordException;
import com.jobhuntly.backend.repository.CandidateProfileRepository;
import com.jobhuntly.backend.repository.RoleRepository;
import com.jobhuntly.backend.repository.UserRepository;
import com.jobhuntly.backend.security.cookie.AuthCookieService;
import com.jobhuntly.backend.security.cookie.CookieProperties;
import com.jobhuntly.backend.security.jwt.JwtUtil;
import com.jobhuntly.backend.service.AuthService;
import com.jobhuntly.backend.service.OneTimeTokenService;
import com.jobhuntly.backend.service.SessionService;
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

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final EmailValidator emailValidator;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final CandidateProfileRepository candidateProfileRepo;
    private final AuthCookieService authCookieService;

    private final SpringTemplateEngine templateEngine;
    private final MailTemplateService mailTemplateService;

    private final SessionService sessionService;
    private final OneTimeTokenService oneTimeTokenService;

    private final CookieProperties cookieProps;

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

        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new IllegalStateException("Email is already in use. Please use a different one.");
        });

        Role role = roleRepository.findByRoleName(request.getRole().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(role)
                .status(Status.INACTIVE)
                .isActive(false)
                .build();

        userRepository.save(user);

        issueAndEmailActivationToken(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse("success", "Registered. Please check your email to activate your account."));
    }

    @Override
    @Transactional
    public ResponseEntity<RegisterResponse> activateAccount(String tokenRaw) {
        if (tokenRaw == null || tokenRaw.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing token");
        }

        User user = oneTimeTokenService.verifyAndConsumeOrThrow(tokenRaw, OneTimeTokenPurpose.ACTIVATION);

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

            if (!oneTimeTokenService.canResend(user, OneTimeTokenPurpose.ACTIVATION, resendCooldown)) {
                return;
            }

            issueAndEmailActivationToken(user);
        });

        return ResponseEntity.ok().build();
    }

    @Override
    public LoginResponse login(LoginRequest request, HttpServletRequest req, HttpServletResponse res) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getGoogleId() != null && (user.getPasswordHash() == null || user.getPasswordHash().isBlank())) {
            throw new GoogleAccountNeedsPasswordException(
                    "This account uses Google Sign-In. Please sign in with Google or set a password first.",
                    user.getEmail()
            );
        }

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String actualRole = user.getRole().getRoleName().toUpperCase();
        String requestedRole = Optional.ofNullable(request.getRole()).map(String::toUpperCase).orElse(null);
        if (requestedRole != null && !requestedRole.equals(actualRole)) {
            throw new org.springframework.security.authentication.BadCredentialsException("Role không phù hợp");
        }

        if ("BANNED".equalsIgnoreCase(String.valueOf(user.getStatus()))) {
            throw new AccountBannedException(
                    "Your account has been banned. If you believe this is a mistake, please contact our support."
            );
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        StartSessionResult ss = sessionService.startSession(user, req, deviceLabelFromUA(req));
        authCookieService.setRefreshCookie(res, ss.refreshToken(), jwtUtil.getRefreshTtl());
        String access = jwtUtil.issueAccessToken(user);
        authCookieService.setAccessCookie(res, access, jwtUtil.getAccessTtl());

        String avatar = candidateProfileRepo.findByUser_Id(user.getId())
                .map(CandidateProfile::getAvatar)
                .orElse(null);

        return LoginResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(actualRole)
                .avatarUrl(avatar)
                .build();
    }

    @Override
    public LoginResponse loginWithGoogle(GoogleLoginRequest request,
                                         HttpServletRequest req,
                                         HttpServletResponse res) {

        GoogleIdToken idToken = verifyGoogleIdToken(request.getIdToken(), GOOGLE_CLIENT_ID);
        if (idToken == null) throw new RuntimeException("Invalid Google ID token");

        GoogleIdToken.Payload payload = idToken.getPayload();
        String googleUserId = payload.getSubject();
        String email = payload.getEmail();
        boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
        String fullName = (String) payload.get("name");
        String avatarUrl = (String) payload.get("picture");

        if (email == null || !emailVerified) {
            throw new RuntimeException("Google email is missing or not verified");
        }

        User user = userRepository.findByGoogleId(googleUserId)
                .orElseGet(() -> userRepository.findByEmail(email).orElse(null));

        if (user == null) {
            Role role = roleRepository.findByRoleName("CANDIDATE")
                    .orElseThrow(() -> new RuntimeException("Default role not found"));
            user = User.builder()
                    .email(email)
                    .fullName(fullName)
                    .googleId(googleUserId)
                    .status(Status.ACTIVE)
                    .isActive(true)
                    .role(role)
                    .build();
            user = userRepository.save(user);

            CandidateProfile profile = new CandidateProfile();
            profile.setUser(user);
            profile.setAvatar(avatarUrl);
            candidateProfileRepo.save(profile);

        } else {
            if (user.getStatus() == Status.BANNED) {
                throw new AccountBannedException(
                        "Your account has been banned. If you believe this is a mistake, please contact our support."
                );
            }

            if (user.getGoogleId() == null) {
                user.setGoogleId(googleUserId);
            }

            if (user.getStatus() == Status.ACTIVE && !Boolean.TRUE.equals(user.getIsActive())) {
                user.setIsActive(true);
            }

            userRepository.save(user);
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        StartSessionResult ss = sessionService.startSession(user, req, deviceLabelFromUA(req));
        authCookieService.setRefreshCookie(res, ss.refreshToken(), jwtUtil.getRefreshTtl());
        String access = jwtUtil.issueAccessToken(user);
        authCookieService.setAccessCookie(res, access, jwtUtil.getAccessTtl());

        String roleName = user.getRole() != null ? user.getRole().getRoleName().toUpperCase() : "CANDIDATE";
        String avatar = candidateProfileRepo.findByUser_Id(user.getId())
                .map(CandidateProfile::getAvatar)
                .orElse(null);

        return new LoginResponse(user.getEmail(), user.getFullName(), roleName, avatar);
    }


    @Override
    public MeResponse getUserMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if ("BANNED".equalsIgnoreCase(String.valueOf(user.getStatus()))) {
            throw new AccountBannedException(
                    "Your account has been banned. If you believe this is a mistake, please contact our support."
            );
        }

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
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.OK));

        // Chỉ cho phép nếu là Google account CHƯA có pass
        if (u.getGoogleId() == null || (u.getPasswordHash() != null && !u.getPasswordHash().isBlank())) {
            return;
        }

        String raw = oneTimeTokenService.issue(u, OneTimeTokenPurpose.SET_PASSWORD, activationTtl);
        String link = FRONTEND_HOST + "/password/set?setpw_token=" + raw;
        String html = mailTemplateService.renderSetPasswordEmail(link, ttlText(activationTtl));
        emailSender.send(u.getEmail(), "[JobHuntly] Set your password", html);
    }

    @Override
    @Transactional
    public void setPassword(String token, String newPassword) {
        User u = oneTimeTokenService.verifyAndConsumeOrThrow(token, OneTimeTokenPurpose.SET_PASSWORD);

        if (u.getGoogleId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a Google account");
        }

        u.setPasswordHash(passwordEncoder.encode(newPassword));
        u.setPasswordSet(true);
        u.setPasswordChangedAt(Instant.now());
        userRepository.save(u);
    }

    @Override
    public void sendResetPasswordLink(String email) {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.OK));

        // Chỉ gửi nếu user đã có password (LOCAL hoặc Google đã set)
        if (u.getPasswordHash() == null || u.getPasswordHash().isBlank()) {
            return;
        }

        String raw = oneTimeTokenService.issue(u, OneTimeTokenPurpose.RESET_PASSWORD, activationTtl);
        String link = FRONTEND_HOST + "/password/reset?reset_token=" + raw;
        String html = mailTemplateService.renderResetPasswordEmail(link, ttlText(activationTtl));
        emailSender.send(u.getEmail(), "[JobHuntly] Reset your password", html);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User u = oneTimeTokenService.verifyAndConsumeOrThrow(token, OneTimeTokenPurpose.RESET_PASSWORD);

        u.setPasswordHash(passwordEncoder.encode(newPassword));
        u.setPasswordSet(true);
        u.setPasswordChangedAt(Instant.now());
        userRepository.save(u);
    }

    @Override
    public void refreshToken(HttpServletRequest req, HttpServletResponse res) {
        String rawRefresh = authCookieService.readCookie(req, cookieProps.getRefreshName()).orElse(null);
        if (rawRefresh == null || rawRefresh.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing refresh token");
        }

        RefreshResult rr = sessionService.rotate(rawRefresh, req);

        authCookieService.setRefreshCookie(res, rr.newRefreshToken(), jwtUtil.getRefreshTtl());

        String access = jwtUtil.issueAccessToken(rr.user());
        authCookieService.setAccessCookie(res, access, jwtUtil.getAccessTtl());
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
        String raw = oneTimeTokenService.issue(user, OneTimeTokenPurpose.ACTIVATION, activationTtl);

        String activationLink = FRONTEND_HOST + "/activate?active_token=" + raw;

        String ttlText = ttlText(activationTtl);
        Context context = new Context();
        context.setVariable("activationLink", activationLink);
        context.setVariable("ttlText", ttlText);
        context.setVariable("appName", "JobHuntly");
        context.setVariable("year", java.time.Year.now().toString());
        context.setVariable("supportEmail", "help.jobhuntly@gmail.com");
        context.setVariable("logoUrl", "https://res.cloudinary.com/dfbqhd5ht/image/upload/v1757058535/logo-title-white_yjzvvr.png");

        String htmlContent = templateEngine.process("activation-email", context);
        emailSender.send(user.getEmail(), "[JobHuntly] Activate your account", htmlContent);
    }

    private static String ttlText(Duration ttl) {
        long m = ttl.toMinutes();
        return m < 60 ? (m + " minutes") : (ttl.toHours() + " hours");
    }

    private String deviceLabelFromUA(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) return "Unknown Device";

        String os = userAgent.contains("Windows") ? "Windows" :
                userAgent.contains("Mac") ? "Mac" :
                        userAgent.contains("X11") ? "Unix" :
                                userAgent.contains("Android") ? "Android" :
                                        userAgent.contains("iPhone") ? "iPhone" : "Unknown OS";

        String browser = (userAgent.contains("Chrome")) ? "Chrome" :
                (userAgent.contains("Firefox")) ? "Firefox" :
                        (userAgent.contains("Safari") && !userAgent.contains("Chrome")) ? "Safari" :
                                (userAgent.contains("Edge")) ? "Edge" : "Unknown Browser";

        return browser + " - " + os;
    }
}
