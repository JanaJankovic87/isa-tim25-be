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

        System.out.println("=== TOKEN FILTER === URL: " + request.getRequestURI());
        System.out.println("=== TOKEN FILTER === Token: " + authToken);

        try {
            if (authToken != null) {
                String username = tokenUtils.getUsernameFromToken(authToken);
                System.out.println("=== TOKEN FILTER === Username: " + username);

                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    System.out.println("=== TOKEN FILTER === UserDetails: " + userDetails);

                    boolean valid = tokenUtils.validateToken(authToken, userDetails);
                    System.out.println("=== TOKEN FILTER === Valid: " + valid);

                    if (valid) {
                        TokenBasedAuthentication authentication =
                                new TokenBasedAuthentication(userDetails);
                        authentication.setToken(authToken);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }

            chain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            System.out.println("=== TOKEN FILTER === EXPIRED: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"JWT_EXPIRED\"}");

        } catch (Exception e) {
            System.out.println("=== TOKEN FILTER === EXCEPTION: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"INVALID_TOKEN\"}");
        }
    }

}
