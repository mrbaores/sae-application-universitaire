<?php
function url_for(string $route, array $params = []): string {
    $q = array_merge(['r' => $route], $params);
    return 'index.php?' . http_build_query($q);
}

function h(?string $s): string {
    return htmlspecialchars($s ?? '', ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}

function flash(string $type, string $message): void {
    $_SESSION['flash'][] = ['type' => $type, 'message' => $message];
}

function flash_take(): array {
    $msgs = $_SESSION['flash'] ?? [];
    unset($_SESSION['flash']);
    return $msgs;
}

function is_role(string $role): bool {
    return (($_SESSION['user']['role'] ?? null) === $role);
}

function has_role(array $roles): bool {
    $r = $_SESSION['user']['role'] ?? null;
    return $r && in_array($r, $roles, true);
}
