package fr.iut.sae.app.service;

import fr.iut.sae.app.model.dto.UserDTO;
import fr.iut.sae.app.util.JsonMini;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthService {

    private final ApiClient apiClient;

    public AuthService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public LoginResult login(String login, String password) throws IOException, InterruptedException {
        String body = "{\"login\":\"" + escape(login) + "\",\"password\":\"" + escape(password) + "\"}";
        String json = apiClient.postJson("/auth/login", body);

        String token = JsonMini.extractStringField(json, "token");
        if (token == null || token.isBlank()) {
            throw new IOException("Réponse login invalide : token manquant");
        }

        UserDTO user = parseUser(json);
        if (user == null) {
            throw new IOException("Réponse login invalide : user manquant");
        }

        return new LoginResult(user, token);
    }

    private UserDTO parseUser(String json) {
        // Capture le bloc user: {...}
        Pattern p = Pattern.compile("\\\"user\\\"\\s*:\\s*(\\{.*?\\})\\s*(,|\\})", Pattern.DOTALL);
        Matcher m = p.matcher(json);
        if (!m.find()) return null;
        String userObj = m.group(1);

        Map<String, String> map = JsonMini.parseObject(userObj);
        UserDTO u = new UserDTO();
        u.setIdUser(toInt(map.get("idUser")));
        u.setLogin(map.getOrDefault("login", ""));
        u.setRole(map.getOrDefault("role", ""));

        // champs optionnels
        if (map.containsKey("idEnseignant")) {
            int v = toInt(map.get("idEnseignant"));
            u.setIdEnseignant(v > 0 ? v : null);
        }
        if (map.containsKey("idEtu")) {
            int v = toInt(map.get("idEtu"));
            u.setIdEtu(v > 0 ? v : null);
        }
        return u;
    }

    private int toInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static class LoginResult {
        private final UserDTO user;
        private final String token;

        public LoginResult(UserDTO user, String token) {
            this.user = user;
            this.token = token;
        }

        public UserDTO getUser() {
            return user;
        }

        public String getToken() {
            return token;
        }
    }
}
