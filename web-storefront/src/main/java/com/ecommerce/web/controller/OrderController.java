package com.ecommerce.web.controller;

import com.ecommerce.web.service.GatewayClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.UUID;

@Controller
public class OrderController {
    private final GatewayClient gateway;

    public OrderController(GatewayClient gateway) {
        this.gateway = gateway;
    }

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        if (!gateway.authenticated(session)) return "redirect:/login";
        JsonNode cart = gateway.get("/api/v1/cart", session);
        var addresses = gateway.list(gateway.get("/api/v1/users/me/addresses", session));
        model.addAttribute("cart", cart);
        model.addAttribute("items", gateway.list(cart));
        model.addAttribute("addresses", addresses);
        return "checkout";
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam String cartId,
                           @RequestParam String shippingAddressId,
                           @RequestParam String paymentMethod,
                           HttpSession session,
                           Model model) {
        if (!gateway.authenticated(session)) return "redirect:/login";

        JsonNode result = gateway.post(
                "/api/v1/orders/checkout",
                Map.of("cartId", cartId,
                        "shippingAddressId", shippingAddressId,
                        "paymentMethod", paymentMethod),
                session,
                UUID.randomUUID().toString());

        if (result.has("error") || (result.has("status") && result.get("status").asInt() >= 400)) {
            model.addAttribute("error", gateway.text(result, "message").isBlank()
                    ? gateway.text(result, "error") : gateway.text(result, "message"));
            model.addAttribute("result", result);
            return checkout(session, model);
        }

        model.addAttribute("order", result);
        return "orders/success";
    }

    @GetMapping("/orders")
    public String orders(HttpSession session, Model model) {
        if (!gateway.authenticated(session)) return "redirect:/login";
        JsonNode result = gateway.get("/api/v1/orders", session);
        model.addAttribute("orders", enrichOrders(gateway.list(result), session));
        return "orders/list";
    }

    @GetMapping("/orders/{orderId}")
    public String order(@PathVariable String orderId,
                        HttpSession session,
                        Model model) {

        if (!gateway.authenticated(session)) {
            return "redirect:/login";
        }

        JsonNode result =
                gateway.get("/api/v1/orders/" + orderId, session);

        model.addAttribute("order", result);

        model.addAttribute(
                "items",
                gateway.list(result.get("items"))
        );

        model.addAttribute("orderIdNode", result.get("orderId"));
        model.addAttribute("orderNumberNode", result.get("orderNumber"));
        model.addAttribute("statusNode", result.get("status"));
        model.addAttribute("paymentStatusNode", result.get("paymentStatus"));
        model.addAttribute("totalAmountNode", result.get("totalAmount"));
        model.addAttribute("currencyNode", result.get("currency"));

        return "orders/detail";
    }


    private java.util.List<JsonNode> enrichOrders(java.util.List<JsonNode> orders, HttpSession session) {
        for (JsonNode order : orders) {
            if (!(order instanceof ObjectNode obj)) continue;
            java.util.List<JsonNode> items = gateway.list(order.has("items") ? order.get("items") : null);
            enrichOrderItems(items, session);
            if (!items.isEmpty()) {
                obj.set("displayItems", gatewayMapperArray(items));
            }
        }
        return orders;
    }

    private java.util.List<JsonNode> enrichOrderItems(java.util.List<JsonNode> items, HttpSession session) {
        for (JsonNode item : items) {
            if (!(item instanceof ObjectNode obj)) continue;
            String productId = item.hasNonNull("productId") ? item.get("productId").asText() : "";
            if (productId.isBlank()) continue;

            JsonNode product = gateway.get("/api/v1/products/" + productId, session);
            if (product == null || product.isMissingNode() || product.has("error")) continue;

            if (product.hasNonNull("name")) obj.set("productName", product.get("name"));
            if (product.hasNonNull("brand")) obj.set("productBrand", product.get("brand"));
            if (product.hasNonNull("category")) obj.set("productCategory", product.get("category"));
            if (product.has("imageUrls") && product.get("imageUrls").isArray() && product.get("imageUrls").size() > 0) {
                obj.put("productImageUrl", product.get("imageUrls").get(0).asText());
            }
        }
        return items;
    }

    private com.fasterxml.jackson.databind.node.ArrayNode gatewayMapperArray(java.util.List<JsonNode> items) {
        com.fasterxml.jackson.databind.node.ArrayNode array = new com.fasterxml.jackson.databind.ObjectMapper().createArrayNode();
        items.forEach(array::add);
        return array;
    }

    @PostMapping("/orders/{orderId}/cancel")
    public String cancel(@PathVariable String orderId,
                         HttpSession session,
                         org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (!gateway.authenticated(session)) {
            return "redirect:/login";
        }

        // The Order Service cancel endpoint does not require a request body.
        // It identifies the order from the path and the user from X-User-Id.
        JsonNode result = gateway.post(
                "/api/v1/orders/" + orderId + "/cancel",
                null,
                session,
                "cancel-" + orderId + "-" + UUID.randomUUID()
        );

        int status = result.has("status") ? result.get("status").asInt(200) : 200;
        boolean failed = result.has("error")
                || (result.has("success") && !result.get("success").asBoolean(true))
                || status >= 400;

        if (failed) {
            String message = gateway.text(result, "message");
            if (message.isBlank()) {
                message = gateway.text(result, "error");
            }
            if (message.isBlank()) {
                message = "Unable to cancel the order.";
            }
            redirectAttributes.addFlashAttribute("cancelError", message);
        } else {
            redirectAttributes.addFlashAttribute("cancelSuccess", "Order cancelled successfully.");
        }

        return "redirect:/orders/" + orderId;
    }
}
