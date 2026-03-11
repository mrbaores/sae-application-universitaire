<h1>Importer des notes (CSV)</h1>

<div class="grid">
  <div class="card">
    <form method="post" enctype="multipart/form-data" class="form">
      <label>
        <span>Fichier CSV</span>
        <input type="file" name="csv" accept=".csv,text/csv" required>
      </label>
      <button class="btn" type="submit">Importer</button>
      <p class="muted">Le fichier est envoyé à l'API via /notes/import.</p>
    </form>
  </div>

  <div class="card">
    <h2>Résultat</h2>
    <?php if (empty($result)): ?>
      <p class="muted">Aucun import effectué.</p>
    <?php else: ?>
      <pre class="pre"><?= h(json_encode($result, JSON_PRETTY_PRINT|JSON_UNESCAPED_UNICODE)) ?></pre>
    <?php endif; ?>
  </div>
</div>
