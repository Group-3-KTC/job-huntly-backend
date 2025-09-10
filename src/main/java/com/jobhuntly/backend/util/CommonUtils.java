package com.jobhuntly.backend.util;

import com.jobhuntly.backend.entity.TicketMessage;

import java.util.Set;

public class CommonUtils {
    public static org.springframework.data.domain.Sort parseSort(String sort, org.springframework.data.domain.Sort fallback) {
        try {
            String[] p = sort.split(",", 2);
            String field = p[0];
            var dir = (p.length > 1 && "ASC".equalsIgnoreCase(p[1]))
                    ? org.springframework.data.domain.Sort.Direction.ASC
                    : org.springframework.data.domain.Sort.Direction.DESC;
            // Whitelist để an toàn
            Set<String> allowed = Set.of("createdAt", "id");
            if (!allowed.contains(field)) field = "createdAt";
            return org.springframework.data.domain.Sort.by(dir, field);
        } catch (Exception e) {
            return fallback;
        }
    }

    public static String buildSnippet(TicketMessage m) {
        if (m == null) return "";
        String txt = m.getBodyText();
        if (txt == null || txt.isBlank()) {
            txt = stripHtml(m.getBodyHtml());
        }
        if (txt == null) return "";
        String oneLine = txt.replaceAll("\\s+", " ").trim();
        return oneLine.length() > 180 ? oneLine.substring(0, 180) : oneLine;
    }

    public static String stripHtml(String html) {
        if (html == null) return null;
        String noStyle = html.replaceAll("(?is)<style.*?>.*?</style>", " ");
        String noScript = noStyle.replaceAll("(?is)<script.*?>.*?</script>", " ");
        String noTags = noScript.replaceAll("<[^>]+>", " ");
        return noTags.replaceAll("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
