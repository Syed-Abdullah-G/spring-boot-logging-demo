package com.example.loggingdemo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Wraps every HTTP request. It puts a short request id into the MDC (so every log line written while
 * handling the request carries it), and logs one summary line with status and duration at the end.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String REQUEST_ID_KEY = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(REQUEST_ID_KEY, requestId);
        response.setHeader("X-Request-Id", requestId);

        long start = System.currentTimeMillis();
        log.debug("--> {} {}", request.getMethod(), request.getRequestURI());
        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("<-- {} {} status={} duration={}ms",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
            // Always clear the MDC: server threads are reused for other requests.
            MDC.remove(REQUEST_ID_KEY);
        }
    }
}
