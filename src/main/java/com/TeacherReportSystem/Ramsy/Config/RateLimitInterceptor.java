package com.TeacherReportSystem.Ramsy.Config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RateLimitInterceptor implements HandlerInterceptor {
    private static final int MAX_REQUESTS = 5; // Max requests
    private static final long TIME_WINDOW = 15 * 60 * 1000; // 15 minutes in milliseconds
    
    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = getClientIp(request);
        String endpoint = request.getRequestURI();
        String key = ip + ":" + endpoint;
        
        RequestCounter counter = requestCounts.computeIfAbsent(key, k -> new RequestCounter());
        
        synchronized (counter) {
            long currentTime = System.currentTimeMillis();
            
            // Reset counter if time window has passed
            if (currentTime - counter.getFirstRequestTime() > TIME_WINDOW) {
                counter.reset(currentTime);
            }
            
            // Check if rate limit exceeded
            if (counter.getCount() >= MAX_REQUESTS) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Rate limit exceeded. Please try again later.");
                return false;
            }
            
            // Increment request count
            counter.increment();
        }
        
        return true;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            // If multiple IPs are in the header, take the first one
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    private static class RequestCounter {
        private int count;
        private long firstRequestTime;
        
        public RequestCounter() {
            this.firstRequestTime = System.currentTimeMillis();
            this.count = 1;
        }
        
        public synchronized void increment() {
            count++;
        }
        
        public synchronized int getCount() {
            return count;
        }
        
        public synchronized long getFirstRequestTime() {
            return firstRequestTime;
        }
        
        public synchronized void reset(long currentTime) {
            this.firstRequestTime = currentTime;
            this.count = 1;
        }
    }
}
