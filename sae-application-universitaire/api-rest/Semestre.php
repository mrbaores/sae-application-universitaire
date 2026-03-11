<?php
require_once __DIR__ . '/db.php';

class Semestre
{
    public static function get(int $idFormation, int $numSemestre): ?array
    {
        $pdo = db();
        $st = $pdo->prepare("SELECT * FROM Semestre WHERE idFormation=:idf AND numSemestre=:sem");
        $st->execute([':idf' => $idFormation, ':sem' => $numSemestre]);
        $row = $st->fetch();
        return $row ?: null;
    }

    public static function maxCovoit(int $idFormation, int $numSemestre): ?int
    {
        $pdo = db();
        $st = $pdo->prepare("SELECT nombreMaxCovoit FROM Semestre WHERE idFormation=:idf AND numSemestre=:sem");
        $st->execute([':idf' => $idFormation, ':sem' => $numSemestre]);
        $val = $st->fetchColumn();
        return $val !== null ? (int)$val : null;
    }
}
