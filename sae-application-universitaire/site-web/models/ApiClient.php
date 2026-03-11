<?php
require_once __DIR__ . '/../core/ApiException.php';

final class ApiClient {
    private string $baseUrl;

    public function __construct(string $baseUrl) {
        $this->baseUrl = rtrim($baseUrl, '/');
    }

    public function get(string $path, array $query = [], bool $auth = true): array|string {
        return $this->request('GET', $path, $query, null, $auth);
    }

    public function post(string $path, array $body = [], bool $auth = true): array|string {
        return $this->request('POST', $path, [], $body, $auth);
    }

    public function put(string $path, array $body = [], bool $auth = true): array|string {
        return $this->request('PUT', $path, [], $body, $auth);
    }

    public function patch(string $path, array $body = [], bool $auth = true): array|string {
        return $this->request('PATCH', $path, [], $body, $auth);
    }

    public function delete(string $path, array $body = [], bool $auth = true): array|string {
       
        return $this->request('DELETE', $path, [], $body ?: null, $auth);
    }

    /**
     * @return array|string  JSON décodé (array) ou texte brut (string) si la réponse n'est pas JSON.
     */
    private function request(string $method, string $path, array $query = [], ?array $body = null, bool $auth = true): array|string {
        $url = $this->baseUrl . '/' . ltrim($path, '/');
        if (!empty($query)) {
            $url .= '?' . http_build_query($query);
        }

        $ch = curl_init($url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_CUSTOMREQUEST, $method);

        $headers = ['Accept: application/json'];
        if ($auth) {
            $token = $_SESSION['token'] ?? null;
            if ($token) $headers[] = 'Authorization: Bearer ' . $token;
        }

        if ($body !== null) {
            $json = json_encode($body, JSON_UNESCAPED_UNICODE);
            $headers[] = 'Content-Type: application/json; charset=utf-8';
            curl_setopt($ch, CURLOPT_POSTFIELDS, $json);
        }

        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);

        $raw = curl_exec($ch);
        if ($raw === false) {
            $err = curl_error($ch);
            curl_close($ch);
            throw new ApiException('Erreur cURL: ' . $err, 0);
        }
        $status = (int)curl_getinfo($ch, CURLINFO_HTTP_CODE);
        $contentType = (string)curl_getinfo($ch, CURLINFO_CONTENT_TYPE);
        curl_close($ch);

        $isJson = stripos($contentType, 'application/json') !== false;

        if ($status < 200 || $status >= 300) {
            $payload = [];
            $msg = "Erreur API ($status)";
            if ($isJson) {
                $payload = json_decode($raw, true) ?? [];
                if (isset($payload['error'])) $msg = $payload['error'];
            }
            throw new ApiException($msg, $status, $payload);
        }

        if ($isJson) {
            return json_decode($raw, true) ?? [];
        }
        return $raw; // CSV, texte, etc.
    }
}
