package com.jobhuntly.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.lang.Nullable;

@Data
@AllArgsConstructor
public class Attachment {
    private String filename;
    private ByteArrayResource resource;
    private @Nullable String contentType;
}