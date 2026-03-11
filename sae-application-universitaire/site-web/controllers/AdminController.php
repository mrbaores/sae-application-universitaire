<?php
require_once __DIR__ . '/../core/Controller.php';

final class AdminController extends Controller {
    public function dashboard(): void {
        $this->requireRole(['ENSEIGNANT','RESP_SEMESTRE','RESP_FORMATION']);
        $this->render('layout/admin_dashboard');
    }
}
