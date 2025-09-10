package com.jobhuntly.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {
    @Bean
    @Primary
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

    @Bean
    public Properties mailProps(Environment env) {
        Properties p = new Properties();
        p.put("mail.store.protocol", "imaps");
        p.put("mail.imaps.host", env.getProperty("spring.mail.imap.host", "imap.zoho.com"));
        p.put("mail.imaps.port", env.getProperty("spring.mail.imap.port", Integer.class, 993));
        p.put("mail.imaps.ssl.enable", "true");
        p.put("mail.imaps.auth", "true");
        p.put("mail.imaps.peek", String.valueOf(env.getProperty("spring.mail.imap.peek", Boolean.class, true)));
        return p;
    }

    @Bean("gmailMailSender")
    public JavaMailSender gmailMailSender(Environment env) {
        JavaMailSenderImpl s = new JavaMailSenderImpl();
        s.setHost(env.getProperty("mail.gmail.host", "smtp.gmail.com"));
        s.setPort(env.getProperty("mail.gmail.port", Integer.class, 587));
        s.setUsername(env.getProperty("mail.gmail.username"));
        s.setPassword(env.getProperty("mail.gmail.password"));

        Properties p = s.getJavaMailProperties();
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.starttls.enable", "true");
        p.put("mail.smtp.starttls.required", "true");
        return s;
    }
}
