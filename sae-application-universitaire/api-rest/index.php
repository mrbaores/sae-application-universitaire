<?php
// Router principal pour l'API REST.

require_once __DIR__ . '/db.php';
require_once __DIR__ . '/Auth.php';
require_once __DIR__ . '/Formation.php';
require_once __DIR__ . '/Etudiant.php';
require_once __DIR__ . '/Groupe.php';
require_once __DIR__ . '/Sondage.php';
require_once __DIR__ . '/Notes.php';
require_once __DIR__ . '/Semestre.php';
require_once __DIR__ . '/Affectation.php';


$cfg = api_config();
$cors = $cfg['cors'] ?? [];

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: ' . ($cors['allow_origin'] ?? '*'));
header('Access-Control-Allow-Methods: ' . ($cors['allow_methods'] ?? 'GET, POST, PUT, PATCH, DELETE, OPTIONS'));
header('Access-Control-Allow-Headers: ' . ($cors['allow_headers'] ?? 'Content-Type, Authorization'));

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(204);
    exit;
}

function send_json($data, int $status = 200): void {
    http_response_code($status);
    echo json_encode($data, JSON_UNESCAPED_UNICODE);
    exit;
}

function send_text(string $text, string $contentType = 'text/plain; charset=utf-8', int $status = 200): void {
    http_response_code($status);
    header('Content-Type: ' . $contentType);
    echo $text;
    exit;
}

function require_auth(?array $roles = null): array {
    $payload = Auth::requireAuth($roles);
    if (!$payload) {
        send_json(['error' => 'Non autorise'], 401);
    }
    return $payload;
}

$method = $_SERVER['REQUEST_METHOD'];

// Calcule le chemin relatif (pour support sous /api/index.php, /api/ etc.)
$apiBase = rtrim(dirname($_SERVER['SCRIPT_NAME']), '/');
$path = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);
$route = substr($path, strlen($apiBase));
$route = trim($route, '/');
$parts = $route === '' ? [] : explode('/', $route);
if (!empty($parts) && $parts[0] === 'index.php') {
    array_shift($parts);
}

