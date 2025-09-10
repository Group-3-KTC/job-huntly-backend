package com.jobhuntly.backend.util;

public final class EmailCanonicalizer {
    private EmailCanonicalizer() {}

    public static String canonicalize(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase();

        // Hỗ trợ "Tên <email@domain>"
        int lt = s.indexOf('<'), gt = s.indexOf('>');
        if (lt >= 0 && gt > lt) s = s.substring(lt + 1, gt).trim();

        int at = s.lastIndexOf('@');
        if (at < 0) return s;
        String local = s.substring(0, at);
        String domain = s.substring(at + 1);

        if ("googlemail.com".equals(domain)) domain = "gmail.com";

        if ("gmail.com".equals(domain)) {
            int plus = local.indexOf('+');
            if (plus >= 0) local = local.substring(0, plus);
            local = local.replace(".", "");
        }
        return local + "@" + domain;
    }
}
