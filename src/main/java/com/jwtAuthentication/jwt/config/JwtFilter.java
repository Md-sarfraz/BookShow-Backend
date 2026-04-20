package com.jwtAuthentication.jwt.config;

import com.jwtAuthentication.jwt.service.JwtService;
import com.jwtAuthentication.jwt.service.MyUserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private ApplicationContext applicationContext;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader=request.getHeader("Authorization");
        String token=null;
        String userName=null;
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            token=authHeader.substring(7).trim();
            try {
                userName=jwtService.extractUserName(token);
            } catch (Exception e) {
                // Token is expired or invalid — continue as unauthenticated.
                // Public endpoints will still be accessible normally.
            }
        }

        if(userName != null && SecurityContextHolder.getContext().getAuthentication()==null){
            try {
                UserDetails userDetails = applicationContext.getBean(MyUserDetailService.class).loadUserByUsername(userName);

                if(jwtService.validateToken(token,userDetails)){
                    UsernamePasswordAuthenticationToken authToken=
                            new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (UsernameNotFoundException ex) {
                // Token may belong to a deleted user; keep request unauthenticated instead of throwing 500.
                SecurityContextHolder.clearContext();
                log.warn("JWT subject not found in DB: {}", userName);
            } catch (Exception ex) {
                // Any token/auth error should not break public endpoints.
                SecurityContextHolder.clearContext();
                log.warn("JWT authentication skipped due to error: {}", ex.getMessage());
            }
        }
        filterChain.doFilter(request,response);
    }
}
