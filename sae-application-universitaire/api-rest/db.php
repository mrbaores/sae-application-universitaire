<?php
/**
 * Connexion PDO centralisee.
 * - config.php doit etre present sur le serveur, mais ne doit PAS etre versionne.
 */

function api_config(): array
{
    static $cfg = null;
    if ($cfg !== null) {
        return $cfg;
    }

    $cfgFile = __DIR__ . '/config.php';
    if (file_exists($cfgFile)) {
        $cfg = require $cfgFile;
        if (!is_array($cfg)) {
            throw new RuntimeException('config.php invalide (doit retourner un tableau).');
        }
        return $cfg;
    }

    // Fallback via variables d'environnement
    $cfg = [
        'db' => [
            'host' => getenv('DB_HOST') ?: 'localhost',
            'name' => getenv('DB_NAME') ?: '',
            'user' => getenv('DB_USER') ?: '',
            'pass' => getenv('DB_PASS') ?: '',
            'charset' => getenv('DB_CHARSET') ?: 'utf8mb4',
        ],
        'jwt_secret' => getenv('API_JWT_SECRET') ?: 'dev-secret-change-me',
        'cors' => [
            'allow_origin' => '*',
            'allow_headers' => 'Content-Type, Authorization',
            'allow_methods' => 'GET, POST, PUT, PATCH, DELETE, OPTIONS',
        ],
    ];
    return $cfg;
}

function db(): PDO
{
    static $pdo = null;
    if ($pdo !== null) {
        return $pdo;
    }

    $cfg = api_config();
    $db = $cfg['db'] ?? [];
    $host = $db['host'] ?? 'localhost';
    $name = $db['name'] ?? '';
    $user = $db['user'] ?? '';
    $pass = $db['pass'] ?? '';
    $charset = $db['charset'] ?? 'utf8mb4';

    if ($name == '' || $user == '') {
        throw new RuntimeException('Configuration BDD manquante: renseignez config.php (db.name/db.user).');
    }

    $dsn = "mysql:host={$host};dbname={$name};charset={$charset}";
    $pdo = new PDO($dsn, $user, $pass, [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES => false,
    ]);
    return $pdo;
}
