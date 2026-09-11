package com.ecommerce.web.config;

import com.ecommerce.web.service.GatewayClient;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.stereotype.Controller;

@ControllerAdvice(annotations = Controller.class)
public class WebMvcConfig {
    private final GatewayClient gateway;

    public WebMvcConfig(GatewayClient gateway) {
        this.gateway = gateway;
    }

    @ModelAttribute("currentUser")
    public Object currentUser(HttpSession session) {
        return gateway.user(session);
    }

    @ModelAttribute("authenticated")
    public boolean authenticated(HttpSession session) {
        return gateway.authenticated(session);
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(HttpSession session) {
        return gateway.admin(session);
    }
}
