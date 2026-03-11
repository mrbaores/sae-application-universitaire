<?php
require_once __DIR__ . '/db.php';

class Sondage
{
    /**
     * Liste les sondages actifs avec leurs rÇ¸ponses.
     */
    public static function listActifs(): array
    {
        $pdo = db();
        $sql = "SELECT s.idSondage, s.questionSondage, s.dateDebut, s.dateFin, s.idEnseignant
                  FROM Sondage s
                 WHERE s.dateDebut <= NOW()
                   AND (s.dateFin IS NULL OR s.dateFin >= NOW())
              ORDER BY s.dateDebut DESC";
        $sondages = $pdo->query($sql)->fetchAll();
        foreach ($sondages as &$s) {
            $s['reponses'] = self::reponses((int)$s['idSondage']);
        }
        return $sondages;
    }

    /**
     * DÇ¸tails d'un sondage + rÇ¸ponses.
     */
    public static function get(int $idSondage): ?array
    {
        $pdo = db();
        $st = $pdo->prepare("SELECT * FROM Sondage WHERE idSondage=:id");
        $st->execute([':id' => $idSondage]);
        $s = $st->fetch();
        if (!$s) {
            return null;
        }
        $s['reponses'] = self::reponses($idSondage, true);
        return $s;
    }

    /**
     * CrÇ¸e un sondage et ses rÇ¸ponses (boutons/cases/texte).
     */
    public static function create(int $idEnseignant, string $question, ?string $dateFin, string $typeReponse, array $reponses): array
{
    $pdo = db();

    // 1. GESTION DE LA DATE : On s'assure que le format est compatible SQL (YYYY-MM-DD HH:MM:SS)
    // Même si dateFin est nul ou une chaîne bizarre, strtotime + date vont le nettoyer.
    $finalDateFin = ($dateFin) ? date('Y-m-d H:i:s', strtotime($dateFin)) : null;

    try {
        // 2. UTILISATION DE LA PROCÉDURE STOCKÉE (si elle existe)
        $hasProc = $pdo->query("SELECT COUNT(*) FROM information_schema.ROUTINES WHERE ROUTINE_SCHEMA = DATABASE() AND ROUTINE_NAME='sp_creer_sondage'")->fetchColumn();
        
        if ($hasProc) {
            $list = implode('|', $reponses);
            $stmt = $pdo->prepare("CALL sp_creer_sondage(:idEns, :q, :fin, :type, :list)");
            $stmt->execute([
                ':idEns' => $idEnseignant,
                ':q'     => $question,
                ':fin'   => $finalDateFin, 
                ':type'  => $typeReponse,
                ':list'  => $list,
            ]);
            // On récupère l'ID généré par la procédure
            $id = $pdo->query("SELECT LAST_INSERT_ID()")->fetchColumn();
            return self::get((int)$id);
        }

        $s = $pdo->prepare("INSERT INTO Sondage(questionSondage, dateFin, idEnseignant) VALUES (:q, :fin, :ens)");
        $s->execute([
            ':q'   => $question,
            ':fin' => $finalDateFin, // Utilisation de la date formatée
            ':ens' => $idEnseignant,
        ]);
        
        $idSondage = (int)$pdo->lastInsertId();
        $r = $pdo->prepare("INSERT INTO Reponse(textReponse, typeReponse, idSondage) VALUES (:txt, :type, :idS)");
        foreach ($reponses as $rep) {
            $r->execute([':txt' => $rep, ':type' => $typeReponse, ':idS' => $idSondage]);
        }
        
        return self::get($idSondage);

    } catch (PDOException $e) {
        // 4. GESTION DES TRIGGERS : Si ton trigger SQL lance un SIGNAL SQLSTATE, 
        http_response_code(400);
        echo json_encode(['error' => 'Erreur SQL : ' . $e->getMessage()], JSON_UNESCAPED_UNICODE);
        exit;
    }
}
    /**
     * Enregistre une rÇ¸ponse d'Ç¸tudiant.
     */
    public static function voter(int $idEtu, int $idReponse): array
    {
        $pdo = db();
        $st = $pdo->prepare("INSERT INTO Etudiant_Reponse(idEtu, idReponse) VALUES (:idEtu, :idRep)");
        $st->execute([':idEtu' => $idEtu, ':idRep' => $idReponse]);
        return ['status' => 'OK', 'idEtu' => $idEtu, 'idReponse' => $idReponse];
    }

    /**
     * Retourne les rÇ¸ponses pour un sondage, avec le nombre de votes.
     */
    public static function reponses(int $idSondage, bool $withVotes = false): array
    {
        $pdo = db();
        $st = $pdo->prepare("SELECT idReponse, textReponse, typeReponse FROM Reponse WHERE idSondage=:id");
        $st->execute([':id' => $idSondage]);
        $res = $st->fetchAll();
        if ($withVotes) {
            foreach ($res as &$r) {
                $cnt = $pdo->prepare("SELECT nb_votes_reponse(:id)");
                $cnt->execute([':id' => $r['idReponse']]);
                $r['votes'] = (int)$cnt->fetchColumn();
            }
        }
        return $res;
    }
}
