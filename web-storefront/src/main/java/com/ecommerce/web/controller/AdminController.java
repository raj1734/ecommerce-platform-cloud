package com.ecommerce.web.controller;

import com.ecommerce.web.service.GatewayClient;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final GatewayClient gateway;

    public AdminController(GatewayClient gateway) {
        this.gateway = gateway;
    }

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        if (!gateway.admin(session)) return "redirect:/products";

        JsonNode productsResponse = gateway.get("/api/v1/products?page=0&size=100", session);
        List<JsonNode> products = gateway.list(productsResponse);
        JsonNode inventoryResponse = gateway.get("/api/v1/inventory", session);
        List<JsonNode> inventory = gateway.list(inventoryResponse);

        Map<String, JsonNode> productsById = new HashMap<>();
        for (JsonNode product : products) {
            String id = productId(product);
            if (!id.isBlank()) productsById.put(id, product);
        }

        List<Map<String, Object>> inventoryRows = new ArrayList<>();
        int totalUnits = 0;
        int reservedUnits = 0;
        int lowStock = 0;
        int outOfStock = 0;

        for (JsonNode item : inventory) {
            String productId = gateway.text(item, "productId");
            JsonNode product = productsById.get(productId);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("inventory", item);
            row.put("product", product);
            inventoryRows.add(row);

            int available = item.path("availableQuantity").asInt(0);
            int reserved = item.path("reservedQuantity").asInt(0);
            totalUnits += available + reserved;
            reservedUnits += reserved;
            String status = item.path("status").asText("");
            if ("LOW_STOCK".equalsIgnoreCase(status)) lowStock++;
            if ("OUT_OF_STOCK".equalsIgnoreCase(status)) outOfStock++;
        }

        model.addAttribute("products", products);
        model.addAttribute("inventoryRows", inventoryRows);
        model.addAttribute("totalUnits", totalUnits);
        model.addAttribute("reservedUnits", reservedUnits);
        model.addAttribute("lowStock", lowStock);
        model.addAttribute("outOfStock", outOfStock);
        return "admin/dashboard";
    }

    @GetMapping("/products/new")
    public String newProduct(HttpSession session) {
        if (!gateway.admin(session)) return "redirect:/products";
        return "admin/product-form";
    }

    @PostMapping("/products")
    public String create(@RequestParam String sku,
                         @RequestParam String name,
                         @RequestParam String category,
                         @RequestParam(required = false, defaultValue = "") String description,
                         @RequestParam(required = false, defaultValue = "") String brand,
                         @RequestParam BigDecimal amount,
                         @RequestParam(defaultValue = "USD") String currency,
                         @RequestParam(required = false, defaultValue = "") String imageUrls,
                         HttpSession session) {
        if (!gateway.admin(session)) return "redirect:/products";
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("sku", sku);
        body.put("name", name);
        body.put("category", category);
        body.put("description", description);
        body.put("brand", brand);
        body.put("price", Map.of("amount", amount, "currency", currency));
        body.put("attributes", Map.of());
        body.put("imageUrls", parseImageUrls(imageUrls));
        gateway.post("/api/v1/products", body, session);
        return "redirect:/admin";
    }

    @GetMapping("/products/{productId}/edit")
    public String edit(@PathVariable String productId, HttpSession session, Model model) {
        if (!gateway.admin(session)) return "redirect:/products";
        JsonNode product = gateway.get("/api/v1/products/" + productId, session);
        model.addAttribute("product", product);
        model.addAttribute("productId", productId);
        model.addAttribute("imageUrlsText", imageUrlsText(product));
        return "admin/product-form";
    }

    @PostMapping("/products/{productId}")
    public String update(@PathVariable String productId,
                         @RequestParam String sku,
                         @RequestParam String name,
                         @RequestParam String category,
                         @RequestParam(required = false, defaultValue = "") String description,
                         @RequestParam(required = false, defaultValue = "") String brand,
                         @RequestParam BigDecimal amount,
                         @RequestParam(defaultValue = "USD") String currency,
                         @RequestParam(required = false, defaultValue = "") String imageUrls,
                         HttpSession session) {
        if (!gateway.admin(session)) return "redirect:/products";
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("sku", sku);
        body.put("name", name);
        body.put("category", category);
        body.put("description", description);
        body.put("brand", brand);
        body.put("price", Map.of("amount", amount, "currency", currency));
        body.put("attributes", Map.of());
        body.put("imageUrls", parseImageUrls(imageUrls));
        gateway.put("/api/v1/products/" + productId, body, session);
        return "redirect:/admin";
    }

    @PostMapping("/products/{productId}/status")
    public String status(@PathVariable String productId,
                          @RequestParam String status,
                          HttpSession session) {
        if (gateway.admin(session)) {
            gateway.patch("/api/v1/products/" + productId + "/status", Map.of("status", status), session);
        }
        return "redirect:/admin";
    }

    @GetMapping("/inventory/{productId}")
    public String inventory(@PathVariable String productId, HttpSession session, Model model) {
        if (!gateway.admin(session)) return "redirect:/products";
        JsonNode inventory = gateway.get("/api/v1/inventory/" + productId, session);
        JsonNode product = gateway.get("/api/v1/products/" + productId, session);
        JsonNode history = gateway.get("/api/v1/inventory/" + productId + "/history", session);
        model.addAttribute("productId", productId);
        model.addAttribute("inventory", inventory);
        model.addAttribute("product", product);
        model.addAttribute("history", gateway.list(history));
        return "admin/inventory";
    }

    @PostMapping("/inventory/{productId}/adjust")
    public String adjustInventory(@PathVariable String productId,
                                   @RequestParam String adjustmentType,
                                   @RequestParam int quantity,
                                   @RequestParam String reason,
                                   HttpSession session) {
        if (gateway.admin(session)) {
            Map<String, Object> body = Map.of(
                    "adjustmentType", adjustmentType,
                    "quantity", quantity,
                    "reason", reason
            );
            gateway.post("/api/v1/inventory/" + productId + "/adjustments", body, session);
        }
        return "redirect:/admin/inventory/" + productId;
    }

    private String productId(JsonNode product) {
        if (product == null) return "";
        if (product.hasNonNull("productId")) return product.get("productId").asText();
        return product.hasNonNull("id") ? product.get("id").asText() : "";
    }

    private String imageUrlsText(JsonNode product) {
        if (product == null || !product.has("imageUrls") || !product.get("imageUrls").isArray()) return "";
        List<String> urls = new ArrayList<>();
        product.get("imageUrls").forEach(node -> {
            if (node != null && !node.isNull() && !node.asText().isBlank()) urls.add(node.asText());
        });
        return String.join(System.lineSeparator(), urls);
    }

    private List<String> parseImageUrls(String imageUrls) {
        if (imageUrls == null || imageUrls.isBlank()) return List.of();
        return Arrays.stream(imageUrls.split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
