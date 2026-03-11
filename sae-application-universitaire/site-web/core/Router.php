<?php
final class Router {
    public function dispatch(): void {
        $route = $_GET['r'] ?? 'home/index';
        $route = trim($route, '/');
        if ($route === '') $route = 'home/index';

        [$ctrl, $action] = array_pad(explode('/', $route, 2), 2, 'index');

        $controllerClass = ucfirst($ctrl) . 'Controller';
        $controllerFile  = __DIR__ . '/../controllers/' . $controllerClass . '.php';
        if (!file_exists($controllerFile)) {
            http_response_code(404);
            echo "Contrôleur introuvable";
            return;
        }

        require_once $controllerFile;
        if (!class_exists($controllerClass)) {
            http_response_code(500);
            echo "Classe contrôleur introuvable";
            return;
        }

        $controller = new $controllerClass();
        if (!method_exists($controller, $action)) {
            http_response_code(404);
            echo "Action introuvable";
            return;
        }

        $controller->$action();
    }
}
