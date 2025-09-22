package com.jobhuntly.backend.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;

@Configuration
public class GoogleCalendarConfig {

    @Value("${gcal.sa.credentials.b64:}") // lấy từ secret/ENV
    private String credentialsB64;

    @Value("${gcal.sa.credentials.path:}") // fallback dev/local
    private String credentialsPath;

    @Value("${gcal.impersonate:}")
    private String impersonatedUser;

    @Bean
    public Calendar googleCalendar() throws Exception {
        InputStream in;

        if (!credentialsB64.isBlank()) {
            // Decode từ ENV base64
            byte[] raw = Base64.getDecoder().decode(credentialsB64);
            in = new ByteArrayInputStream(raw);

        } else if (!credentialsPath.isBlank()) {
            // Đọc từ path
            Resource res;
            if (credentialsPath.startsWith("classpath:")) {
                res = new ClassPathResource(credentialsPath.substring("classpath:".length()));
            } else if (credentialsPath.startsWith("file:")) {
                res = new FileSystemResource(credentialsPath.substring("file:".length()));
            } else {
                res = new FileSystemResource(credentialsPath);
            }
            in = res.getInputStream();

        } else {
            // Dev fallback nếu để file trong resources (đã .gitignore)
            in = new ClassPathResource("google/service-account.json")
                    .getInputStream();
        }

        GoogleCredentials creds = GoogleCredentials.fromStream(in)
                .createScoped(List.of("https://www.googleapis.com/auth/calendar"));

        if (!impersonatedUser.isBlank() && creds instanceof ServiceAccountCredentials sac) {
            creds = sac.createDelegated(impersonatedUser);
        }

        var httpInit = new HttpCredentialsAdapter(creds);

        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                httpInit).setApplicationName("JobHuntly").build();
    }
}
