<?php
require_once __DIR__ . '/../core/Controller.php';

final class GroupeController extends Controller {

    public function liste(): void {
        $this->requireRole(['ENSEIGNANT','RESP_SEMESTRE','RESP_FORMATION','ETUDIANT']);

        $idf = (int)($_GET['idf'] ?? 1);
        $sem = (int)($_GET['sem'] ?? 1);

        try {
            $groupes = $this->api->get("/formations/$idf/semestres/$sem/groupes");
            $this->render('groupes/liste', [
                'idf' => $idf,
                'sem' => $sem,
                'groupes' => $groupes
            ]);
        } catch (ApiException $e) {
            $this->render('layout/error', ['title' => 'Erreur', 'message' => $e->getMessage()]);
        }
    }

    public function detail(): void {
        $this->requireRole(['ENSEIGNANT','RESP_SEMESTRE','RESP_FORMATION','ETUDIANT']);

        $idf = (int)($_GET['idf'] ?? 1);
        $sem = (int)($_GET['sem'] ?? 1);
        $nom = (string)($_GET['g'] ?? '');

        if ($nom === '') {
            $this->redirect(url_for('groupe/liste', ['idf' => $idf, 'sem' => $sem]));
        }

        try {
            $etudiants = $this->api->get("/formations/$idf/semestres/$sem/groupes/" . rawurlencode($nom) . "/etudiants");
            $this->render('groupes/detail', [
                'idf' => $idf,
                'sem' => $sem,
                'nom' => $nom,
                'etudiants' => $etudiants
            ]);
        } catch (ApiException $e) {
            $this->render('layout/error', ['title' => 'Erreur', 'message' => $e->getMessage()]);
        }
    }

    public function creer(): void {
        $this->requireRole(['RESP_SEMESTRE','RESP_FORMATION']);

        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            http_response_code(405);
            $this->redirect(url_for('groupe/liste'));
        }

        $idf = (int)($_POST['idf'] ?? 1);
        $sem = (int)($_POST['sem'] ?? 1);
        $nom = trim($_POST['nomGroupe'] ?? '');

        if ($nom === '') {
            flash('error', "Nom de groupe requis.");
            $this->redirect(url_for('groupe/liste', ['idf' => $idf, 'sem' => $sem]));
        }

        try {
            $this->api->post("/formations/$idf/semestres/$sem/groupes", ['nomGroupe' => $nom], true);
            flash('success', "Groupe créé.");
        } catch (ApiException $e) {
            flash('error', $e->getMessage());
        }

        $this->redirect(url_for('groupe/liste', ['idf' => $idf, 'sem' => $sem]));
    }

    public function supprimer(): void {
        $this->requireRole(['RESP_SEMESTRE','RESP_FORMATION']);

        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            http_response_code(405);
            $this->redirect(url_for('groupe/liste'));
        }

        $idf = (int)($_POST['idf'] ?? 1);
        $sem = (int)($_POST['sem'] ?? 1);
        $nom = trim($_POST['nomGroupe'] ?? '');

        if ($nom === '') {
            flash('error', "Nom de groupe manquant.");
            $this->redirect(url_for('groupe/liste', ['idf' => $idf, 'sem' => $sem]));
        }

        try {
            // Nécessite que tu aies bien ajouté la route DELETE groupe côté API.
            $this->api->delete("/formations/$idf/semestres/$sem/groupes/" . rawurlencode($nom), [], true);
            flash('success', "Groupe supprimé (étudiants désaffectés).");
        } catch (ApiException $e) {
            flash('error', $e->getMessage());
        }

        $this->redirect(url_for('groupe/liste', ['idf' => $idf, 'sem' => $sem]));
    }
}
