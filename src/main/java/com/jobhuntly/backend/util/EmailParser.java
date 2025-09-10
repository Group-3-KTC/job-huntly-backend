package com.jobhuntly.backend.util;

import org.springframework.util.StringUtils;

public final class EmailParser {
    private EmailParser() {}

    /**
     * Lấy phần nội dung mới người dùng vừa gõ, bỏ chữ ký/quoted Gmail.
     * Ưu tiên HTML nếu có class gmail_quote/gmail_signature; fallback regex "Vào Th ... đã viết:" / "On ... wrote:"
     */
    public static String extractNewContentPlain(String bodyHtml, String bodyText) {
        // 1) Nếu có HTML: cắt trước .gmail_quote / .gmail_quote_container / .gmail_attr / .gmail_signature
        if (StringUtils.hasText(bodyHtml)) {
            String html = bodyHtml;
            html = cutBefore(html, "(?is)<div\\s+class=\"gmail_quote[^\"]*\".*");
            html = cutBefore(html, "(?is)<div\\s+class=\"gmail_quote_container[^\"]*\".*");
            html = cutBefore(html, "(?is)<div\\s+class=\"gmail_attr\".*"); // "Vào Th..., ... đã viết:"
            html = cutBefore(html, "(?is)<div\\s+class=\"gmail_signature\".*");

            String plain = htmlToPlain(html);
            if (StringUtils.hasText(plain)) {
                return normalizeSpaces(plain);
            }
        }

        // 2) Nếu không có HTML/không match, dùng bodyText + regex common
        String txt = StringUtils.hasText(bodyText) ? bodyText : "";
        // Tiếng Việt Gmail:
        txt = cutBefore(txt, "(?im)^V[àa]o\\s+Th\\s*\\d+.*đ[ãa]\\s+vi[tê]t:.*$");
        // Tiếng Anh:
        txt = cutBefore(txt, "(?im)^On\\s+.*wrote:.*$");
        // Outlook:
        txt = cutBefore(txt, "(?im)^-+\\s*Original Message\\s*-+.*$");
        return normalizeSpaces(txt);
    }

    public static String htmlToPlain(String html) {
        if (!StringUtils.hasText(html)) return "";
        String s = html.replaceAll("(?is)<style.*?>.*?</style>", " ")
                .replaceAll("(?is)<script.*?>.*?</script>", " ")
                .replaceAll("<br\\s*/?>", "\n")
                .replaceAll("</p>", "\n")
                .replaceAll("<[^>]+>", " ")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("\\u00A0", " ");
        return s;
    }

    private static String cutBefore(String src, String pattern) {
        if (src == null) return "";
        var m = java.util.regex.Pattern.compile(pattern).matcher(src);
        if (m.find()) {
            return src.substring(0, m.start());
        }
        return src;
    }

    private static String normalizeSpaces(String s) {
        if (s == null) return "";
        return s.replaceAll("[ \\t\\x0B\\f\\r]+", " ")
                .replaceAll(" *\\n *", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}

