<?php
require_once __DIR__ . '/../core/Controller.php';

final class SondageController extends Controller {

    public function actifs(): void {
        $this->requireRole(['ETUDIANT','ENSEIGNANT','RESP_SEMESTRE','RESP_FORMATION']);
        try {
            $sondages = $this->api->get('/sondages/actifs');
            $this->render('sondages/actifs', ['sondages' => $sondages]);
        } catch (ApiException $e) {
            $this->render('layout/error', ['title' => 'Erreur', 'message' => $e->getMessage()]);
        }
    }

    public function voir(): void {
        $this->requireRole(['ETUDIANT','ENSEIGNANT','RESP_SEMESTRE','RESP_FORMATION']);
        $id = (int)($_GET['id'] ?? 0);
        if ($id <= 0) $this->redirect(url_for('sondage/actifs'));

        try {
            $sondage = $this->api->get('/sondages/' . $id);
            $this->render('sondages/voir', ['sondage' => $sondage]);
        } catch (ApiException $e) {
            $this->render('layout/error', ['title' => 'Erreur', 'message' => $e->getMessage()]);
        }
    }

    public function creer(): void {
        $this->requireRole(['ENSEIGNANT','RESP_SEMESTRE','RESP_FORMATION']);

        if ($_SERVER['REQUEST_METHOD'] === 'POST') {
            $question = trim($_POST['question'] ?? '');
            $dateFin = trim($_POST['dateFin'] ?? '');
            $reponses = array_values(array_filter(array_map('trim', $_POST['reponses'] ?? [])));

            if ($question === '' || count($reponses) < 2) {
                flash('error', "Question + au moins 2 réponses.");
                $this->render('sondages/creer', ['question' => $question, 'dateFin' => $dateFin, 'reponses' => $reponses]);
                return;
            }

            try {
                $payload = [
                    'question' => $question,
                    'dateFin' => ($dateFin !== '' ? $dateFin : null),
                    
                    'typeReponse' => 'bouton',
                    'reponses' => $reponses
                ];
                $this->api->post('/sondages', $payload, true);
                flash('success', "Sondage créé.");
                $this->redirect(url_for('sondage/actifs'));
            } catch (ApiException $e) {
                flash('error', $e->getMessage());
                $this->render('sondages/creer', ['question' => $question, 'dateFin' => $dateFin, 'reponses' => $reponses]);
            }
            return;
        }

        $this->render('sondages/creer', ['question' => '', 'dateFin' => '', 'reponses' => ['','']]);
    }

    public function repondre(): void {
        $this->requireRole(['ETUDIANT']);

        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            http_response_code(405);
            $this->redirect(url_for('sondage/actifs'));
        }

        $idSondage = (int)($_POST['idSondage'] ?? 0);
        $idReponse = (int)($_POST['idReponse'] ?? 0);
        if ($idSondage <= 0 || $idReponse <= 0) {
            flash('error', "Choix invalide.");
            $this->redirect(url_for('sondage/voir', ['id' => $idSondage]));
        }

        try {
            $this->api->post("/sondages/$idSondage/repondre", ['idReponse' => $idReponse], true);
            flash('success', "Réponse enregistrée.");
        } catch (ApiException $e) {
            flash('error', $e->getMessage());
        }

        $this->redirect(url_for('sondage/voir', ['id' => $idSondage]));
    }
}
