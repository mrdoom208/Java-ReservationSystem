package com.mycompany.reservationsystem.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.*;
import java.util.UUID;

@Configuration
public class CorsConfig {

    @Value("${app.client.identifier:staff-app-01}")
    private String clientIdentifier;

    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> loggingFilter() {
        FilterRegistrationBean<RequestLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestLoggingFilter());
        registrationBean.addUrlPatterns("/api/*");
        return registrationBean;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("https://reservation-system-frontend.onrender.com", "http://localhost:3000", "http://localhost:8080")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("Content-Type", "Accept", "Authorization", "X-Client-Identifier")
                        .exposedHeaders("X-Client-Identifier");
            }
        };
    }

    public static class RequestLoggingFilter implements Filter {
        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
                throws IOException, ServletException {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;

            CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);

            String clientId = request.getHeader("X-Client-Identifier");
            String clientDisplay = (clientId != null && !clientId.isEmpty()) ? clientId : "UNKNOWN";

            System.out.println("=== REQUEST [" + UUID.randomUUID().toString().substring(0, 8) + "] ===");
            System.out.println("Client-ID: " + clientDisplay);
            System.out.println("URL: " + request.getRequestURL());
            System.out.println("Method: " + request.getMethod());
            System.out.println("Headers:");
            java.util.Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String header = headerNames.nextElement();
                System.out.println("  " + header + ": " + request.getHeader(header));
            }
            System.out.println("Body: " + cachedRequest.getCachedBody());
            System.out.println("==========================================");

            response.setHeader("X-Client-Identifier", clientDisplay);

            chain.doFilter(cachedRequest, response);

            System.out.println("=== RESPONSE ===");
            System.out.println("Status: " + response.getStatus());
            System.out.println("===============");
        }
    }

    public static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private final String cachedBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            this.cachedBody = readBody(request);
        }

        private String readBody(HttpServletRequest request) throws IOException {
            try {
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = request.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            } catch (IllegalStateException e) {
                InputStream inputStream = request.getInputStream();
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                return result.toString();
            }
        }

        public String getCachedBody() {
            return cachedBody;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedBody.getBytes());
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return byteArrayInputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {}

                @Override
                public int read() {
                    return byteArrayInputStream.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream()));
        }
    }
}
