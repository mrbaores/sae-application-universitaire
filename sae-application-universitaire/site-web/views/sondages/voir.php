<h1>Sondage</h1>

<div class="card">
  <h2><?= h($sondage['questionSondage'] ?? 'Question') ?></h2>
  <p class="muted">Début: <?= h($sondage['dateDebut'] ?? '') ?> — Fin: <?= h($sondage['dateFin'] ?? '') ?></p>

  <?php $reps = $sondage['reponses'] ?? []; ?>
  <?php if (empty($reps)): ?>
    <p class="muted">Aucune réponse configurée.</p>
  <?php else: ?>
    <?php if (has_role(['ETUDIANT'])): ?>
      <form method="post" action="<?= h(url_for('sondage/repondre')) ?>" class="form">
        <input type="hidden" name="idSondage" value="<?= (int)($sondage['idSondage'] ?? 0) ?>">
        <?php foreach ($reps as $r): ?>
          <label class="radio">
            <input type="radio" name="idReponse" value="<?= (int)($r['idReponse'] ?? 0) ?>" required>
            <span><?= h($r['textReponse'] ?? '') ?></span>
          </label>
        <?php endforeach; ?>
        <button class="btn" type="submit">Valider</button>
      </form>
    <?php else: ?>
      <ul class="list">
        <?php foreach ($reps as $r): ?>
          <li><?= h($r['textReponse'] ?? '') ?></li>
        <?php endforeach; ?>
      </ul>
    <?php endif; ?>
  <?php endif; ?>
</div>

<p><a href="<?= h(url_for('sondage/actifs')) ?>">← Retour</a></p>
