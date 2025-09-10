package com.jobhuntly.backend.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ApplicationRequest {

    @NotNull
    private Long jobId;

    private MultipartFile cvFile;

    @Email
    @Size(max = 200)
    private String email;

    @Size(max = 200)
    private String phoneNumber;

    @Size(max = 200)
    private String candidateName;

    private String description;
}
