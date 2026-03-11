package fr.iut.sae.app.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mini parseur JSON très léger (sans dépendance externe).
 *
 * Suffisant pour l'API du projet (réponses = tableaux d'objets plats et petits objets).
 *
 * Limites assumées :
 * - ne gère pas correctement les chaînes contenant des accolades/virgules non échappées.
 * - ne gère pas les tableaux imbriqués.
 *
 * Si vous passez à Maven plus tard, remplacez ceci par Gson/Jackson.
 */
public final class JsonMini {

    private JsonMini() {}

    /** Parse un JSON de type: [ { ... }, { ... } ] */
    public static List<Map<String, String>> parseArrayOfObjects(String json) {
        if (json == null) {
            return List.of();
        }
        String trimmed = json.trim();
        if (trimmed.isEmpty() || trimmed.equals("[]")) {
            return List.of();
        }

        // Repère les blocs {...} en scannant les accolades.
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int start = -1;
        boolean inString = false;
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c == '"' && (i == 0 || trimmed.charAt(i - 1) != '\\')) {
                inString = !inString;
            }
            if (inString) continue;
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    objects.add(trimmed.substring(start, i + 1));
                    start = -1;
                }
            }
        }

        List<Map<String, String>> out = new ArrayList<>();
        for (String obj : objects) {
            out.add(parseObject(obj));
        }
        return out;
    }

    /** Parse un JSON de type: { "k": "v", "n": 12, ... } */
    public static Map<String, String> parseObject(String jsonObject) {
        Map<String, String> map = new HashMap<>();
        if (jsonObject == null) return map;
        String s = jsonObject.trim();
        if (!s.startsWith("{") || !s.endsWith("}")) return map;

        // Regex clé/valeur (valeur = string ou nombre ou bool ou null)
        Pattern p = Pattern.compile("\\\"([^\\\"]+)\\\"\\s*:\\s*(\\\"(.*?)\\\"|true|false|null|-?\\d+(?:\\.\\d+)?)", Pattern.DOTALL);
        Matcher m = p.matcher(s);
        while (m.find()) {
            String key = m.group(1);
            String raw = m.group(2);
            String value;
            if (raw.startsWith("\"")) {
                value = m.group(3);
                value = unescape(value);
            } else if (raw.equals("null")) {
                value = "";
            } else {
                value = raw;
            }
            map.put(key, value);
        }
        return map;
    }

    /** Parse un JSON de type: {"user":{...},"token":"..."} -> récupère la valeur string d'une clé */
    public static String extractStringField(String json, String field) {
        if (json == null || field == null) return null;
        Pattern p = Pattern.compile("\\\"" + Pattern.quote(field) + "\\\"\\s*:\\s*\\\"(.*?)\\\"", Pattern.DOTALL);
        Matcher m = p.matcher(json);
        if (m.find()) {
            return unescape(m.group(1));
        }
        return null;
    }

    private static String unescape(String s) {
        // suffisant pour notre API
        return s.replace("\\\\\"", "\"")
                .replace("\\\\n", "\n")
                .replace("\\\\r", "\r")
                .replace("\\\\t", "\t")
                .replace("\\\\/", "/")
                .replace("\\\\\\\\", "\\");
    }
}
