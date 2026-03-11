<?php
require_once __DIR__ . '/db.php';

class Notes
{
    /**
     * Importe un CSV (texte) de notes.
     * Format attendu (en-tÇºte facultatif) : idEtu;idMatiere;nomControle;noteMax;coefficient;note
     * CrÇ¸e le contrÇïle s'il n'existe pas (recherche sur nomControle + idMatiere).
     */
    public static function importFromCsv(string $csv): array
    {
        $lines = preg_split('/\r\n|\r|\n/', trim($csv));
        $imported = 0;
        $errors = [];
        $pdo = db();
        foreach ($lines as $idx => $line) {
            if ($line === '') {
                continue;
            }
            $cols = str_getcsv($line, ';');
            if (count($cols) === 1) {
                $cols = str_getcsv($line, ',');
            }
            if (count($cols) < 6) {
                if ($idx === 0 && preg_match('/idEtu/i', $line)) {
                    continue;
                }
                $errors[] = "Ligne " . ($idx + 1) . " invalide";
                continue;
            }
            [$idEtu, $idMatiere, $nomCtrl, $noteMax, $coef, $note] = $cols;
            $idEtu = (int)$idEtu;
            $idMatiere = (int)$idMatiere;
            $noteMax = (int)$noteMax;
            $coef = (int)$coef;
            $note = (float)$note;
            try {
                $idControle = self::ensureControle($pdo, $idMatiere, $nomCtrl, $noteMax, $coef);
                $stmt = $pdo->prepare("INSERT INTO Note(idEtu, idControle, note) VALUES (:idEtu, :idCtrl, :note)
                                        ON DUPLICATE KEY UPDATE note = VALUES(note)");
                $stmt->execute([':idEtu' => $idEtu, ':idCtrl' => $idControle, ':note' => $note]);
                $imported++;
            } catch (Exception $e) {
                $errors[] = "Ligne " . ($idx + 1) . " : " . $e->getMessage();
            }
        }
        return ['imported' => $imported, 'errors' => $errors];
    }

    /**
     * Recupere les controle pour une matiere
     */
    private static function ensureControle(PDO $pdo, int $idMatiere, string $nom, int $noteMax, int $coef): int
    {
        $sel = $pdo->prepare("SELECT idControle FROM Controle WHERE nomExamen=:nom AND idMatiere=:mat");
        $sel->execute([':nom' => $nom, ':mat' => $idMatiere]);
        $id = $sel->fetchColumn();
        if ($id) {
            return (int)$id;
        }
        $ins = $pdo->prepare("INSERT INTO Controle(nomExamen, noteMaximale, coefficient, idMatiere)
                              VALUES (:nom, :max, :coef, :mat)");
        $ins->execute([':nom' => $nom, ':max' => $noteMax, ':coef' => $coef, ':mat' => $idMatiere]);
        return (int)$pdo->lastInsertId();
    }
}
