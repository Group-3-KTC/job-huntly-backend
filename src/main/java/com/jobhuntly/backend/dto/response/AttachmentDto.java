package com.jobhuntly.backend.dto.response;

import com.jobhuntly.backend.entity.TicketMessageAttachment;

public record AttachmentDto(
        Long id,
        String filename,
        String contentType,
        Long sizeBytes,
        String contentId,
        boolean inline,
        String storageProvider,
        String storagePublicId,
        String storageUrl
) {
    public static AttachmentDto from(TicketMessageAttachment a) {
        return new AttachmentDto(
                a.getId(),
                a.getFilename(),
                a.getContentType(),
                a.getSizeBytes(),
                a.getContentId(),
                a.isInline(),
                a.getStorageProvider(),
                a.getStoragePublicId(),
                a.getStorageUrl()
        );
    }
}

