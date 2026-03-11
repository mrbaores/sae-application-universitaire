<?php
require_once __DIR__ . '/db.php';

/**
 * Classe utilitaire pour manipuler les étudiants.
 */
class Etudiant {
    /**
     * Retourne la liste des étudiants pour une formation et un semestre donnés.
     */
    public static function listEtudiants(int $idFormation, int $numSemestre): array {
        $pdo = db();
        $sql = "SELECT idEtu, nomEtu, prenomEtu, genreEtu, typeBac, indiceCovoiturage,
                       estAnglophone, estRedoublant, estApprenti, nomGroupe,
                       emailUniEtu, emailPersoEtu, idFormation, numSemestre,
                       score_global_etudiant(idEtu) AS scoreGlobal
                FROM Etudiant
                WHERE idFormation = :idf AND numSemestre = :sem
                ORDER BY nomEtu, prenomEtu";
        $st = $pdo->prepare($sql);
        $st->execute([':idf' => $idFormation, ':sem' => $numSemestre]);
        return $st->fetchAll();
    }

   public static function listEtudiantsDuGroupe(int $idFormation, int $numSemestre, string $nomGroupe): array
{
    $pdo = db();

    $sql = "SELECT idEtu, nomEtu, prenomEtu, genreEtu, typeBac,
                   indiceCovoiturage, estAnglophone, estRedoublant, estApprenti,
                   nomGroupe, emailUniEtu
            FROM Etudiant
            WHERE idFormation = :idf
              AND numSemestre = :sem
              AND nomGroupe = :grp
            ORDER BY nomEtu, prenomEtu";

    $st = $pdo->prepare($sql);

    // bindValue suffit ici (valeurs simples), bindParam marche aussi
    $st->bindValue(':idf', $idFormation, PDO::PARAM_INT);
    $st->bindValue(':sem', $numSemestre, PDO::PARAM_INT);
    $st->bindValue(':grp', $nomGroupe, PDO::PARAM_STR);

    $st->execute();
    $rows = $st->fetchAll();

    // Normalisation BIT -> 0/1 (important en JSON)
    foreach ($rows as &$r) {
        $r['genreEtu'] = (int)($r['genreEtu'] ?? 0);
        $r['estAnglophone'] = (int)($r['estAnglophone'] ?? 0);
        $r['estRedoublant'] = (int)($r['estRedoublant'] ?? 0);
        $r['estApprenti'] = (int)($r['estApprenti'] ?? 0);
        $r['indiceCovoiturage'] = (int)($r['indiceCovoiturage'] ?? 0);
    }

    return $rows;
}
    /**
     * Affecte un étudiant à un groupe.
     */
    public static function setGroupe(int $idEtu, int $idFormation, int $numSemestre, ?string $nomGroupe): array {
        $pdo = db();
        if ($nomGroupe !== null) {
            $check = $pdo->prepare("SELECT COUNT(*) FROM Groupe WHERE idFormation=:idf AND numSemestre=:sem AND nomGroupe=:g");
            $check->execute([':idf' => $idFormation, ':sem' => $numSemestre, ':g' => $nomGroupe]);
            if ($check->fetchColumn() == 0) {
                throw new RuntimeException("Groupe inexistant");
            }
        }
        $upd = $pdo->prepare("UPDATE Etudiant
                              SET nomGroupe = :g
                              WHERE idEtu = :id AND idFormation = :idf AND numSemestre = :sem");
        $upd->execute([
            ':g' => $nomGroupe,
            ':id' => $idEtu,
            ':idf' => $idFormation,
            ':sem' => $numSemestre,
        ]);
        return ['status' => 'OK', 'idEtu' => $idEtu, 'nomGroupe' => $nomGroupe];
    }

    public static function find(int $idEtu): ?array
    {
        $pdo = db();
        $st = $pdo->prepare("SELECT *, score_global_etudiant(idEtu) AS scoreGlobal FROM Etudiant WHERE idEtu = :id");
        $st->execute([':id' => $idEtu]);
        $row = $st->fetch();
        return $row ?: null;
    }

   public static function create(array $data): array
{
    $pdo = db();

    $required = ['nomEtu','prenomEtu','dateNaissance','tel','typeBac','addresseEtu','emailPersoEtu','emailUniEtu','idFormation','numSemestre'];
    foreach ($required as $k) {
        if (!array_key_exists($k, $data)) {
            throw new InvalidArgumentException("Champ manquant: $k");
        }
    }

    $sql = "INSERT INTO Etudiant(
                nomEtu, prenomEtu, dateNaissance, tel, typeBac, addresseEtu,
                emailPersoEtu, emailUniEtu, genreEtu, indiceCovoiturage,
                estAnglophone, estRedoublant, estApprenti,
                idFormation, numSemestre, nomGroupe, idUser
            ) VALUES (
                :nom, :prenom, :naissance, :tel, :bac, :adresse,
                :emailPerso, :emailUni, :genre, :indiceCovoit,
                :anglophone, :redoublant, :apprenti,
                :idFormation, :numSemestre, :nomGroupe, :idUser
            )";

    $st = $pdo->prepare($sql);

    $st->bindValue(':nom', $data['nomEtu']);
    $st->bindValue(':prenom', $data['prenomEtu']);
    $st->bindValue(':naissance', $data['dateNaissance']);
    $st->bindValue(':tel', $data['tel']);
    $st->bindValue(':bac', $data['typeBac']);
    $st->bindValue(':adresse', $data['addresseEtu']);
    $st->bindValue(':emailPerso', $data['emailPersoEtu']);
    $st->bindValue(':emailUni', $data['emailUniEtu']);

    // Int/nullable
    if (array_key_exists('genreEtu', $data) && $data['genreEtu'] !== null) {
        $st->bindValue(':genre', (int)$data['genreEtu'], PDO::PARAM_INT);
    } else {
        $st->bindValue(':genre', null, PDO::PARAM_NULL);
    }

    if (array_key_exists('indiceCovoiturage', $data) && $data['indiceCovoiturage'] !== null) {
        $st->bindValue(':indiceCovoit', (int)$data['indiceCovoiturage'], PDO::PARAM_INT);
    } else {
        $st->bindValue(':indiceCovoit', null, PDO::PARAM_NULL);
    }

    // BOOL -> INT (crucial si BIT)
    $st->bindValue(':anglophone', !empty($data['estAnglophone']) ? 1 : 0, PDO::PARAM_INT);
    $st->bindValue(':redoublant', !empty($data['estRedoublant']) ? 1 : 0, PDO::PARAM_INT);
    $st->bindValue(':apprenti',   !empty($data['estApprenti'])   ? 1 : 0, PDO::PARAM_INT);

    $st->bindValue(':idFormation', (int)$data['idFormation'], PDO::PARAM_INT);
    $st->bindValue(':numSemestre', (int)$data['numSemestre'], PDO::PARAM_INT);

    // groupe nullable
    if (array_key_exists('nomGroupe', $data) && $data['nomGroupe'] !== null) {
        $st->bindValue(':nomGroupe', $data['nomGroupe']);
    } else {
        $st->bindValue(':nomGroupe', null, PDO::PARAM_NULL);
    }

    // idUser nullable
    if (array_key_exists('idUser', $data) && $data['idUser'] !== null) {
        $st->bindValue(':idUser', (int)$data['idUser'], PDO::PARAM_INT);
    } else {
        $st->bindValue(':idUser', null, PDO::PARAM_NULL);
    }

    $st->execute();

    $id = (int)$pdo->lastInsertId();
    return self::find($id);
}

