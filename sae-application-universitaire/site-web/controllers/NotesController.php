<?php
require_once __DIR__ . '/../core/Controller.php';

final class NotesController extends Controller {

    public function import(): void {
        $this->requireRole(['ENSEIGNANT','RESP_SEMESTRE','RESP_FORMATION']);

        if ($_SERVER['REQUEST_METHOD'] === 'POST') {
            if (empty($_FILES['csv']['tmp_name'])) {
                flash('error', "Fichier CSV requis.");
                $this->redirect(url_for('notes/import'));
            }

            $csv = file_get_contents($_FILES['csv']['tmp_name']);
            try {
                $res = $this->api->post('/notes/import', ['csv' => $csv], true);
                flash('success', "Import terminé.");
                $this->render('notes/import', ['result' => $res]);
                return;
            } catch (ApiException $e) {
                flash('error', $e->getMessage());
            }
        }

        $this->render('notes/import', ['result' => null]);
    }


    public function mesNotes(): void {
        // Sécurité : Seul un étudiant peut voir cette page
        $this->requireRole(['ETUDIANT']);

        $user = $_SESSION['user'] ?? [];
        $idEtu = (int)($user['idEtu'] ?? 0);

        try {
            // A. On récupère les infos de l'étudiant (pour avoir sa moyenne 'scoreGlobal')
            $etu = $this->api->get('/etudiants/' . $idEtu);

            // B. On récupère le détail des notes via l'API
            $notes = $this->api->get("/etudiants/$idEtu/notes");

            // C. On affiche la vue avec ces données
            $this->render('notes/mes_notes', [
                'etu' => $etu,
                'notes' => $notes
            ]);

        } catch (ApiException $e) {
            $this->render('layout/error', ['title' => 'Erreur', 'message' => $e->getMessage()]);
        }
    }
      /**
     * Permet à un PROF de voir les notes d'un étudiant spécifique
     */
    public function voir(): void {
        $this->requireRole(['ENSEIGNANT', 'RESP_FORMATION', 'RESP_SEMESTRE']);

        // 2. On récupère l'ID de l'étudiant dans l'URL (ex: index.php?r=notes/voir&id=28)
        $idEtu = (int)($_GET['id'] ?? 0);

        if ($idEtu === 0) {
            flash('error', "Étudiant introuvable.");
            $this->redirect(url_for('etudiant/promo'));
        }

        try {
            // 3. On récupère les infos de l'étudiant (pour son nom et sa moyenne)
            $etu = $this->api->get('/etudiants/' . $idEtu);

            // 4. On récupère ses notes
            $notes = $this->api->get("/etudiants/$idEtu/notes");

            // 5. On affiche la MÊME belle vue que pour les étudiants
            $this->render('notes/mes_notes', [
                'etu' => $etu,
                'notes' => $notes,
                'vueProf' => true // Petite astuce pour changer le titre si on veut
            ]);

        } catch (ApiException $e) {
            flash('error', "Impossible de récupérer les notes : " . $e->getMessage());
            $this->redirect(url_for('etudiant/promo'));
        }
    }

    public function export(): void {
        $this->requireRole(['ENSEIGNANT','RESP_SEMESTRE','RESP_FORMATION']);

        $idf = (int)($_GET['idf'] ?? 1);
        $sem = (int)($_GET['sem'] ?? 1);

        try {
            // L'API renvoie du CSV (texte). On le proxy.
            $csv = $this->api->get('/export/etudiants', ['formation' => $idf, 'semestre' => $sem], true);

            header('Content-Type: text/csv; charset=utf-8');
            header('Content-Disposition: attachment; filename="etudiants_'.$idf.'_'.$sem.'.csv"');
            echo $csv;
            exit;
        } catch (ApiException $e) {
            $this->render('layout/error', ['title' => 'Erreur', 'message' => $e->getMessage()]);
        }
    }
}
