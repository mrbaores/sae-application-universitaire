package fr.iut.sae.app.service;

import fr.iut.sae.app.model.state.AppSession;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Client HTTP centralisé :
 * - baseUrl configurée une fois
 * - ajoute automatiquement l'en-tête Authorization: Bearer <token> si présent
 */
public class ApiClient {

    private final String baseUrl;
    private final AppSession session;
    private final HttpClient client;

    public ApiClient(String baseUrl, AppSession session) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.session = session;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String get(String path) throws IOException, InterruptedException {
        HttpRequest request = baseRequest(path)
                .GET()
                .build();
        return send(request);
    }

    public String postJson(String path, String jsonBody) throws IOException, InterruptedException {
        HttpRequest request = baseRequest(path)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();
        return send(request);
    }

    public String patchJson(String path, String jsonBody) throws IOException, InterruptedException {
        HttpRequest request = baseRequest(path)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();
        return send(request);
    }

    public String delete(String path) throws IOException, InterruptedException {
        HttpRequest request = baseRequest(path)
                .DELETE()
                .build();
        return send(request);
    }

    private HttpRequest.Builder baseRequest(String path) {
        String full = path.startsWith("/") ? baseUrl + path : baseUrl + "/" + path;
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(full))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json");

        String token = session.getToken();
        if (token != null && !token.isBlank()) {
            b.header("Authorization", "Bearer " + token);
        }
        return b;
    }

    private String send(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> res = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        int code = res.statusCode();
        if (code < 200 || code >= 300) {
            throw new IOException("HTTP " + code + " -> " + res.body());
        }
        return res.body();
    }
}
