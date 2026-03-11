<h1>Détail — Groupe <?= h($nom) ?> (F<?= (int)$idf ?> S<?= (int)$sem ?>)</h1>

<div class="card">
  <h2>Étudiants</h2>
  <?php if (empty($etudiants)): ?>
    <p class="muted">Aucun étudiant dans ce groupe.</p>
  <?php else: ?>
    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th><th>Nom</th><th>Prénom</th><th>Bac</th><th>Anglais</th><th>Apprenti</th><th>Redoublant</th>
          </tr>
        </thead>
        <tbody>
          <?php foreach ($etudiants as $e): ?>
            <tr>
              <td><?= (int)($e['idEtu'] ?? 0) ?></td>
              <td><?= h($e['nomEtu'] ?? '') ?></td>
              <td><?= h($e['prenomEtu'] ?? '') ?></td>
              <td><?= h($e['typeBac'] ?? '') ?></td>
              <td><?= (int)($e['estAnglophone'] ?? 0) ?></td>
              <td><?= (int)($e['estApprenti'] ?? 0) ?></td>
              <td><?= (int)($e['estRedoublant'] ?? 0) ?></td>
            </tr>
          <?php endforeach; ?>
        </tbody>
      </table>
    </div>
  <?php endif; ?>
</div>

<p><a href="<?= h(url_for('groupe/liste', ['idf'=>$idf,'sem'=>$sem])) ?>">← Retour</a></p>
