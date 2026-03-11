<h1>Créer un sondage</h1>

<form method="post" class="card form">
  <label>
    <span>Question</span>
    <input type="text" name="question" value="<?= h($question ?? '') ?>" required>
  </label>

  <label>
    <span>Date fin (optionnel, format libre selon ton API)</span>
    <input type="text" name="dateFin" value="<?= h($dateFin ?? '') ?>" placeholder="2026-02-01">
  </label>

  <h3>Réponses</h3>
  <?php
    $rs = $reponses ?? ['',''];
    $n = max(2, count($rs));
    for ($i=0; $i<$n; $i++):
  ?>
    <label>
      <span>Réponse <?= $i+1 ?></span>
      <input type="text" name="reponses[]" value="<?= h($rs[$i] ?? '') ?>" required>
    </label>
  <?php endfor; ?>

  <p class="muted">Astuce : tu peux ajouter des champs en modifiant le HTML (ou on fera un bouton + JS ensuite).</p>

  <button class="btn" type="submit">Créer</button>
</form>
