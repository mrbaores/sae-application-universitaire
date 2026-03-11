<?php
require_once __DIR__ . '/db.php';


final class Affectation
{
    /**
     * Applique toutes les affectations en une transaction.
     *
     * @param int $idFormation
     * @param int $numSemestre
     * @param array $affectations Tableau d'objets {idEtu, nomGroupe}
     * @return array
     */
    public static function applyBulk(int $idFormation, int $numSemestre, array $affectations): array
    {
        $pdo = db();

        if (empty($affectations) || !is_array($affectations)) {
            throw new RuntimeException("Le champ 'affectations' doit être un tableau non vide.");
        }

        // 1) Normalisation + validation minimale
        $rows = [];
        $groupNames = []; // groupes non null à créer/valider

        foreach ($affectations as $i => $a) {
            if (!is_array($a)) {
                throw new RuntimeException("Affectation #$i invalide : objet attendu.");
            }

            $idEtu = (int)($a['idEtu'] ?? 0);
            if ($idEtu <= 0) {
                throw new RuntimeException("Affectation #$i invalide : idEtu manquant ou invalide.");
            }

            // nomGroupe peut être null => désaffecter
            $nomGroupe = $a['nomGroupe'] ?? null;
            if ($nomGroupe !== null) {
                $nomGroupe = trim((string)$nomGroupe);
                if ($nomGroupe === '') {
                    $nomGroupe = null;
                }
            }

            $rows[] = ['idEtu' => $idEtu, 'nomGroupe' => $nomGroupe];

            if ($nomGroupe !== null) {
                $groupNames[$nomGroupe] = true;
            }
        }

        // 2) Transaction : tout ou rien
        $pdo->beginTransaction();
        try {
            
            if (!empty($groupNames)) {
                $stIns = $pdo->prepare(
                    "INSERT INTO Groupe(idFormation, numSemestre, nomGroupe)
                     VALUES (:idf, :sem, :nom)
                     ON DUPLICATE KEY UPDATE nomGroupe = nomGroupe"
                );

                foreach (array_keys($groupNames) as $nom) {
                    $stIns->execute([
                        ':idf' => $idFormation,
                        ':sem' => $numSemestre,
                        ':nom' => $nom
                    ]);
                }
            }

            
            // On protège avec idFormation/numSemestre pour ne pas toucher une autre promo.
            $stCheck = $pdo->prepare(
                "SELECT COUNT(*)
                 FROM Etudiant
                 WHERE idEtu = :id
                   AND idFormation = :idf
                   AND numSemestre = :sem"
            );

            $stUpd = $pdo->prepare(
                "UPDATE Etudiant
                 SET nomGroupe = :g
                 WHERE idEtu = :id
                   AND idFormation = :idf
                   AND numSemestre = :sem"
            );

            $updated = 0;

            foreach ($rows as $r) {
                // Vérif existence de l'étudiant dans la bonne promo/semestre
                $stCheck->execute([
                    ':id'  => $r['idEtu'],
                    ':idf' => $idFormation,
                    ':sem' => $numSemestre
                ]);

                if ((int)$stCheck->fetchColumn() !== 1) {
                    throw new RuntimeException(
                        "Étudiant introuvable dans formation=$idFormation semestre=$numSemestre : idEtu=" . $r['idEtu']
                    );
                }

                $stUpd->execute([
                    ':g'   => $r['nomGroupe'], // peut être null
                    ':id'  => $r['idEtu'],
                    ':idf' => $idFormation,
                    ':sem' => $numSemestre
                ]);

                $updated += $stUpd->rowCount();
            }

            $pdo->commit();

            return [
                'status' => 'OK',
                'idFormation' => $idFormation,
                'numSemestre' => $numSemestre,
                'nbAffectations' => count($rows),
                'updatedRows' => $updated
            ];
        } catch (Throwable $e) {
            $pdo->rollBack();
            throw $e;
        }
    }
}
