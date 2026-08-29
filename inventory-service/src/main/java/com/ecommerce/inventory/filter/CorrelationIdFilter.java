package com.ecommerce.inventory.filter;

import jakarta.servlet.FilterChain; import jakarta.servlet.ServletException; import jakarta.servlet.http.HttpServletRequest; import jakarta.servlet.http.HttpServletResponse; import org.slf4j.MDC; import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter; import java.io.IOException; import java.util.UUID;
@Component public class CorrelationIdFilter extends OncePerRequestFilter {
 protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {String id=request.getHeader("X-Correlation-ID"); if(id==null||id.isBlank()) id=UUID.randomUUID().toString(); response.setHeader("X-Correlation-ID",id); try(MDC.MDCCloseable c=MDC.putCloseable("correlationId",id)){chain.doFilter(request,response);} }
}
