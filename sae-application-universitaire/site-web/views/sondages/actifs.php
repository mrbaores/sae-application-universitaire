<h1>Sondages actifs</h1>

<?php if (has_role(['ENSEIGNANT','RESP_SEMESTRE','RESP_FORMATION'])): ?>
  <p><a class="btn" href="<?= h(url_for('sondage/creer')) ?>">Créer un sondage</a></p>
<?php endif; ?>

<?php if (empty($sondages)): ?>
  <p class="muted">Aucun sondage actif.</p>
<?php else: ?>
  <div class="card">
    <ul class="list">
      <?php foreach ($sondages as $s): ?>
        <li>
          <a href="<?= h(url_for('sondage/voir', ['id'=>$s['idSondage']])) ?>">
            <?= h($s['questionSondage'] ?? ('Sondage #' . ($s['idSondage'] ?? ''))) ?>
          </a>
        </li>
      <?php endforeach; ?>
    </ul>
  </div>
<?php endif; ?>
