package com.ecommerce.web.controller;

import com.ecommerce.web.service.GatewayClient;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class ProductController {
    private final GatewayClient gateway;

    public ProductController(GatewayClient gateway) {
        this.gateway = gateway;
    }

    @GetMapping("/products")
    public String products(@RequestParam(required = false) String q,
                           @RequestParam(required = false) String category,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size,
                           HttpSession session,
                           Model model) {
        StringBuilder path = new StringBuilder("/api/v1/products?page=").append(page).append("&size=").append(size);
        if (q != null && !q.isBlank()) path.append("&q=").append(java.net.URLEncoder.encode(q, java.nio.charset.StandardCharsets.UTF_8));
        if (category != null && !category.isBlank()) path.append("&category=").append(java.net.URLEncoder.encode(category, java.nio.charset.StandardCharsets.UTF_8));

        JsonNode response = gateway.get(path.toString(), session);
        model.addAttribute("products", gateway.list(response));
        model.addAttribute("rawPage", response);
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("category", category == null ? "" : category);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "products/list";
    }

    @GetMapping("/products/{productId}")
    public String product(@PathVariable String productId, HttpSession session, Model model) {
        JsonNode response = gateway.get("/api/v1/products/" + productId, session);
        model.addAttribute("product", response);
        return "products/detail";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam String productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session) {
        if (!gateway.authenticated(session)) return "redirect:/login";
        gateway.post("/api/v1/cart/items",
                Map.of("productId", productId, "quantity", quantity), session);
        return "redirect:/cart";
    }
}
