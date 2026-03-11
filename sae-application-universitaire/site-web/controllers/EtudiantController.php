<?php
require_once __DIR__ . '/../core/Controller.php';

final class EtudiantController extends Controller {

   public function dashboard(): void {
        $this->requireRole(['ETUDIANT']);

        $user = $_SESSION['user'] ?? [];
        $idEtu = (int)($user['idEtu'] ?? 0);

        try {
            // 1. Récupérer l'étudiant connecté
            $etu = $idEtu ? $this->api->get('/etudiants/' . $idEtu) : null;

            // 2. Contexte (Formation / Semestre)
            $idf = (int)($etu['idFormation'] ?? 1);
            $sem = (int)($etu['numSemestre'] ?? 1);
            
            // 3. Récupérer toute la promo pour analyser les covoiturages
            $promo = $this->api->get("/formations/$idf/semestres/$sem/etudiants");
            
            $covoitsDispo = []; 
            $monGroupeCovoit = []; 
            $monIndice = $etu['indiceCovoiturage'] ?? null;

            foreach ($promo as $p) {
                $ind = $p['indiceCovoiturage'] ?? null;
                
                // Si l'étudiant a un covoit
                if ($ind !== null) {
                    // Si c'est mon indice, je l'ajoute à ma liste perso
                    if ($ind == $monIndice) {
                        $monGroupeCovoit[] = $p;
                    }
                    // J'ajoute à la liste globale des groupes
                    if (!isset($covoitsDispo[$ind])) {
                        $covoitsDispo[$ind] = [];
                    }
                    $covoitsDispo[$ind][] = $p;
                }
            }

            $this->render('etudiant/dashboard', [
                'etu' => $etu,
                'idf' => $idf,
                'sem' => $sem,
                'sondages' => $this->api->get('/sondages/actifs'), // On garde tes sondages
                'covoitsDispo' => $covoitsDispo, // On passe la liste des groupes
                'monGroupeCovoit' => $monGroupeCovoit // On passe mes partenaires
            ]);

        } catch (ApiException $e) {
            $this->render('layout/error', ['title' => 'Erreur', 'message' => $e->getMessage()]);
        }
    }

      public function creer(): void {
        $this->requireRole(['RESP_FORMATION', 'RESP_SEMESTRE']);

        // On récupère idf et sem depuis l'URL (ex: ?idf=1&sem=1)
        $idf = (int)($_GET['idf'] ?? 1);
        $sem = (int)($_GET['sem'] ?? 1);

        // Si le formulaire est soumis
        if ($_SERVER['REQUEST_METHOD'] === 'POST') {
            try {
                // On prépare les données pour l'API
                $data = [
                    'nomEtu' => $_POST['nomEtu'],
                    'prenomEtu' => $_POST['prenomEtu'],
                    'emailUniEtu' => $_POST['emailUniEtu'],
                    'emailPersoEtu' => $_POST['emailPersoEtu'],
                    'typeBac' => $_POST['typeBac'],
                    'tel' => $_POST['tel'] ?? '',
                    'dateNaissance' => $_POST['dateNaissance'] ?? date('Y-m-d'),
                    'addresseEtu' => $_POST['addresseEtu'] ?? '',
                    'genreEtu' => (int)$_POST['genreEtu'],
                    'estBoursier' => 0, // Par défaut
                    'idFormation' => $idf,
                    'numSemestre' => $sem
                ];

                // Appel API : POST /formations/{idf}/semestres/{sem}/etudiants
                $this->api->post("/formations/$idf/semestres/$sem/etudiants", $data);

                flash('success', "Étudiant ajouté avec succès !");
                $this->redirect(url_for('etudiant/promo', ['idf' => $idf, 'sem' => $sem]));

            } catch (ApiException $e) {
                flash('error', "Erreur lors de la création : " . $e->getMessage());
            }
        }

        // Affichage du formulaire
        $this->render('etudiant/creer', ['idf' => $idf, 'sem' => $sem]);
    }

    // -----------------------------------------------------------
    // SUPPRIMER UN ÉTUDIANT
    // -----------------------------------------------------------
    public function supprimer(): void {
        $this->requireRole(['RESP_FORMATION', 'RESP_SEMESTRE']);

        if ($_SERVER['REQUEST_METHOD'] === 'POST') {
            $idEtu = (int)$_POST['idEtu'];
            $idf = (int)$_POST['idf'];
            $sem = (int)$_POST['sem'];

            try {
                // Appel API : DELETE /etudiants/{id}
                $this->api->delete("/etudiants/$idEtu");
                
                flash('success', "Étudiant supprimé.");
            } catch (ApiException $e) {
                flash('error', "Impossible de supprimer : " . $e->getMessage());
            }

            // On revient sur la liste
            $this->redirect(url_for('etudiant/promo', ['idf' => $idf, 'sem' => $sem]));
        }
    }







    public function covoit(): void {
        $this->requireRole(['ETUDIANT']);
        $user = $_SESSION['user'] ?? [];
        $idEtu = (int)($user['idEtu'] ?? 0);

        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            http_response_code(405);
            $this->redirect(url_for('etudiant/dashboard'));
        }

        $idf = (int)($_POST['idFormation'] ?? 1);
        $sem = (int)($_POST['numSemestre'] ?? 1);
        $indice = $_POST['indiceCovoiturage'] ?? null;
        if ($indice === '') $indice = null;

        try {
            // IMPORTANT : côté site, on empêche l'étudiant de modifier quelqu'un d'autre.
            $this->api->patch("/etudiants/$idEtu/covoit", [
                'idFormation' => $idf,
                'numSemestre' => $sem,
                'indiceCovoiturage' => $indice
            ], true);
            flash('success', "Covoiturage mis à jour.");
        } catch (ApiException $e) {
            flash('error', $e->getMessage());
        }

        $this->redirect(url_for('etudiant/dashboard'));
    }

    public function promo(): void {
        $this->requireRole(['ETUDIANT','ENSEIGNANT','RESP_SEMESTRE','RESP_FORMATION']);

        $idf = (int)($_GET['idf'] ?? 1);
        $sem = (int)($_GET['sem'] ?? 1);

        try {
            $etudiants = $this->api->get("/formations/$idf/semestres/$sem/etudiants");
            $groupes = $this->api->get("/formations/$idf/semestres/$sem/groupes");
            $this->render('etudiant/promo', [
                'idf' => $idf, 'sem' => $sem,
                'etudiants' => $etudiants,
                'groupes' => $groupes
            ]);
        } catch (ApiException $e) {
            $this->render('layout/error', ['title' => 'Erreur', 'message' => $e->getMessage()]);
        }
    }

    public function signaler(): void {
        $this->requireRole(['ETUDIANT']);
        $sujet = rawurlencode("Signalement erreur données - " . ($_SESSION['user']['login'] ?? ''));
        $body = rawurlencode("Bonjour,

Je signale une erreur sur mes données :

- ...

Cordialement.");
        header('Location: mailto:' . SUPPORT_EMAIL . '?subject=' . $sujet . '&body=' . $body);
        exit;
    }
}
