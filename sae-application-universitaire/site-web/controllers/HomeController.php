<?php
final class HomeController extends Controller {
    public function index(): void {
        if (!empty($_SESSION['token'])) {
            $role = $_SESSION['user']['role'] ?? '';
            if ($role === 'ETUDIANT') $this->redirect(url_for('etudiant/dashboard'));
            $this->redirect(url_for('admin/dashboard'));
        }
        $this->redirect(url_for('auth/login'));
    }
}
