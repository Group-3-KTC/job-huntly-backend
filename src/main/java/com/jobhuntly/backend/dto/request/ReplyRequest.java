package com.jobhuntly.backend.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ReplyRequest {
    private String bodyHtml;
    private List<String> cc;
    private List<String> to;
    private String subjectOverride;
    private String replyToMessageId;
    private String message;

}