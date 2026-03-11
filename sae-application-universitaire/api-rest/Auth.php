<?php
require_once __DIR__ . '/db.php';

/**
 * Authentification
 * - POST /auth/login -> retourne {user, token}
 * - Token type JWT-like HS256 (HMAC), sans lib externe.
 */
class Auth
{
    private static function secret(): string
    {
        $cfg = api_config();
        return getenv('API_JWT_SECRET') ?: ($cfg['jwt_secret'] ?? 'dev-secret-change-me');
    }

    public static function login(string $login, string $password): ?array
{
    $pdo = db();
    $st = $pdo->prepare('SELECT idUser, login, passwordHash, role, actif FROM Utilisateur WHERE login = :l LIMIT 1');
    $st->execute([':l' => $login]);
    $row = $st->fetch();

    if (!$row) {
        return null;
    }

    // MODIFICATION 1 : Conversion forcée en entier pour le champ BIT(1)
    
    if ((int)$row['actif'] !== 1) {
        return null;
    }

    if (!password_verify($password, $row['passwordHash'])) {
        return null;
    }

    $user = [
        'idUser' => (int)$row['idUser'],
        'login'  => $row['login'],
        'role'   => $row['role'],
    ];

    if ($user['role'] === 'ETUDIANT') {
        $st = $pdo->prepare('SELECT idEtu FROM Etudiant WHERE idUser = :id LIMIT 1');
        $st->execute([':id' => $user['idUser']]);
        
        // MODIFICATION 2 : On utilise fetchColumn et on vérifie strictement
        $id = $st->fetchColumn();
        if ($id !== false) {
            $user['idEtu'] = (int)$id;
        } else {
            
            return null; 
        }
    } else {
        $st = $pdo->prepare('SELECT idEnseignant FROM Enseignant WHERE idUser = :id LIMIT 1');
        $st->execute([':id' => $user['idUser']]);
        $id = $st->fetchColumn();
        if ($id !== false) {
            $user['idEnseignant'] = (int)$id;
        }
    }

    return [
        'user' => $user,
        'token' => self::generateToken($user),
    ];
}
    public static function bearerToken(): ?string
    {
        $auth = $_SERVER['HTTP_AUTHORIZATION'] ?? $_SERVER['Authorization'] ?? null;
        if ($auth && stripos($auth, 'bearer ') === 0) {
            return trim(substr($auth, 7));
        }
        return null;
    }

    public static function requireAuth(?array $roles = null): ?array
    {
        $payload = self::verify(self::bearerToken());
        if (!$payload) {
            return null;
        }
        if ($roles && !in_array($payload['role'] ?? null, $roles, true)) {
            return null;
        }
        return $payload;
    }

    // ------------------- Token helpers -------------------

    public static function generateToken(array $payload): string
    {
        $header = ['alg' => 'HS256', 'typ' => 'JWT'];
        $now = time();
        $payload['iat'] = $now;
        $payload['exp'] = $now + 60 * 60 * 4; // 4 heures

        $h = self::b64(json_encode($header, JSON_UNESCAPED_UNICODE));
        $p = self::b64(json_encode($payload, JSON_UNESCAPED_UNICODE));
        $sig = self::b64(hash_hmac('sha256', "$h.$p", self::secret(), true));
        return "$h.$p.$sig";
    }

    public static function verify(?string $token): ?array
    {
        if (!$token || substr_count($token, '.') !== 2) {
            return null;
        }
        [$h, $p, $s] = explode('.', $token);
        $expected = self::b64(hash_hmac('sha256', "$h.$p", self::secret(), true));
        if (!hash_equals($expected, $s)) {
            return null;
        }
        $payload = json_decode(self::unb64($p), true);
        if (!is_array($payload)) {
            return null;
        }
        if (isset($payload['exp']) && time() > (int)$payload['exp']) {
            return null;
        }
        return $payload;
    }

    private static function b64(string $bin): string
    {
        return rtrim(strtr(base64_encode($bin), '+/', '-_'), '=');
    }

    private static function unb64(string $txt): string
    {
        $pad = strlen($txt) % 4;
        if ($pad) {
            $txt .= str_repeat('=', 4 - $pad);
        }
        return base64_decode(strtr($txt, '-_', '+/'));
    }
}