    public static function update(int $idEtu, array $data): ?array
{
    $pdo = db();

    $required = [
        'nomEtu','prenomEtu','dateNaissance','tel','typeBac',
        'addresseEtu','emailPersoEtu','emailUniEtu'
    ];
    foreach ($required as $k) {
        if (!array_key_exists($k, $data)) {
            throw new InvalidArgumentException("Champ manquant: $k");
        }
    }

    $sql = "UPDATE Etudiant
               SET nomEtu=:nom,
                   prenomEtu=:prenom,
                   dateNaissance=:naissance,
                   tel=:tel,
                   typeBac=:bac,
                   addresseEtu=:adresse,
                   emailPersoEtu=:emailPerso,
                   emailUniEtu=:emailUni,
                   genreEtu=:genre,
                   indiceCovoiturage=:indice,
                   estAnglophone=:anglophone,
                   estRedoublant=:redoublant,
                   estApprenti=:apprenti,
                   nomGroupe=:nomGroupe
             WHERE idEtu=:id";

    $st = $pdo->prepare($sql);

    $st->bindValue(':nom', $data['nomEtu']);
    $st->bindValue(':prenom', $data['prenomEtu']);
    $st->bindValue(':naissance', $data['dateNaissance']);
    $st->bindValue(':tel', $data['tel']);
    $st->bindValue(':bac', $data['typeBac']);
    $st->bindValue(':adresse', $data['addresseEtu']);
    $st->bindValue(':emailPerso', $data['emailPersoEtu']);
    $st->bindValue(':emailUni', $data['emailUniEtu']);

    if (array_key_exists('genreEtu', $data) && $data['genreEtu'] !== null) {
        $st->bindValue(':genre', (int)$data['genreEtu'], PDO::PARAM_INT);
    } else {
        $st->bindValue(':genre', null, PDO::PARAM_NULL);
    }

    if (array_key_exists('indiceCovoiturage', $data) && $data['indiceCovoiturage'] !== null) {
        $st->bindValue(':indice', (int)$data['indiceCovoiturage'], PDO::PARAM_INT);
    } else {
        $st->bindValue(':indice', null, PDO::PARAM_NULL);
    }

    // BOOL -> INT (crucial si colonnes BIT(1))
    $st->bindValue(':anglophone', !empty($data['estAnglophone']) ? 1 : 0, PDO::PARAM_INT);
    $st->bindValue(':redoublant', !empty($data['estRedoublant']) ? 1 : 0, PDO::PARAM_INT);
    $st->bindValue(':apprenti',   !empty($data['estApprenti'])   ? 1 : 0, PDO::PARAM_INT);

    // nomGroupe nullable (STRING ou NULL)
    if (array_key_exists('nomGroupe', $data) && $data['nomGroupe'] !== null) {
        $st->bindValue(':nomGroupe', $data['nomGroupe']);
    } else {
        $st->bindValue(':nomGroupe', null, PDO::PARAM_NULL);
    }

    $st->bindValue(':id', $idEtu, PDO::PARAM_INT);

    $st->execute();

    return self::find($idEtu);
}

