package com.berke.urlshortener.filter;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;

@Component
@Order(1) 
public class RateLimitFilter implements Filter {

    private final ProxyManager<String> proxyManager;

    public RateLimitFilter(ProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String uri = request.getRequestURI();
        String method = request.getMethod();
        String ip = request.getRemoteAddr();

        
        if (uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs") || uri.startsWith("/h2-console")) {
            filterChain.doFilter(request, response);
            return;
        }

        
        String key;
        long limit;

        if ("POST".equalsIgnoreCase(method) && uri.contains("/api/v1/urls")) {
            key = "WRITE:" + ip;
            limit = 10; 
        } else {
            key = "READ:" + ip;
            limit = 100; 
        }

        
        Supplier<BucketConfiguration> configSupplier = () -> BucketConfiguration.builder()
                .addLimit(limitBuilder -> limitBuilder
                        .capacity(limit)
                        .refillGreedy(limit, Duration.ofMinutes(1))
                )
                .build();

        
        Bucket bucket = proxyManager.builder().build(key, configSupplier);

       
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"status\": 429, \"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded. Please wait. (Limit: " + limit + " req/min)\"}"
            );
        }
    }
}