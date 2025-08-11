package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.request.RegisterRequest;
import com.jobhuntly.backend.dto.response.AuthResponse;
import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.entity.Role;
import com.jobhuntly.backend.entity.enums.Status;
import com.jobhuntly.backend.repository.UserRepository;
import com.jobhuntly.backend.repository.RoleRepository;
import com.jobhuntly.backend.service.AuthService;
import com.jobhuntly.backend.service.email.EmailSender;
import com.jobhuntly.backend.service.email.EmailValidator;
import lombok.RequiredArgsConstructor;
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

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (!emailValidator.test(request.getEmail())) {
            throw new IllegalStateException("Email không hợp lệ");
        }

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new IllegalStateException("Email đã được sử dụng");
        }
        Role role = roleRepository.findByRoleName(request.getRole())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        String token = UUID.randomUUID().toString();

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .activationToken(token)
                .build();

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

        return new AuthResponse("Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt.");
    }

    @Override
    public AuthResponse activateAccount(String token) {
        User user = userRepository.findByActivationToken(token)
                .orElseThrow(() -> new IllegalStateException("Token không hợp lệ"));

        user.setStatus(Status.ACTIVE);
        user.setActivationToken(null);
        userRepository.save(user);

        return new AuthResponse("Tài khoản đã được kích hoạt thành công!");
    }
}
