<?php
require_once __DIR__ . '/db.php';

class Formation
{
    public static function list(): array
    {
        $pdo = db();
        return $pdo->query('SELECT idFormation, nomFormation FROM Formation ORDER BY nomFormation')->fetchAll();
    }

    public static function get(int $idFormation): ?array
    {
        $pdo = db();
        $st = $pdo->prepare('SELECT idFormation, nomFormation FROM Formation WHERE idFormation = :id');
        $st->execute([':id' => $idFormation]);
        $row = $st->fetch();
        return $row ?: null;
    }

    public static function listSemestres(int $idFormation): array
    {
        $pdo = db();
        $st = $pdo->prepare('SELECT idFormation, numSemestre, nombreMaxCovoit, estApprentissage FROM Semestre WHERE idFormation = :id ORDER BY numSemestre');
        $st->execute([':id' => $idFormation]);
        return $st->fetchAll();
    }
}
