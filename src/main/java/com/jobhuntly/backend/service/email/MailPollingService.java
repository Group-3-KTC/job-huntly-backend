package com.jobhuntly.backend.service.email;

import com.jobhuntly.backend.entity.Ticket;
import com.jobhuntly.backend.entity.TicketMessage;
import com.jobhuntly.backend.entity.enums.MessageDirection;
import com.jobhuntly.backend.entity.enums.TicketStatus;
import com.jobhuntly.backend.repository.TicketMessageRepository;
import com.jobhuntly.backend.repository.TicketRepository;
import com.jobhuntly.backend.util.EmailCanonicalizer;
import com.jobhuntly.backend.util.EmailParser;
import jakarta.mail.*;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;

import static jakarta.mail.Flags.Flag.SEEN;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailPollingService {

    private final TicketRepository ticketRepository;
    private final TicketMessageRepository ticketMessageRepository;

    @Value("${mail.imap.host}")
    private String host;

    @Value("${mail.imap.port:993}")
    private int port;

    @Value("${mail.imap.username}")
    private String username;

    @Value("${mail.imap.password}")
    private String password;

    @Value("${mail.imap.folder:INBOX}")
    private String folder;

    @Value("${mail.imap.peek:true}")
    private boolean peek;

    @Value("${mail.imap.mark-seen:true}")
    private boolean markSeen;

    @Value("${mail.imap.move-to-folder:}")
    private String moveToFolder;

    @Value("${mail.imap.max-per-poll:100}")
    private int maxPerPoll;

    @Value("${mail.imap.poll-interval-ms:30000}")
    private long pollIntervalMs;

    @Value("${mail.imap.sent-folder:}")
    private String sentFolder;


    private static final Set<String> SYSTEM_EMAILS = Set.of(
            "contact.jobhuntly@gmail.com"
    );

    @Scheduled(fixedDelayString = "${mail.imap.poll-interval-ms:30000}")
    public void poll() {
        log.info("Starting IMAP poll");
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.ssl.enable", "true");
        props.put("mail.imaps.port", String.valueOf(port));

        try {
            Session session = Session.getInstance(props);
            try (Store store = session.getStore("imaps")) {
                store.connect(host, port, username, password);

                IMAPFolder inbox = (IMAPFolder) store.getFolder(folder);
                inbox.open(Folder.READ_WRITE);

                int total = inbox.getMessageCount();
                if (total <= 0) {
                    inbox.close(false);
                } else {
                    int to = total;
                    int from = Math.max(1, to - Math.max(1, maxPerPoll) + 1);
                    Message[] messages = inbox.getMessages(from, to);

                    if (peek) {
                        FetchProfile fp = new FetchProfile();
                        fp.add(FetchProfile.Item.ENVELOPE);
                        fp.add(FetchProfile.Item.CONTENT_INFO);
                        fp.add("Message-Id");
                        fp.add("In-Reply-To");
                        fp.add("References");
                        inbox.fetch(messages, fp);
                    }

                    Arrays.sort(messages, Comparator.comparing(m -> {
                        try {
                            return m.getSentDate();
                        } catch (Exception e) {
                            return null;
                        }
                    }, Comparator.nullsFirst(Comparator.naturalOrder())));

                    List<Message> processed = new ArrayList<>();

                    for (Message msg : messages) {
                        try {
                            if (processOne(inbox, msg)) {
                                processed.add(msg);
                            }
                        } catch (Exception ex) {
                            log.error("Failed to process message", ex);
                        }
                    }

                    if (!processed.isEmpty()) {
                        if (markSeen) {
                            for (Message p : processed) {
                                try {
                                    p.setFlag(SEEN, true);
                                } catch (Exception ignored) {
                                }
                            }
                        }
                        if (StringUtils.hasText(moveToFolder)) {
                            moveMessages(inbox, moveToFolder, processed);
                        }
                    }

                    inbox.close(true);
                }
                log.info("IMAP poll completed");
            }
        } catch (Exception e) {
            log.error("IMAP poll error", e);
        }
    }

    private boolean processOne(IMAPFolder inbox, Message m) throws Exception {
        String messageId = extractHeaderFirst(m, "Message-Id");
        if (!StringUtils.hasText(messageId)) {
            log.warn("Skip message without Message-Id");
            return false;
        }
        if (ticketMessageRepository.findByMessageId(messageId).isPresent()) {
            return true;
        }

        String inReplyTo = extractHeaderFirst(m, "In-Reply-To");
        String subject = safeSubject(m);
        String rawFrom = extractFromAddress(m);
        String canonicalFrom = EmailCanonicalizer.canonicalize(rawFrom);

        MessageDirection direction = isSystemAddress(canonicalFrom) ? MessageDirection.OUTBOUND : MessageDirection.INBOUND;

        Ticket ticket = null;
        if (StringUtils.hasText(inReplyTo)) {
            Optional<TicketMessage> replied = ticketMessageRepository.findByMessageId(inReplyTo);
            if (replied.isPresent()) {
                ticket = replied.get().getTicket();
            }
        }

        if (ticket == null) {
            String references = extractHeaderFirst(m, "References");
            String lastRef = extractLastMessageIdFromReferences(references);
            if (StringUtils.hasText(lastRef)) {
                Optional<TicketMessage> repliedByRef = ticketMessageRepository.findByMessageId(lastRef);
                if (repliedByRef.isPresent()) {
                    ticket = repliedByRef.get().getTicket();
                }
            }
        }

        if (ticket == null) {
            ticket = createNewTicketFrom(m, subject, canonicalFrom);
        }

        BodyContent bc = new BodyContent();
        collectBody(m, bc);

        String strippedPlain = EmailParser.extractNewContentPlain(bc.html, bc.text);
        String strippedHtml  = bc.html;

        TicketMessage tm = new TicketMessage();
        tm.setTicket(ticket);
        tm.setMessageId(messageId);
        tm.setInReplyTo(inReplyTo);
        tm.setFromEmail(canonicalFrom != null ? canonicalFrom : rawFrom);
        tm.setSentAt(m.getSentDate() != null ? m.getSentDate().toInstant() : Instant.now());
        tm.setDirection(direction);
        tm.setBodyText(StringUtils.hasText(strippedPlain) ? strippedPlain : bc.text);
        tm.setBodyHtml(StringUtils.hasText(strippedHtml) ? strippedHtml : bc.html);

        ticketMessageRepository.save(tm);

        if (direction == MessageDirection.INBOUND && ticket.getCustomerEmail() == null) {
            ticket.setCustomerEmail(canonicalFrom);
            ticketRepository.save(ticket);
        }

        return true;
    }

    private Ticket createNewTicketFrom(Message m, String subject, String canonicalFrom) throws Exception {
        String threadId = extractHeaderFirst(m, "Message-Id");
        if (!StringUtils.hasText(threadId)) {
            threadId = UUID.randomUUID().toString();
        } else {
            Optional<Ticket> existed = ticketRepository.findByThreadId(threadId);
            if (existed.isPresent()) return existed.get();
        }

        Ticket t = new Ticket();
        t.setSubject(subject);
        t.setFromEmail(canonicalFrom);
        t.setThreadId(threadId);
        t.setStatus(TicketStatus.OPEN);

        if (!isSystemAddress(canonicalFrom)) {
            t.setCustomerEmail(canonicalFrom);
        }
        return ticketRepository.save(t);
    }

    // --- MOVE ---
    private void moveMessages(IMAPFolder fromFolder, String targetFolderName, List<Message> messages) {
        try {
            Folder target = fromFolder.getStore().getFolder(targetFolderName);
            if (!target.exists()) {
                boolean created = target.create(Folder.HOLDS_MESSAGES);
                if (!created) {
                    log.warn("Cannot create folder {}", targetFolderName);
                    return;
                }
            }
            if (!target.isOpen()) target.open(Folder.READ_WRITE);

            fromFolder.copyMessages(messages.toArray(Message[]::new), target);

            for (Message m : messages) {
                try { m.setFlag(Flags.Flag.DELETED, true); } catch (Exception ignored) {}
            }
            fromFolder.expunge();

            target.close(false);
        } catch (Exception e) {
            log.error("Move messages to {} failed", targetFolderName, e);
        }
    }

    private static class BodyContent {
        String text = null;
        String html = null;
    }

    private void collectBody(Part p, BodyContent bc) throws Exception {
        if (p.isMimeType("text/plain")) {
            if (bc.text == null) bc.text = getText(p);
            return;
        }
        if (p.isMimeType("text/html")) {
            if (bc.html == null) bc.html = getText(p);
            return;
        }
        if (p.isMimeType("multipart/*")) {
            Object c = p.getContent();
            if (c instanceof Multipart mp) {
                if (mp instanceof MimeMultipart mmp) {
                    String contentType = mmp.getContentType();
                    ContentType ct = new ContentType(contentType);
                    if ("alternative".equalsIgnoreCase(ct.getSubType())) {
                        for (int i = mp.getCount() - 1; i >= 0; i--) {
                            BodyPart bp = mp.getBodyPart(i);
                            collectBody(bp, bc);
                            if (bc.html != null) return;
                        }
                        return;
                    }
                }

                for (int i = 0; i < mp.getCount(); i++) {
                    BodyPart bp = mp.getBodyPart(i);
                    collectBody(bp, bc);
                }
            }
            return;
        }

        if (p.isMimeType("message/rfc822")) {
            Object c = p.getContent();
            if (c instanceof Message nested) {
                collectBody(nested, bc);
            }
            return;
        }
    }

    private String getText(Part p) throws Exception {
        Object c = p.getContent();
        if (c == null) return null;
        if (c instanceof String s) return s;
        if (c instanceof Multipart mp) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                String sub = getText(bp);
                if (sub != null) sb.append(sub);
            }
            return sb.toString();
        }
        return String.valueOf(c);
    }

    private String extractFromAddress(Message m) {
        try {
            Address[] froms = m.getFrom();
            if (froms != null && froms.length > 0) {
                if (froms[0] instanceof InternetAddress ia) {
                    String addr = ia.getAddress();
                    if (addr != null) return addr;
                    return ia.toUnicodeString();
                }
                return froms[0].toString();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private boolean isSystemAddress(String email) {
        if (!StringUtils.hasText(email)) return false;
        String c = EmailCanonicalizer.canonicalize(email);
        return SYSTEM_EMAILS.contains(c);
    }

    private String safeSubject(Message m) {
        try {
            String s = m.getSubject();
            if (s == null) return "(no subject)";
            s = s.trim();
            return s.isEmpty() ? "(no subject)" : s;
        } catch (Exception e) {
            return "(no subject)";
        }
    }

    private String extractHeaderFirst(Message m, String name) throws MessagingException {
        String[] vals = m.getHeader(name);
        return (vals != null && vals.length > 0) ? vals[0] : null;
    }

    private String extractLastMessageIdFromReferences(String references) {
        if (!StringUtils.hasText(references)) return null;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("<[^>]+>").matcher(references);
        String last = null;
        while (matcher.find()) {
            last = matcher.group();
        }
        return last;
    }

}