try {
    // ------------------- Meta -------------------
    if ($method === 'GET' && $route === 'health') {
        send_json(['status' => 'ok']);
    }

    // ------------------- Auth -------------------
    if ($method === 'POST' && $route === 'auth/login') {
        $input = json_decode(file_get_contents('php://input'), true) ?? [];
        $login = trim($input['login'] ?? '');
        $pass  = trim($input['password'] ?? '');
        if ($login === '' || $pass === '') {
            send_json(['error' => 'Identifiants requis'], 400);
        }
        $auth = Auth::login($login, $pass);
        if (!$auth) {
            send_json(['error' => 'Login ou mot de passe incorrect'], 401);
        }
        send_json($auth);
    }

    if ($method === 'GET' && $route === 'auth/me') {
        $payload = require_auth();
        send_json(['user' => $payload]);
    }

    // ------------------- Formations -------------------
    if ($method === 'GET' && $route === 'formations') {
        send_json(Formation::list());
    }

    if ($method === 'GET' && count($parts) === 2 && $parts[0] === 'formations') {
        $f = Formation::get((int)$parts[1]);
        if (!$f) send_json(['error' => 'Formation introuvable'], 404);
        send_json($f);
    }

    if ($method === 'GET' && count($parts) === 3 && $parts[0] === 'formations' && $parts[2] === 'semestres') {
        send_json(Formation::listSemestres((int)$parts[1]));
    }

    // ------------------- Semestre/Groupes/Etudiants -------------------
    // GET /formations/{idf}/semestres/{sem}
    if ($method === 'GET' && count($parts) == 4 && $parts[0] === 'formations' && $parts[2] === 'semestres') {
        $s = Semestre::get((int)$parts[1], (int)$parts[3]);
        if (!$s) send_json(['error' => 'Semestre introuvable'], 404);
        send_json($s);
    }

    // GET /formations/{idf}/semestres/{sem}/groupes
    if ($method === 'GET' && count($parts) === 5 && $parts[0] === 'formations' && $parts[2] === 'semestres' && $parts[4] === 'groupes') {
        $idf = (int)$parts[1];
        $sem = (int)$parts[3];
        send_json(Groupe::listGroupes($idf, $sem, true));
    }
  // GET /formations/{idf}/semestres/{sem}/groupes/{nomGroupe}/etudiants     
    if ($method === 'GET'
    && count($parts) === 7
    && $parts[0] === 'formations'
    && $parts[2] === 'semestres'
    && $parts[4] === 'groupes'
    && $parts[6] === 'etudiants') {

    require_auth(['ENSEIGNANT', 'RESP_SEMESTRE', 'RESP_FORMATION']); // recommandé

    $idf = (int)$parts[1];
    $sem = (int)$parts[3];
    $nomGroupe = trim(urldecode($parts[5]));

    send_json(Etudiant::listEtudiantsDuGroupe($idf, $sem, $nomGroupe));
}
    
    // POST /formations/{idf}/semestres/{sem}/groupes
    if ($method === 'POST' && count($parts) === 5 && $parts[0] === 'formations' && $parts[2] === 'semestres' && $parts[4] === 'groupes') {
        require_auth(['RESP_SEMESTRE', 'RESP_FORMATION']);
        $idf = (int)$parts[1];
        $sem = (int)$parts[3];
        $input = json_decode(file_get_contents('php://input'), true) ?? [];
        $nom = trim($input['nomGroupe'] ?? '');
        if ($nom === '') send_json(['error' => 'nomGroupe requis'], 400);
        send_json(Groupe::create($idf, $sem, $nom), 201);
    }

    // POST /formations/{idf}/semestres/{sem}/affectations
    if ($method === 'POST'
    && count($parts) === 5
    && $parts[0] === 'formations'
    && $parts[2] === 'semestres'
    && $parts[4] === 'affectations'
    ) {
    require_auth(['RESP_SEMESTRE', 'RESP_FORMATION']);

    $idf = (int)$parts[1];
    $sem = (int)$parts[3];

    $input = json_decode(file_get_contents('php://input'), true) ?? [];
    $affectations = $input['affectations'] ?? null;

    if (!is_array($affectations) || count($affectations) === 0) {
        send_json(['error' => 'Le champ affectations doit être un tableau non vide.'], 400);
    }

    $res = Affectation::applyBulk($idf, $sem, $affectations);
    send_json($res, 200);
    }

    // GET /formations/{idf}/semestres/{sem}/etudiants
    if ($method === 'GET' && count($parts) === 5 && $parts[0] === 'formations' && $parts[2] === 'semestres' && $parts[4] === 'etudiants') {
        $idf = (int)$parts[1];
        $sem = (int)$parts[3];
        send_json(Etudiant::listEtudiants($idf, $sem));
    }
    // DELETE /formations/{idf}/semestres/{sem}/groupes/{nomGroupe}
if ($method === 'DELETE'
    && count($parts) === 6
    && $parts[0] === 'formations'
    && $parts[2] === 'semestres'
    && $parts[4] === 'groupes'
) {
    require_auth(['RESP_SEMESTRE', 'RESP_FORMATION']);

    $idf = (int)$parts[1];
    $sem = (int)$parts[3];
    $nomGroupe = trim(urldecode($parts[5]));

    $res = Groupe::delete($idf, $sem, $nomGroupe);
    send_json($res, 200);
} 

    // POST /formations/{idf}/semestres/{sem}/etudiants
    if ($method === 'POST' && count($parts) === 5 && $parts[0] === 'formations' && $parts[2] === 'semestres' && $parts[4] === 'etudiants') {
        require_auth(['RESP_SEMESTRE', 'RESP_FORMATION']);
        $idf = (int)$parts[1];
        $sem = (int)$parts[3];
        $input = json_decode(file_get_contents('php://input'), true) ?? [];
        $input['idFormation'] = $idf;
        $input['numSemestre'] = $sem;
        $etu = Etudiant::create($input);
        send_json($etu, 201);
    }

    // GET /etudiants/{id}
    if ($method === 'GET' && count($parts) === 2 && $parts[0] === 'etudiants') {
        $etu = Etudiant::find((int)$parts[1]);
        if (!$etu) send_json(['error' => 'Etudiant introuvable'], 404);
        send_json($etu);
    }

   

    // PUT /etudiants/{id}
    if ($method === 'PUT' && count($parts) === 2 && $parts[0] === 'etudiants') {
    require_auth(['RESP_SEMESTRE', 'RESP_FORMATION']);

    $input = json_decode(file_get_contents('php://input'), true) ?? [];

    $idEtu = (int)$parts[1];
    $etu = Etudiant::update($idEtu, $input);

    if (!$etu) {
        send_json(['error' => 'Etudiant introuvable'], 404);
    }

    send_json($etu, 200);
    }

    // DELETE /etudiants/{id}
if ($method === 'DELETE' && count($parts) === 2 && $parts[0] === 'etudiants') {
    require_auth(['RESP_SEMESTRE', 'RESP_FORMATION']);

    $idEtu = (int)$parts[1];
    if ($idEtu <= 0) {
        send_json(['error' => 'Identifiant invalide'], 400);
    }

    // Vérifie existence (sinon on renvoie 404 au lieu de "OK")
    $etu = Etudiant::find($idEtu);
    if (!$etu) {
        send_json(['error' => 'Etudiant introuvable'], 404);
    }

    Etudiant::delete($idEtu);

    // Standard REST : 204 No Content
    http_response_code(204);
    exit;
}

    if ($method === 'PATCH') {
        // PATCH /etudiants/{id}/groupe
        if (count($parts) === 3 && $parts[0] === 'etudiants' && $parts[2] === 'groupe') {
            require_auth(['RESP_SEMESTRE', 'RESP_FORMATION']);
            $input = json_decode(file_get_contents('php://input'), true) ?? [];
            if (!isset($input['idFormation'], $input['numSemestre'])) {
                send_json(['error' => 'idFormation et numSemestre requis'], 400);
            }
            $res = Etudiant::setGroupe(
                (int)$parts[1],
                (int)$input['idFormation'],
                (int)$input['numSemestre'],
                isset($input['nomGroupe']) ? $input['nomGroupe'] : null
            );
            send_json($res);
        }

        // PATCH /etudiants/{id}/covoit
        if (count($parts) === 3 && $parts[0] === 'etudiants' && $parts[2] === 'covoit') {
            require_auth(['ETUDIANT', 'RESP_SEMESTRE', 'RESP_FORMATION']);
            $input = json_decode(file_get_contents('php://input'), true) ?? [];
            if (!isset($input['idFormation'], $input['numSemestre'])) {
                send_json(['error' => 'idFormation et numSemestre requis'], 400);
            }
            $res = Etudiant::setIndiceCovoit(
                (int)$parts[1],
                (int)$input['idFormation'],
                (int)$input['numSemestre'],
                $input['indiceCovoiturage'] ?? null
            );
            send_json($res);
        }

        send_json(['error' => 'Route PATCH non reconnue'], 404);
    }

    // GET /etudiants/{id}/notes
    if ($method === 'GET' && count($parts) === 3 && $parts[0] === 'etudiants' && $parts[2] === 'notes') {
        send_json(Notes::listByEtudiant((int)$parts[1]));
    }

    // POST /notes/import
    if ($method === 'POST' && $route === 'notes/import') {
        require_auth(['ENSEIGNANT', 'RESP_SEMESTRE', 'RESP_FORMATION']);
        $csv = null;
        if (!empty($_FILES['file']['tmp_name'])) {
            $csv = file_get_contents($_FILES['file']['tmp_name']);
        } else {
            $body = file_get_contents('php://input');
            $data = json_decode($body, true);
            if (is_array($data) && isset($data['csv'])) {
                $csv = $data['csv'];
            } else {
                $csv = $body;
            }
        }
        if (!$csv) {
            send_json(['error' => 'CSV manquant'], 400);
        }
        send_json(Notes::importFromCsv($csv));
    }

    // ------------------- Sondages -------------------
    // GET /sondages/actifs
    if ($method === 'GET' && $route === 'sondages/actifs') {
        send_json(Sondage::listActifs());
    }

    // GET /sondages/{id}
    if ($method === 'GET' && count($parts) === 2 && $parts[0] === 'sondages') {
        $s = Sondage::get((int)$parts[1]);
        if (!$s) send_json(['error' => 'Sondage non trouve'], 404);
        send_json($s);
    }

    // POST /sondages
    if ($method === 'POST' && $route === 'sondages') {
        $user = require_auth(['ENSEIGNANT', 'RESP_SEMESTRE', 'RESP_FORMATION']);
        $input = json_decode(file_get_contents('php://input'), true) ?? [];
        $question = trim($input['question'] ?? '');
       // $type = $input['typeReponse'] ?? 'bouton';
        $reps = $input['reponses'] ?? [];
        $dateFin = $input['dateFin'] ?? null;
        if ($question === '' || empty($reps)) {
            send_json(['error' => 'Question et reponses requises'], 400);
        }
        // idEnseignant obligatoire dans la table Sondage
        $idEns = (int)($user['idEnseignant'] ?? 0);
        if ($idEns <= 0) {
            send_json(['error' => 'Compte enseignant non lie (idEnseignant manquant)'], 400);
        }
        send_json(Sondage::create($idEns, $question, $dateFin, $type, $reps), 201);
    }

    // POST /sondages/{id}/repondre
    if ($method === 'POST' && count($parts) === 3 && $parts[0] === 'sondages' && $parts[2] === 'repondre') {
        $payload = require_auth(['ETUDIANT']);
        $idEtu = (int)($payload['idEtu'] ?? 0);
        if ($idEtu <= 0) {
            send_json(['error' => 'Compte etudiant non lie (idEtu manquant)'], 400);
        }
        $input = json_decode(file_get_contents('php://input'), true) ?? [];
        $idRep = (int)($input['idReponse'] ?? 0);
        if ($idRep <= 0) {
            send_json(['error' => 'idReponse requis'], 400);
        }
        $sondage = Sondage::get((int)$parts[1]);
        if (!$sondage) send_json(['error' => 'Sondage introuvable'], 404);
        send_json(Sondage::voter($idEtu, $idRep), 201);
    }

    // ------------------- Export CSV -------------------
    // GET /export/etudiants?formation=1&semestre=1
    if ($method === 'GET' && $route === 'export/etudiants') {
        require_auth(['RESP_SEMESTRE', 'RESP_FORMATION', 'ENSEIGNANT']);
        $idf = (int)($_GET['formation'] ?? 0);
        $sem = (int)($_GET['semestre'] ?? 0);
        if ($idf <= 0 || $sem <= 0) {
            send_json(['error' => 'Parametres formation et semestre requis'], 400);
        }
        $rows = Etudiant::listEtudiants($idf, $sem);
        $out = fopen('php://temp', 'r+');
        fputcsv($out, ['idEtu','nomEtu','prenomEtu','typeBac','genreEtu','estRedoublant','estApprenti','estAnglophone','indiceCovoiturage','nomGroupe','emailUniEtu'], ';');
        foreach ($rows as $r) {
            fputcsv($out, [
                $r['idEtu'] ?? '', $r['nomEtu'] ?? '', $r['prenomEtu'] ?? '', $r['typeBac'] ?? '', $r['genreEtu'] ?? '',
                $r['estRedoublant'] ?? '', $r['estApprenti'] ?? '', $r['estAnglophone'] ?? '', $r['indiceCovoiturage'] ?? '',
                $r['nomGroupe'] ?? '', $r['emailUniEtu'] ?? ''
            ], ';');
        }
        rewind($out);
        $csv = stream_get_contents($out);
        fclose($out);
        send_text($csv, 'text/csv; charset=utf-8');
    }

    send_json(['error' => 'Route non reconnue'], 404);

} catch (RuntimeException $e) {
    send_json(['error' => $e->getMessage()], 400);
} catch (Exception $e) {
    send_json(['error' => 'Erreur serveur', 'details' => $e->getMessage()], 500);
}
