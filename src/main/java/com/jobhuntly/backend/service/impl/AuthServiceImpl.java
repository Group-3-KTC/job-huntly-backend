package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.auth.request.LoginRequest;
import com.jobhuntly.backend.dto.auth.request.RegisterRequest;
import com.jobhuntly.backend.dto.auth.response.LoginResponse;
import com.jobhuntly.backend.dto.auth.response.RegisterResponse;
import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.entity.Role;
import com.jobhuntly.backend.entity.enums.Status;
import com.jobhuntly.backend.repository.UserRepository;
import com.jobhuntly.backend.repository.RoleRepository;
import com.jobhuntly.backend.security.jwt.JwtUtil;
import com.jobhuntly.backend.service.AuthService;
import com.jobhuntly.backend.service.email.EmailSender;
import com.jobhuntly.backend.service.email.EmailValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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


    @Override
    public RegisterResponse register(RegisterRequest request) {
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
                .role(role)
                .isActive(false)
                .activationToken(token)
                .build();

        user.setStatus(Status.INACTIVE);

        userRepository.save(user);

        String activationLink = "http://localhost:8080/api/auth/activate?token=" + token;

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

        return new RegisterResponse("Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt.");
    }

    @Override
    public RegisterResponse activateAccount(String token) {
        User user = userRepository.findByActivationToken(token)
                .orElseThrow(() -> new IllegalStateException("Token không hợp lệ"));

        user.setIsActive(true);
        user.setStatus(Status.ACTIVE);
        user.setActivationToken(null);
        userRepository.save(user);

        return new RegisterResponse("Tài khoản đã được kích hoạt thành công!");
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // lấy user để build payload
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String roleName = user.getRole().getRoleName(); // ví dụ: CANDIDATE
        String token = jwtUtil.generateToken(user.getEmail(), roleName);

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn( /* ví dụ 30 ngày (giây) */ 30L * 24 * 60 * 60 )
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(roleName)
                .build();
    }
}
