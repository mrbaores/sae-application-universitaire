<?php
require_once __DIR__ . '/../core/Controller.php';

class AuthController extends Controller {

    /**
     * Gère la connexion
     */
    public function login(): void {
        // Si déjà connecté, on va à l'accueil
        if (!empty($_SESSION['token'])) {
            $this->redirect(url_for('home/index'));
        }

        if ($_SERVER['REQUEST_METHOD'] === 'POST') {
            $login = $_POST['login'] ?? '';
            $password = $_POST['password'] ?? '';

            try {
                // On appelle ton API existante
                $result = $this->api->post('/auth/login', [
                    'login' => $login, 
                    'password' => $password
                ]);

                if (!empty($result['token'])) {
                    // On enregistre tout dans la session
                    $_SESSION['token'] = $result['token'];
                    $_SESSION['user']  = $result['user'];
                    
                    // On récupère aussi idFormation / numSemestre pour les utiliser partout
                    // (L'API doit renvoyer ces infos dans 'user', sinon on met des valeurs par défaut)
                    $_SESSION['user']['idFormation'] = $result['user']['idFormation'] ?? 1;
                    $_SESSION['user']['numSemestre'] = $result['user']['numSemestre'] ?? 1;

                    flash('success', "Connexion réussie.");
                    
                    // Redirection intelligente selon le rôle
                    if (in_array($result['user']['role'], ['ENSEIGNANT', 'RESP_FORMATION', 'RESP_SEMESTRE'])) {
                        $this->redirect(url_for('admin/dashboard'));
                    } else {
                        $this->redirect(url_for('etudiant/dashboard'));
                    }
                    return;
                }
            } catch (ApiException $e) {
                flash('error', "Identifiant ou mot de passe incorrect.");
            }
        }

        $this->render('auth/login');
    }

    /**
     * Gère la déconnexion
     */
    public function logout(): void {
        // On vide la session
        session_unset();
        session_destroy();
        
        // On redirige vers la page de login
        // Attention : on utilise header() direct car flash() a besoin de la session
        header("Location: index.php?r=auth/login");
        exit;
    }
}