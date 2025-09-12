package com.jobhuntly.backend.websocket;

import com.jobhuntly.backend.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class WsSubscribeGuard implements ChannelInterceptor {
    private final FollowRepository followRepo;
    private static final Pattern COMPANY_TOPIC = Pattern.compile("^/topic/company\\.(\\d+)$");

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var acc = StompHeaderAccessor.wrap(message);

        if (StompCommand.SUBSCRIBE.equals(acc.getCommand())) {
            String dest = acc.getDestination();
            var principal = acc.getUser();
            if (dest == null || principal == null) return null;

            Matcher m = COMPANY_TOPIC.matcher(dest);
            if (m.matches()) {
                Long companyId = Long.valueOf(m.group(1));
                Long userId = Long.valueOf(principal.getName());
                if (!followRepo.existsByUserIdAndCompanyId(userId, companyId)) {
                    return null;
                }
            }
        }
        return message;
    }

}
