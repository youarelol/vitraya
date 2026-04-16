package com.vitraya.adjudication.engine.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TxnIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String txnId = request.getHeader("X-TXN-ID");
        if (txnId == null || txnId.isBlank()) {
            txnId = UUID.randomUUID().toString();
        }

        MDC.put("txnId", txnId);
        response.setHeader("X-TXN-ID", txnId); // Optional: return it to the client

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("txnId"); // Clean up after request
        }
    }
}
