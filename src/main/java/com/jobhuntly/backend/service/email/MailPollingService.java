package com.jobhuntly.backend.service.email;

import com.jobhuntly.backend.repository.TicketMessageRepository;
import jakarta.annotation.PreDestroy;
import jakarta.mail.*;
import jakarta.mail.Flags.Flag;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.OrTerm;
import jakarta.mail.search.ReceivedDateTerm;
import jakarta.mail.search.SearchTerm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailPollingService {
    private final AtomicBoolean polling = new AtomicBoolean(false);
    private final ImapProperties imapProperties;
    private final MailInboxService mailInboxService;
    private final TicketMessageRepository ticketMessageRepository;

    private volatile Store cachedStore;

    public int pollNow() {
        if (!polling.compareAndSet(false, true)) {
            log.info("Poll is already running; skip.");
            return 0;
        }
        try {
            log.info("Starting IMAP poll (manual)");
            int processed = pollInternal(true);
            log.info("Manual IMAP poll finished, processed: {}", processed);
            return processed;
        } finally {
            polling.set(false);
        }
    }

    @Scheduled(fixedDelayString = "${mail.poll.fixed-delay-ms:30000}")
    public void scheduledPoll() {
        if (!polling.compareAndSet(false, true)) {
            log.info("Poll is already running; skip scheduled run.");
            return;
        }
        try {
            log.info("Starting IMAP poll (scheduled)");
            int processed = pollInternal(false);
            log.info("Scheduled IMAP poll finished, processed: {}", processed);
        } finally {
            polling.set(false);
        }
    }

    private int pollInternal(boolean manual) {
        int processed = 0;
        Store store = null;
        Folder inbox = null;
        try {
            store = connect();
            inbox = store.getFolder(imapProperties.getFolder());
            if (inbox == null) {
                log.warn("IMAP folder '{}' not found", imapProperties.getFolder());
                return 0;
            }
            inbox.open(Folder.READ_WRITE);

            Message[] messages = searchMessages(inbox, manual);
            log.info("Found {} message(s) to consider", messages.length);

            for (Message m : messages) {
                if (!(m instanceof MimeMessage mm)) continue;

                String mid = ensureAngle(mm.getMessageID());
                if (StringUtils.hasText(mid) && ticketMessageRepository.findByMessageId(mid).isPresent()) {
                    markSeenIfNeeded(m);
                    continue;
                }

                try {
                    mailInboxService.saveInboundMimeMessage(mm);
                    processed++;
                    markSeenIfNeeded(m);
                } catch (Exception ex) {
                    log.error("Failed to ingest message-id={}", mid, ex);
                }
            }

        } catch (Exception e) {
            log.error("IMAP poll error", e);
        } finally {
            safeClose(inbox);
            safeClose(store);
        }
        return processed;
    }

    private Store connect() throws Exception {
        if (cachedStore != null && cachedStore.isConnected()) return cachedStore;

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", imapProperties.getHost());
        props.put("mail.imaps.port", String.valueOf(imapProperties.getPort()));
        props.put("mail.imaps.ssl.enable", "true");

        Session session = Session.getInstance(props);
        Store store = session.getStore("imaps");
        store.connect(imapProperties.getHost(), imapProperties.getUsername(), imapProperties.getPassword());
        this.cachedStore = store;
        return store;
    }

    private Message[] searchMessages(Folder inbox, boolean manual) throws Exception {
        Message[] unseen = inbox.search(new FlagTerm(new Flags(Flag.SEEN), false));
        if (unseen != null && unseen.length > 0) return unseen;

        if (!manual) return new Message[0];

        ZonedDateTime since = Instant.now().minusSeconds(24 * 3600).atZone(ZoneId.systemDefault());
        SearchTerm recentTerm = new OrTerm(
                new FlagTerm(new Flags(Flag.RECENT), true),
                new ReceivedDateTerm(ReceivedDateTerm.GT, Date.from(since.toInstant()))
        );
        Message[] recent = inbox.search(recentTerm);
        return recent != null ? recent : new Message[0];
    }

    private void markSeenIfNeeded(Message m) {
        try {
            if (!m.isSet(Flag.SEEN)) {
                m.setFlag(Flag.SEEN, true);
            }
        } catch (Exception e) {
            log.warn("Failed to mark SEEN", e);
        }
    }

    private static void safeClose(Folder folder) {
        if (folder == null) return;
        try {
            if (folder.isOpen()) folder.close(false);
        } catch (Exception ignored) {}
    }

    private void safeClose(Store store) {
        if (store != null) {
            try { store.close(); } catch (Exception ignore) {}
        }
    }

    @PreDestroy
    public void closeStore() {
        if (cachedStore != null) {
            try { cachedStore.close(); } catch (Exception ignored) {}
        }
    }

    private static String ensureAngle(String v) {
        if (v == null || v.isBlank()) return null;
        v = v.trim();
        if (!v.startsWith("<")) v = "<" + v;
        if (!v.endsWith(">"))   v = v + ">";
        return v;
    }
}
