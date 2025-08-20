package com.jobhuntly.backend.config;

import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {
    @Bean
    public JavaMailSender javaMailSender(Environment env) {
        JavaMailSenderImpl s = new JavaMailSenderImpl();
        s.setHost(env.getProperty("spring.mail.host", "smtp.gmail.com"));
        s.setPort(env.getProperty("spring.mail.port", Integer.class, 587));
        s.setUsername(env.getProperty("spring.mail.username"));
        s.setPassword(env.getProperty("spring.mail.password"));
        Properties p = s.getJavaMailProperties();
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.starttls.enable", "true");
        p.put("mail.smtp.starttls.required", "true");
        return s;
    }
}
