<?php
require_once __DIR__ . '/../models/ApiClient.php';

abstract class Controller {
    protected ApiClient $api;

    public function __construct() {
        $this->api = new ApiClient(API_BASE_URL);
    }

    protected function render(string $view, array $data = []): void {
        extract($data);
        $viewFile = __DIR__ . '/../views/' . $view . '.php';
        if (!file_exists($viewFile)) {
            http_response_code(500);
            echo "Vue introuvable: " . htmlspecialchars($view);
            exit;
        }
        require __DIR__ . '/../views/layout/header.php';
        require $viewFile;
        require __DIR__ . '/../views/layout/footer.php';
    }

    protected function redirect(string $route): void {
        header('Location: ' . $route);
        exit;
    }

    protected function requireLogin(): void {
        if (empty($_SESSION['token'])) {
            $this->redirect(url_for('auth/login'));
        }
    }

    protected function requireRole(array $roles): void {
        $this->requireLogin();
        $role = $_SESSION['user']['role'] ?? null;
        if (!$role || !in_array($role, $roles, true)) {
            http_response_code(403);
            $this->render('layout/error', [
                'title' => 'Accès interdit',
                'message' => "Vous n'avez pas les droits nécessaires."
            ]);
            exit;
        }
    }
}
