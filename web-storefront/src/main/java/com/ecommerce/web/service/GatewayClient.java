package com.ecommerce.web.service;

import com.ecommerce.web.model.SessionUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.*;

@Service
public class GatewayClient {
    public static final String AUTH_USER = "AUTH_USER";
    public static final String CORRELATION_ID = "CORRELATION_ID";

    private final RestClient client;
    private final ObjectMapper mapper;

    public GatewayClient(RestClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public SessionUser user(HttpSession session) {
        return (SessionUser) session.getAttribute(AUTH_USER);
    }

    public boolean authenticated(HttpSession session) {
        return user(session) != null;
    }

    public boolean admin(HttpSession session) {
        SessionUser u = user(session);
        return u != null && u.admin();
    }

    public JsonNode get(String path, HttpSession session) {
        return request("GET", path, null, session, null);
    }

    public JsonNode post(String path, Object body, HttpSession session) {
        return request("POST", path, body, session, null);
    }

    public JsonNode post(String path, Object body, HttpSession session, String idempotencyKey) {
        return request("POST", path, body, session, idempotencyKey);
    }

    public JsonNode put(String path, Object body, HttpSession session) {
        return request("PUT", path, body, session, null);
    }

    public JsonNode patch(String path, Object body, HttpSession session) {
        return request("PATCH", path, body, session, null);
    }

    public JsonNode delete(String path, HttpSession session) {
        return request("DELETE", path, null, session, null);
    }

    private JsonNode request(String method, String path, Object body,
                             HttpSession session, String idempotencyKey) {
        try {
            String correlationId = correlationId(session);
            SessionUser u = user(session);

            if (body != null) {
                RestClient.RequestBodySpec req = switch (method) {
                    case "POST" -> client.post().uri(path);
                    case "PUT" -> client.put().uri(path);
                    case "PATCH" -> client.patch().uri(path);
                    default -> throw new IllegalArgumentException("Body not supported for " + method);
                };

                req.contentType(MediaType.APPLICATION_JSON)
                        .header("X-Correlation-ID", correlationId);
                addAuth(req, u);
                addIdempotency(req, idempotencyKey);
                req.body(body);
                return parse(req.retrieve().body(String.class));
            }

            RestClient.RequestHeadersSpec<?> req = switch (method) {
                case "GET" -> client.get().uri(path);
                case "POST" -> client.post().uri(path);
                case "DELETE" -> client.delete().uri(path);
                default -> throw new IllegalArgumentException("Unsupported method " + method);
            };

            req.header("X-Correlation-ID", correlationId);
            addAuth(req, u);
            addIdempotency(req, idempotencyKey);
            return parse(req.retrieve().body(String.class));

        } catch (RestClientResponseException ex) {
            String raw = ex.getResponseBodyAsString();
            if (raw != null && !raw.isBlank()) {
                try { return mapper.readTree(raw); } catch (Exception ignored) {}
            }
            return error(ex.getStatusCode().value(), ex.getStatusText());
        } catch (Exception ex) {
            return error(503, ex.getMessage() == null ? "Gateway unavailable" : ex.getMessage());
        }
    }

    private void addAuth(RestClient.RequestHeadersSpec<?> req, SessionUser u) {
        if (u != null && u.token() != null && !u.token().isBlank()) {
            req.header(HttpHeaders.AUTHORIZATION, "Bearer " + u.token());
        }
    }

    private void addIdempotency(RestClient.RequestHeadersSpec<?> req, String key) {
        if (key != null && !key.isBlank()) req.header("Idempotency-Key", key);
    }

    private String correlationId(HttpSession session) {
        String id = (String) session.getAttribute(CORRELATION_ID);
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
            session.setAttribute(CORRELATION_ID, id);
        }
        return id;
    }

    private JsonNode parse(String raw) {
        if (raw == null || raw.isBlank()) return mapper.createObjectNode();
        try { return mapper.readTree(raw); }
        catch (Exception e) { return error(502, "Invalid response from Gateway"); }
    }

    private JsonNode error(int status, String message) {
        var n = mapper.createObjectNode();
        n.put("success", false);
        n.put("status", status);
        n.put("error", message == null ? "Unknown error" : message);
        return n;
    }

    public String text(JsonNode n, String field) {
        if (n == null || n.get(field) == null || n.get(field).isNull()) return "";
        return n.get(field).asText();
    }

    public List<JsonNode> list(JsonNode n) {
        if (n == null || n.isNull()) return List.of();
        if (n.isArray()) return iterable(n);
        if (n.has("content") && n.get("content").isArray()) return iterable(n.get("content"));
        if (n.has("items") && n.get("items").isArray()) return iterable(n.get("items"));
        if (n.has("data") && n.get("data").isArray()) return iterable(n.get("data"));
        return List.of();
    }

    public Map<String,Object> map(JsonNode n) {
        if (n == null || n.isNull()) return new LinkedHashMap<>();
        return mapper.convertValue(n, new TypeReference<Map<String,Object>>() {});
    }

    public List<Map<String,Object>> maps(JsonNode n) {
        List<Map<String,Object>> out = new ArrayList<>();
        for (JsonNode x : list(n)) out.add(map(x));
        return out;
    }

    public Object value(JsonNode n, String field) {
        return n != null && n.has(field) && !n.get(field).isNull() ? n.get(field) : null;
    }

    private List<JsonNode> iterable(JsonNode n) {
        List<JsonNode> out = new ArrayList<>();
        n.forEach(out::add);
        return out;
    }
}
