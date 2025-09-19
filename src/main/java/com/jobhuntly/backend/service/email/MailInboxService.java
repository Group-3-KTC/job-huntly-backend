package com.jobhuntly.backend.service.email;

import com.jobhuntly.backend.dto.response.AttachmentDto;
import com.jobhuntly.backend.dto.response.InboxItemDto;
import com.jobhuntly.backend.dto.response.MessageDto;
import com.jobhuntly.backend.entity.Ticket;
import com.jobhuntly.backend.entity.TicketMessage;
import com.jobhuntly.backend.entity.TicketMessageAttachment;
import com.jobhuntly.backend.entity.enums.MessageDirection;
import com.jobhuntly.backend.entity.enums.TicketStatus;
import com.jobhuntly.backend.repository.TicketMessageAttachmentRepository;
import com.jobhuntly.backend.repository.TicketMessageRepository;
import com.jobhuntly.backend.repository.TicketRepository;
import com.jobhuntly.backend.service.impl.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailInboxService {

    private final TicketRepository ticketRepository;
    private final TicketMessageRepository ticketMessageRepository;
    private final TicketService ticketService;

    private final TicketMessageAttachmentRepository attachmentRepository;
    private final CloudinaryService cloudinaryService;

    private static final Tika TIKA = new Tika();


    public Page<InboxItemDto> listTickets(String statusStr,
                                          String customerEmail,
                                          String q,
                                          int page,
                                          int size,
                                          Sort sort) {

        TicketStatus status = null;
        if (StringUtils.hasText(statusStr)) {
            status = TicketStatus.valueOf(statusStr.toUpperCase(Locale.ROOT));
        }

        Sort fallback = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, (sort == null || sort.isUnsorted()) ? fallback : sort);

        Page<Ticket> ticketsPage = ticketRepository.searchTickets(status, blankToNull(customerEmail), blankToNull(q), pageable);

        List<Long> ids = ticketsPage.getContent().stream().map(Ticket::getId).toList();
        Map<Long, TicketMessage> lastByTicket = ids.isEmpty()
                ? Collections.emptyMap()
                : ticketMessageRepository.findLastMessagesForTickets(ids).stream()
                .collect(Collectors.toMap(tm -> tm.getTicket().getId(), Function.identity()));

        List<InboxItemDto> items = ticketsPage.getContent().stream()
                .map(t -> {
                    TicketMessage last = lastByTicket.get(t.getId());
                    return new InboxItemDto(
                            t.getId(),
                            t.getSubject(),
                            t.getStatus(),
                            t.getCustomerEmail(),
                            t.getCreatedAt(),
                            t.getThreadId(),
                            last != null ? last.getSentAt() : t.getCreatedAt(),
                            last != null ? last.getFromEmail() : t.getFromEmail(),
                            last != null ? snippetOf(last) : null
                    );
                })
                .toList();

        return new PageImpl<>(items, ticketsPage.getPageable(), ticketsPage.getTotalElements());
    }

    public Page<MessageDto> listMessages(Long ticketId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "sentAt"));

        Page<TicketMessage> pageData =
                ticketMessageRepository.findByTicket_IdOrderBySentAtAsc(ticketId, pageable);

        List<TicketMessage> msgs = pageData.getContent();
        List<Long> msgIds = msgs.stream().map(TicketMessage::getId).toList();

        Map<Long, List<AttachmentDto>> attsByMsg = msgIds.isEmpty()
                ? Map.of()
                : attachmentRepository.findByTicketMessage_IdIn(msgIds).stream()
                .collect(Collectors.groupingBy(
                        a -> a.getTicketMessage().getId(),
                        Collectors.mapping(AttachmentDto::from, Collectors.toList())
                ));

        List<MessageDto> dtoContent = msgs.stream()
                .map(m -> MessageDto.from(m, attsByMsg.getOrDefault(m.getId(), List.of())))
                .toList();

        log.info("listMessages ticket={} page={} size={} -> msgs={}, atts-total={}",
                ticketId, page, size, dtoContent.size(),
                attsByMsg.values().stream().mapToInt(List::size).sum());

        return new PageImpl<>(dtoContent, pageData.getPageable(), pageData.getTotalElements());
    }

    private String snippetOf(TicketMessage m) {
        String raw = (m.getBodyText() != null && !m.getBodyText().isBlank())
                ? m.getBodyText()
                : (m.getBodyHtml() != null ? m.getBodyHtml().replaceAll("<[^>]+>", " ") : "");
        raw = raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
        return raw.length() > 180 ? raw.substring(0, 180) : raw;
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    @Transactional
    public TicketMessage saveInboundMimeMessage(MimeMessage mm) throws Exception {
        String messageId = ensureAngle(mm.getMessageID());
        String subjectRaw = mm.getSubject() != null ? mm.getSubject() : "(no subject)";
        String subjectNorm = normalizeSubject(subjectRaw);
        String fromEmail = extractFromEmail(mm);
        Instant sentAt = mm.getSentDate() != null ? mm.getSentDate().toInstant() : Instant.now();

        String inReplyTo = ensureAngle(firstHeader(mm, "In-Reply-To"));
        String refsHeader = firstHeader(mm, "References");
        java.util.List<String> refIds = parseMessageIds(refsHeader);

        Ticket ticket = null;
        if (StringUtils.hasText(inReplyTo)) {
            var parentMsg = ticketMessageRepository.findByMessageId(inReplyTo).orElse(null);
            if (parentMsg != null) ticket = parentMsg.getTicket();
        }

        if (ticket == null && !refIds.isEmpty()) {
            for (int i = refIds.size() - 1; i >= 0; i--) {
                String ref = ensureAngle(refIds.get(i));
                var m = ticketMessageRepository.findByMessageId(ref).orElse(null);
                if (m != null) { ticket = m.getTicket(); break; }
            }
        }

        if (ticket == null) {
            ticket = ticketRepository
                    .findFirstByCustomerEmailAndSubjectOrderByCreatedAtDesc(fromEmail, subjectNorm)
                    .orElse(null);
        }

        if (ticket == null) {
            ticket = new Ticket();
            ticket.setSubject(subjectNorm);
            ticket.setFromEmail(fromEmail != null ? fromEmail : "unknown@local");
            ticket.setCustomerEmail(fromEmail);
            String rootId = !refIds.isEmpty() ? ensureAngle(refIds.get(0)) : messageId;
            ticket.setThreadId(StringUtils.hasText(rootId) ? rootId : (messageId != null ? messageId : UUID.randomUUID().toString()));
            ticket.setCreatedAt(sentAt);
            ticket.setStatus(TicketStatus.OPEN);
            ticket = ticketRepository.save(ticket);
        }

        return saveInboundMimeMessageForTicket(mm, ticket);
    }

    @Transactional
    public TicketMessage saveInboundMimeMessageForTicket(MimeMessage mm, Ticket ticket) throws Exception {
        ParseCtx ctx = new ParseCtx();
        extractParts(mm, ctx);
        log.info("Parsed parts: html?={}, text?={}, attachments={} -> {}",
                ctx.html != null, ctx.text != null, ctx.attachments.size(),
                ctx.attachments.stream()
                        .map(a -> a.contentType() + "|" + a.filename() + "|" + (a.inline() ? "inline" : "attach"))
                        .toList());

        Map<AttachmentDraft, CloudUpload> uploaded = new HashMap<>();
        int uploadedOk = 0;

        for (AttachmentDraft d : ctx.attachments) {
            String ctLower = (d.contentType != null ? d.contentType.toLowerCase(Locale.ROOT) : "");
            if ("image/jpg".equals(ctLower) || "image/pjpeg".equals(ctLower) || "image/jpe".equals(ctLower)) {
                ctLower = "image/jpeg";
            } else if (ctLower.isBlank()) {
                ctLower = "application/octet-stream";
            }

            final String resourceType =
                    ctLower.startsWith("image/") ? "image" :
                            ctLower.startsWith("video/") ? "video" : "raw";

            String cleanedFilename = safeFilenameForCloudinary(d.filename);

            String publicId = null;
            String secureUrl = null;

            try {
                log.debug("Uploading attachment: file={} ct={} resourceType={}", cleanedFilename, ctLower, resourceType);

                var up = cloudinaryService.uploadBytes(
                        d.data,
                        "tickets/" + ticket.getId(),
                        cleanedFilename,
                        resourceType
                );
                publicId = up.publicId();
                secureUrl = up.secureUrl();
                uploadedOk++;
                log.info("Uploaded attachment OK: filename={} cid={} inline={} -> {}",
                        d.filename, d.cid, d.inline, secureUrl);
            } catch (Exception ex) {
                // vẫn lưu row DB bên dưới dù upload fail
                log.warn("Upload failed: filename={} ct={} cid={} inline={}. Will save row without URL.",
                        d.filename, ctLower, d.cid, d.inline, ex);
            }

            uploaded.put(d, new CloudUpload(publicId, secureUrl));

            // map CID -> URL để thay thế trong HTML nếu là inline image
            if (d.inline && StringUtils.hasText(d.cid) && StringUtils.hasText(secureUrl)) {
                ctx.cidToUrl.put(d.cid, secureUrl);
            }
        }

        // --- render HTML & TEXT (thay cid:... -> https ...) ---
        String html = ctx.html != null ? ctx.html
                : (ctx.text != null ? "<pre>" + escapeHtml(ctx.text) + "</pre>" : "<p>(no content)</p>");
        html = replaceCidWithUrl(html, ctx.cidToUrl);

        log.info("Before strip len={}...", html.length());
        String cleanedHtml = stripQuotedReplyHtml(html);
        log.info("After strip len={}...", cleanedHtml.length());
        String bodyText = htmlToText(cleanedHtml);

        // --- headers ---
        String messageId = ensureAngle(mm.getMessageID());
        String inReplyTo = firstHeader(mm, "In-Reply-To");
        String fromEmail = extractFromEmail(mm);
        Instant sentAt   = mm.getSentDate() != null ? mm.getSentDate().toInstant() : Instant.now();

        // --- lưu message INBOUND ---
        TicketMessage tm = new TicketMessage();
        tm.setTicket(ticket);
        tm.setMessageId(messageId);
        tm.setInReplyTo(inReplyTo);
        tm.setFromEmail(fromEmail);
        tm.setSentAt(sentAt);
        tm.setDirection(MessageDirection.INBOUND);
        tm.setBodyText(bodyText);
        tm.setBodyHtml(cleanedHtml);
        ticketMessageRepository.save(tm);

        // --- lưu attachments (kể cả khi upload fail) ---
        int savedRows = 0;
        for (AttachmentDraft d : ctx.attachments) {
            CloudUpload up = uploaded.get(d);
            TicketMessageAttachment att = new TicketMessageAttachment();
            att.setTicketMessage(tm);
            att.setFilename(d.filename);
            att.setContentType(d.contentType);
            att.setSizeBytes(d.size);
            att.setContentId(d.cid);
            att.setInline(d.inline);
            att.setStorageProvider("CLOUDINARY");
            att.setStoragePublicId(up != null ? up.publicId : null);
            att.setStorageUrl(up != null ? up.secureUrl : null);
            attachmentRepository.save(att);
            savedRows++;
        }
        log.info("Uploaded OK: {}/{} attachments; saved rows: {}", uploadedOk, ctx.attachments.size(), savedRows);

        // đánh dấu OPEN khi có thư inbound mới
        ticketService.markOpenOnInbound(ticket.getId());

        return tm;
    }

    /** Làm sạch tên file để dùng làm public_id an toàn (ASCII, không ký tự lạ). */
    private static String safeFilenameForCloudinary(String fn) {
        String base = (fn != null && !fn.isBlank()) ? fn : "file";
        // bỏ path, chỉ giữ tên
        base = base.replaceAll("[\\\\/]+", "_");
        // cắt phần ext ra khỏi base (Cloudinary không cần ext trong public_id)
        int dot = base.lastIndexOf('.');
        if (dot > 0) base = base.substring(0, dot);

        base = java.text.Normalizer.normalize(base, java.text.Normalizer.Form.NFKD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^A-Za-z0-9._-]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("(^_|_$)", "");
        if (base.isBlank()) base = "file";
        if (base.length() > 80) base = base.substring(0, 80);

        // thêm hậu tố ngắn chống trùng
        String suffix = java.util.UUID.randomUUID().toString().substring(0, 8);
        return base + "-" + suffix;
    }



    private static class ParseCtx {
        String html;
        String text;
        Map<String, String> cidToUrl = new HashMap<>();
        List<AttachmentDraft> attachments = new ArrayList<>();
    }
    private record AttachmentDraft(String filename, String contentType, long size, String cid, boolean inline, byte[] data) {}
    private record CloudUpload(String publicId, String secureUrl) {}

    private void extractParts(Part part, ParseCtx ctx) throws Exception {
        if (part.isMimeType("message/rfc822")) {            // email bọc email
            Object inner = part.getContent();
            if (inner instanceof Part p) extractParts(p, ctx);
            else if (inner instanceof jakarta.mail.internet.MimeMessage mm) extractParts(mm, ctx);
            return;
        }
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) extractParts(mp.getBodyPart(i), ctx);
            return;
        }
        if (part.isMimeType("text/html")) {
            String html = (String) part.getContent();
            if (!StringUtils.hasText(ctx.html)) ctx.html = html;
            return;
        }
        if (part.isMimeType("text/plain")) {
            String text = (String) part.getContent();
            if (!StringUtils.hasText(ctx.text)) ctx.text = text;
            return;
        }

        // ---- leaf: coi là attachment nếu có bytes ----
        String disp = part.getDisposition();
        String[] cids = part.getHeader("Content-ID");
        String cid = (cids != null && cids.length > 0) ? trimAngles(cids[0]) : null;

        byte[] data = readAllBytes(part.getInputStream());
        if (data == null || data.length == 0) return;

        String headerCT = part.getContentType();
        String contentType = determineContentType(headerCT, part.getFileName(), data);
        contentType = normalizeImageMime(contentType != null ? contentType.toLowerCase(Locale.ROOT) : null);
        if (!StringUtils.hasText(contentType)) contentType = "application/octet-stream";

        String filename = part.getFileName();
        if (!StringUtils.hasText(filename)) filename = guessFilenameByType(contentType, data);
        filename = safeFilename(filename);

        boolean isInline = (cid != null) || "inline".equalsIgnoreCase(disp);

        ctx.attachments.add(new AttachmentDraft(filename, contentType, data.length, cid, isInline, data));
        log.debug("ATTACH captured: ct={}, size={}, disp={}, cid={}, filename={}, inline={}",
                contentType, data.length, disp, cid, filename, isInline);
    }


    private static String guessFilenameByType(String contentType, byte[] data) {
        String ext = null;
        if (StringUtils.hasText(contentType)) {
            String ct = normalizeImageMime(normalizeMime(contentType));
            if ("application/pdf".equals(ct)) ext = "pdf";
            else if ("image/jpeg".equals(ct)) ext = "jpg";
            else if (ct != null && ct.startsWith("image/")) ext = ct.substring("image/".length());
            else if ("application/zip".equals(ct)) ext = "zip";
        }
        if (ext == null || ext.isBlank()) ext = "bin";
        return "attachment." + ext;
    }



    private static String replaceCidWithUrl(String html, Map<String, String> cidToUrl) {
        if (cidToUrl.isEmpty() || html == null) return html;
        Pattern p = Pattern.compile("(?i)(src\\s*=\\s*[\"'])cid:([^\"']+)([\"'])");
        Matcher m = p.matcher(html);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String rawCid = m.group(2);
            String url = cidToUrl.getOrDefault(rawCid, cidToUrl.get(trimAngles(rawCid)));
            if (url != null) {
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group(1) + url + m.group(3)));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String htmlToText(String html) {
        if (html == null) return "";
        return html.replaceAll("(?s)<style.*?>.*?</style>", "")
                .replaceAll("(?s)<script.*?>.*?</script>", "")
                .replaceAll("<br\\s*/?>", "\n")
                .replaceAll("</p>", "\n")
                .replaceAll("<[^>]+>", "")
                .replaceAll("&nbsp;", " ")
                .replace("&lt;", "<").replace("&gt;", ">")
                .replace("&amp;", "&").replace("&quot;", "\"").replace("&#39;", "'");
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'", "&#x27;");
    }

    private static String normalizeMime(String headerContentType) {
        if (headerContentType == null) return null;
        String ct = headerContentType;
        int semi = ct.indexOf(';');
        if (semi > 0) ct = ct.substring(0, semi);
        ct = ct.trim().toLowerCase(Locale.ROOT);
        return normalizeImageMime(ct);
    }

    private static String normalizeImageMime(String ct) {
        if (ct == null) return null;
        return switch (ct) {
            case "image/jpg", "image/pjpeg", "image/jpe" -> "image/jpeg";
            default -> ct;
        };
    }

    private static String determineContentType(String headerContentType, String filename, byte[] data) {
        String ct = normalizeMime(headerContentType);
        if (ct != null && !ct.isBlank() && !ct.equals("application/octet-stream")) return ct;

        if (filename != null && !filename.isBlank()) {
            try {
                String byName = TIKA.detect(filename);
                if (byName != null && !byName.isBlank() && !byName.equals("application/octet-stream")) {
                    return byName.toLowerCase(Locale.ROOT);
                }
            } catch (Exception ignored) {}
        }

        if (data != null && data.length > 0) {
            try {
                String byBytes = TIKA.detect(data);
                if (byBytes != null && !byBytes.isBlank()) {
                    return byBytes.toLowerCase(Locale.ROOT);
                }
            } catch (Exception ignored) {}
        }

        return "application/octet-stream";
    }


    private static String safeFilename(String fn) {
        if (!StringUtils.hasText(fn)) return "file";
        return fn.replaceAll("[\\r\\n\\\\/\\t\\x00]+", "_");
    }


    private static String extractFromEmail(MimeMessage mm) throws Exception {
        Address[] froms = mm.getFrom();
        if (froms != null && froms.length > 0) {
            Address a = froms[0];
            if (a instanceof InternetAddress ia) return ia.getAddress();
            return a.toString();
        }
        return null;
    }

    private static String firstHeader(MimeMessage mm, String name) throws Exception {
        String[] v = mm.getHeader(name);
        return (v != null && v.length > 0) ? v[0] : null;
    }

    private static String ensureAngle(String v) {
        if (v == null || v.isBlank()) return null;
        v = v.trim();
        if (!v.startsWith("<")) v = "<" + v;
        if (!v.endsWith(">"))   v = v + ">";
        return v;
    }

    private static String trimAngles(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.startsWith("<")) t = t.substring(1);
        if (t.endsWith(">"))   t = t.substring(0, t.length() - 1);
        return t;
    }

    private static byte[] readAllBytes(InputStream is) throws Exception {
        try (is) { return is.readAllBytes(); }
    }

    private static int firstIndexOfAny(String s, String... regexes) {
        int ans = -1;
        for (String rgx : regexes) {
            Pattern p = Pattern.compile(rgx,
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.MULTILINE);
            Matcher m = p.matcher(s);
            if (m.find()) {
                int i = m.start();
                if (ans == -1 || i < ans) ans = i;
            }
        }
        return ans;
    }

    private static String stripQuotedReplyHtml(String html) {
        if (html == null || html.isBlank()) return html;
        try {
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(html);
            org.jsoup.nodes.Element body = doc.body();
            if (body == null) return html;

            // --- ĐẾM TRƯỚC ĐỂ LOG (giúp xác nhận có match) ---
            int nAttrBefore   = body.select("div.gmail_attr").size();
            int nGqDivBefore  = body.select("div.gmail_quote").size();
            int nGqBqBefore   = body.select("blockquote.gmail_quote").size();
            int nExtraBefore  = body.select("div.gmail_extra").size();
            int nYahooBefore  = body.select("div.yahoo_quoted").size();
            int nOutlookBefore= body.select("#divRplyFwdMsg").size();
            int nBqBefore     = body.select("blockquote").size();

            body.select("div.gmail_attr").remove();
            body.select("div.gmail_quote").remove();
            body.select("blockquote.gmail_quote").remove();
            body.select("div.gmail_extra").remove();
            body.select("div.yahoo_quoted").remove();
            body.select("#divRplyFwdMsg").remove();
            body.select("blockquote[type=cite]").remove();

            body.select("*:matches((?i)(?:\\bOn\\b|\\bVào\\b|\\bNgày\\b)[\\s\\S]{0,4000}?(?:\\bwrote\\b|đã\\s*viết)\\s*:)")
                    .forEach(el -> {
                        // Xoá el và mọi sibling đứng sau trong parent để cắt toàn bộ phần quoted phía dưới.
                        org.jsoup.nodes.Element p = el.parent();
                        if (p == null) { el.remove(); return; }
                        boolean after = false;
                        java.util.List<org.jsoup.nodes.Node> nodes = new java.util.ArrayList<>(p.childNodes());
                        for (org.jsoup.nodes.Node n : nodes) {
                            if (n == el) after = true;
                            if (after) n.remove();
                        }
                    });

            body.select("blockquote").remove();

            body.select("hr#stopSpelling").remove();

            String out = body.html()
                    .replaceAll("(?is)(<br\\s*/?>\\s*)+$", "")
                    .trim();

            int nAttrAfter   = org.jsoup.Jsoup.parse(out).select("div.gmail_attr").size();
            int nGqDivAfter  = org.jsoup.Jsoup.parse(out).select("div.gmail_quote").size();
            int nGqBqAfter   = org.jsoup.Jsoup.parse(out).select("blockquote.gmail_quote").size();
            int nExtraAfter  = org.jsoup.Jsoup.parse(out).select("div.gmail_extra").size();
            int nYahooAfter  = org.jsoup.Jsoup.parse(out).select("div.yahoo_quoted").size();
            int nOutlookAfter= org.jsoup.Jsoup.parse(out).select("#divRplyFwdMsg").size();
            int nBqAfter     = org.jsoup.Jsoup.parse(out).select("blockquote").size();

            log.info("Quote strip: gmail_attr {}->{} | gmail_quote(div) {}->{} | gmail_quote(bq) {}->{} | extra {}->{} | yahoo {}->{} | outlook {}->{} | blockquote {}->{}",
                    nAttrBefore, nAttrAfter, nGqDivBefore, nGqDivAfter, nGqBqBefore, nGqBqAfter,
                    nExtraBefore, nExtraAfter, nYahooBefore, nYahooAfter, nOutlookBefore, nOutlookAfter,
                    nBqBefore, nBqAfter);

            return out.isBlank() ? html : out;
        } catch (Exception e) {
            return html;
        }
    }





    private static int indexOfRegex(String s, String regex) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher m = p.matcher(s);
        return m.find() ? m.start() : -1;
    }

    private static String normalizeSubject(String s) {
        if (s == null) return null;
        String out = s.trim();
        while (out.matches("(?is)^(re|fw|fwd)\\s*:\\s*.*")) {
            out = out.replaceFirst("(?is)^(re|fw|fwd)\\s*:\\s*", "");
            out = out.trim();
        }
        return out.isBlank() ? "(no subject)" : out;
    }

    private static java.util.List<String> parseMessageIds(String refs) {
        if (!StringUtils.hasText(refs)) return java.util.Collections.emptyList();
        java.util.List<String> ids = new java.util.ArrayList<>();
        Matcher m = Pattern.compile("<[^>]+>").matcher(refs);
        while (m.find()) ids.add(m.group());
        if (ids.isEmpty()) {
            for (String tok : refs.trim().split("\\s+")) if (!tok.isBlank()) ids.add(tok);
        }
        return ids;
    }
}
