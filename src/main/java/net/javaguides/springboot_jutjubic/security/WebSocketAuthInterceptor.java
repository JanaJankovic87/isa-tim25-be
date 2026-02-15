package net.javaguides.springboot_jutjubic.security;

import net.javaguides.springboot_jutjubic.util.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token == null || token.isEmpty()) {
                token = accessor.getFirstNativeHeader("token");
            }

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            logger.info("WS AUTH Token: {}", token != null ? "present" : "missing");

            if (token != null) {
                try {
                    String username = tokenUtils.getUsernameFromToken(token);
                    logger.info(" WS AUTH Username from token: {}", username);

                    if (username != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        if (tokenUtils.validateToken(token, userDetails)) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            accessor.setUser(authentication);
                            logger.info("WS AUTH Authenticated: {}", username);
                        }
                    }
                } catch (Exception e) {
                    logger.error("WS AUTH Error: {}", e.getMessage());
                }
            }
        }

        return message;
    }
}