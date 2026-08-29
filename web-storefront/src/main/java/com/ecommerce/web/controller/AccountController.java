package com.ecommerce.web.controller;

import com.ecommerce.web.service.GatewayClient;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class AccountController {
    private final GatewayClient gateway;

    public AccountController(GatewayClient gateway) {
        this.gateway = gateway;
    }

    @GetMapping("/account")
    public String account(HttpSession session, Model model) {
        if (!gateway.authenticated(session)) return "redirect:/login";
        model.addAttribute("profile", gateway.get("/api/v1/users/me", session));
        model.addAttribute("addresses", gateway.list(gateway.get("/api/v1/users/me/addresses", session)));
        return "account";
    }

    @PostMapping("/account/profile")
    public String updateProfile(@RequestParam String firstName,
                                @RequestParam(required = false, defaultValue = "") String lastName,
                                @RequestParam(required = false, defaultValue = "") String phone,
                                HttpSession session) {
        gateway.put("/api/v1/users/me",
                Map.of("firstName", firstName, "lastName", lastName, "phone", phone), session);
        return "redirect:/account";
    }

    @PostMapping("/account/address")
    public String addAddress(@RequestParam String addressLine1,
                             @RequestParam(required = false, defaultValue = "") String addressLine2,
                             @RequestParam String city,
                             @RequestParam String state,
                             @RequestParam String postalCode,
                             @RequestParam String country,
                             @RequestParam(defaultValue = "false") boolean isDefault,
                             HttpSession session) {
        gateway.post("/api/v1/users/me/addresses",
                Map.of("addressLine1", addressLine1, "addressLine2", addressLine2,
                        "city", city, "state", state, "postalCode", postalCode,
                        "country", country, "isDefault", isDefault), session);
        return "redirect:/account";
    }

    @PostMapping("/account/preferences")
    public String preferences(@RequestParam Map<String,String> prefs, HttpSession session) {
        gateway.put("/api/v1/users/me/preferences", prefs, session);
        return "redirect:/account";
    }
}
