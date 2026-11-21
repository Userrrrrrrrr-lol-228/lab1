package com.example.demo;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

final class HttpSupport {

    record Resp(int status, JsonNode json) {
        String text(String field) {
            JsonNode n = json == null ? null : json.get(field);
            return n == null || n.isNull() ? null : n.asString();
        }
    }

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final String base;

    HttpSupport(int port) {
        this.base = "http://localhost:" + port;
    }

    Resp post(String path, String body) {
        return send(HttpRequest.newBuilder(URI.create(base + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build());
    }

    Resp get(String path, String bearer) {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + path)).GET();
        if (bearer != null) b.header("Authorization", "Bearer " + bearer);
        return send(b.build());
    }

    private Resp send(HttpRequest req) {
        try {
            HttpResponse<String> r = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode json = r.body() == null || r.body().isBlank() ? null : JSON.readTree(r.body());
            return new Resp(r.statusCode(), json);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static String json(String... kv) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < kv.length; i += 2) {
            if (i > 0) sb.append(',');
            sb.append('"').append(kv[i]).append("\":\"").append(kv[i + 1]).append('"');
        }
        return sb.append('}').toString();
    }
}