    public static function delete(int $idEtu): void
    {
        $pdo = db();
        $check = $pdo->prepare("SELECT COUNT(*) FROM information_schema.ROUTINES WHERE ROUTINE_SCHEMA = DATABASE() AND ROUTINE_NAME='sp_supprimer_etudiant'");
        $check->execute();
        $hasProc = $check->fetchColumn();
        if ($hasProc && (int)$hasProc > 0) {
            $call = $pdo->prepare("CALL sp_supprimer_etudiant(:id)");
            $call->execute([':id' => $idEtu]);
        } else {
            $pdo->prepare("DELETE FROM Etudiant_Reponse WHERE idEtu=:id")->execute([':id' => $idEtu]);
            $pdo->prepare("DELETE FROM Note WHERE idEtu=:id")->execute([':id' => $idEtu]);
            $pdo->prepare("DELETE FROM Etudiant WHERE idEtu=:id")->execute([':id' => $idEtu]);
        }
    }

    public static function setIndiceCovoit(int $idEtu, int $idFormation, int $numSemestre, ?int $indice): array
    {
        $pdo = db();
        if ($indice !== null) {
            $stmt = $pdo->prepare("SELECT indice_covoit_est_plein(:idf,:sem,:ind)");
            $stmt->execute([':idf' => $idFormation, ':sem' => $numSemestre, ':ind' => $indice]);
            $plein = (int)$stmt->fetchColumn();
            if ($plein === 1) {
                throw new RuntimeException("Indice covoiturage $indice saturé pour ce semestre");
            }
        }
        $upd = $pdo->prepare("UPDATE Etudiant SET indiceCovoiturage = :ind WHERE idEtu=:id");
        $upd->execute([':ind' => $indice, ':id' => $idEtu]);
        return ['status' => 'OK', 'idEtu' => $idEtu, 'indiceCovoiturage' => $indice];
    }

    public static function score(int $idEtu): ?float
    {
        $pdo = db();
        $stmt = $pdo->prepare("SELECT score_global_etudiant(:id)");
        $stmt->execute([':id' => $idEtu]);
        $val = $stmt->fetchColumn();
        return $val !== null ? (float)$val : null;
    }
}
?>
