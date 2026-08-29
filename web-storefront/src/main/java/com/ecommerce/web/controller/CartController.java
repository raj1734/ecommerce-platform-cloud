package com.ecommerce.web.controller;

import com.ecommerce.web.service.GatewayClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class CartController {
    private final GatewayClient gateway;

    public CartController(GatewayClient gateway) {
        this.gateway = gateway;
    }

    @GetMapping("/cart")
    public String cart(HttpSession session, Model model) {
        if (!gateway.authenticated(session)) return "redirect:/login";
        JsonNode cart = gateway.get("/api/v1/cart", session);
        model.addAttribute("cart", cart);
        model.addAttribute("items", enrichCartItems(gateway.list(cart), session));
        return "cart";
    }

    private java.util.List<JsonNode> enrichCartItems(java.util.List<JsonNode> items, HttpSession session) {
        for (JsonNode item : items) {
            if (!(item instanceof ObjectNode obj)) continue;

            String productId = item.hasNonNull("productId") ? item.get("productId").asText() : "";
            if (productId.isBlank()) continue;

            JsonNode product = gateway.get("/api/v1/products/" + productId, session);
            if (product == null || product.isMissingNode() || product.has("error")) continue;

            if (!obj.hasNonNull("productName") && product.hasNonNull("name")) {
                obj.set("productName", product.get("name"));
            }
            if (product.hasNonNull("name")) obj.set("productName", product.get("name"));
            if (product.hasNonNull("brand")) obj.set("productBrand", product.get("brand"));
            if (product.hasNonNull("category")) obj.set("productCategory", product.get("category"));
            if (product.has("imageUrls") && product.get("imageUrls").isArray() && product.get("imageUrls").size() > 0) {
                obj.put("productImageUrl", product.get("imageUrls").get(0).asText());
            }
            if (product.hasNonNull("description")) obj.set("productDescription", product.get("description"));
        }
        return items;
    }

    @PostMapping("/cart/items/{itemId}")
    public String update(@PathVariable String itemId,
                         @RequestParam int quantity,
                         HttpSession session) {
        gateway.patch("/api/v1/cart/items/" + itemId,
                Map.of("quantity", quantity), session);
        return "redirect:/cart";
    }

    @PostMapping("/cart/items/{itemId}/delete")
    public String delete(@PathVariable String itemId, HttpSession session) {
        gateway.delete("/api/v1/cart/items/" + itemId, session);
        return "redirect:/cart";
    }
}
