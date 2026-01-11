package net.javaguides.springboot_jutjubic.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import net.javaguides.springboot_jutjubic.util.TokenUtils;

public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private TokenUtils tokenUtils;
    private UserDetailsService userDetailsService;

    protected final Log LOGGER = LogFactory.getLog(getClass());

    public TokenAuthenticationFilter(TokenUtils tokenHelper, UserDetailsService userDetailsService) {
        this.tokenUtils = tokenHelper;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws IOException, ServletException {

        String authToken = tokenUtils.getToken(request);

        try {
            if (authToken != null) {
                String username = tokenUtils.getUsernameFromToken(authToken);

                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (tokenUtils.validateToken(authToken, userDetails)) {
                        TokenBasedAuthentication authentication =
                                new TokenBasedAuthentication(userDetails);
                        authentication.setToken(authToken);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }

            chain.doFilter(request, response);

        } catch (ExpiredJwtException e) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"JWT_EXPIRED\"}");

        } catch (Exception e) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"INVALID_TOKEN\"}");
        }
    }

}
