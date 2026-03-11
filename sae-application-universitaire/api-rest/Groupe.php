<?php
require_once __DIR__ . '/db.php';

/**
 * Classe utilitaire pour manipuler les groupes.
 */
class Groupe {
    /**
     * Retourne la liste des groupes pour une formation et un semestre.
     * Si $withEffectif est vrai, ajoute l'effectif via la fonction effectif_groupe().
     */
    public static function listGroupes(int $idFormation, int $numSemestre, bool $withEffectif = false): array {
        $pdo = db();
        $select = $withEffectif
            ? "SELECT g.nomGroupe, effectif_groupe(g.idFormation, g.numSemestre, g.nomGroupe) AS effectif"
            : "SELECT g.nomGroupe";
        $sql = "$select
                FROM Groupe g
                WHERE g.idFormation = :idf AND g.numSemestre = :sem
                ORDER BY g.nomGroupe";
        $st = $pdo->prepare($sql);
        $st->execute([':idf' => $idFormation, ':sem' => $numSemestre]);
        return $st->fetchAll();
    }
       /**
     * Supprime un groupe :
     * 1) Désaffecte tous les étudiants (nomGroupe = NULL)
     * 2) Supprime le groupe
     * Retourne le nb d'étudiants désaffectés.
     */
    public static function delete(int $idFormation, int $numSemestre, string $nomGroupe): array {
        $pdo = db();
        $pdo->beginTransaction();

        try {
            // 1) Désaffecter les étudiants
            $st1 = $pdo->prepare("
                UPDATE Etudiant
                SET nomGroupe = NULL
                WHERE idFormation = :idf AND numSemestre = :sem AND nomGroupe = :grp
            ");
            $st1->execute([':idf' => $idFormation, ':sem' => $numSemestre, ':grp' => $nomGroupe]);
            $nbDesaffectes = $st1->rowCount();

            // 2) Supprimer le groupe
            $st2 = $pdo->prepare("
                DELETE FROM Groupe
                WHERE idFormation = :idf AND numSemestre = :sem AND nomGroupe = :grp
            ");
            $st2->execute([':idf' => $idFormation, ':sem' => $numSemestre, ':grp' => $nomGroupe]);

            if ($st2->rowCount() === 0) {
                // Si aucun groupe supprimé, on rollback
                $pdo->rollBack();
                throw new RuntimeException("Groupe introuvable");
            }

            $pdo->commit();
            return [
                'status' => 'OK',
                'idFormation' => $idFormation,
                'numSemestre' => $numSemestre,
                'nomGroupe' => $nomGroupe,
                'nbEtudiantsDesaffectes' => $nbDesaffectes
            ];
        } catch (Exception $e) {
            if ($pdo->inTransaction()) $pdo->rollBack();
            throw $e;
        }

    }
        
    /**
     * Crée un groupe pour une promo/semestre.
     */
    public static function create(int $idFormation, int $numSemestre, string $nom): array
    {
        $pdo = db();
        $st = $pdo->prepare("INSERT INTO Groupe(idFormation, numSemestre, nomGroupe) VALUES (:idf, :sem, :nom)");
        $st->execute([':idf' => $idFormation, ':sem' => $numSemestre, ':nom' => $nom]);
        return ['idFormation' => $idFormation, 'numSemestre' => $numSemestre, 'nomGroupe' => $nom];
    }
}
?>
