<div class="card" style="margin-bottom: 20px; background: #ecececff; padding: 15px;">
  <form method="get" action="index.php" style="display: flex; flex-wrap: wrap; gap: 15px; align-items: flex-end;">
    <input type="hidden" name="r" value="groupe/liste">
    
    <label style="margin-bottom: 0;">
        <span style="font-weight: bold; display: block; margin-bottom: 5px;">Formation ID :</span>
        <input type="number" name="idf" value="<?= (int)$idf ?>" style="padding: 8px; width: 80px;" min="1">
    </label>

    <label style="margin-bottom: 0;">
        <span style="font-weight: bold; display: block; margin-bottom: 5px;">Semestre :</span>
        <select name="sem" style="padding: 8px; width: 100px;">
            <?php for($i=1; $i<=6; $i++): ?>
                <option value="<?= $i ?>" <?= $sem == $i ? 'selected' : '' ?>>S<?= $i ?></option>
            <?php endfor; ?>
        </select>
    </label>

    <button type="submit" class="btn btn--sm" style="height: 38px;">Afficher</button>
  </form>
</div>

<h1>Groupes — Formation <?= (int)$idf ?> / Semestre <?= (int)$sem ?></h1>

<div class="grid">
  <div class="card">
    <h2>📂 Liste des Groupes</h2>
    <?php if (empty($groupes)): ?>
      <p class="muted">Aucun groupe trouvé pour ce semestre.</p>
    <?php else: ?>
      <ul class="list">
        <?php foreach ($groupes as $g): ?>
          <li style="display: flex; justify-content: space-between; align-items: center; padding: 12px; border-bottom: 1px solid #0e0d0dff;">
            
            <a href="<?= h(url_for('groupe/detail', ['idf'=>$idf,'sem'=>$sem,'g'=>$g['nomGroupe']])) ?>" 
               style="font-weight: bold; font-size: 1.1em; color: #2563eb; text-decoration: none; flex-grow: 1;">
               📂 Groupe <?= h($g['nomGroupe'] ?? '?') ?>
            </a>
            
            <span class="pill" title="Nombre d'étudiants" style="background-color: #ffffffff; color: #ecececff;">
                <?= (int)($g['effectif'] ?? 0) ?> étudiants
            </span>
          </li>
        <?php endforeach; ?>
      </ul>
    <?php endif; ?>
  </div>

  <?php if (has_role(['RESP_SEMESTRE','RESP_FORMATION'])): ?>
  <div class="card">
    <h2>🛠 Gestion</h2>
    
    <form method="post" action="<?= h(url_for('groupe/creer')) ?>" class="form">
      <input type="hidden" name="idf" value="<?= (int)$idf ?>">
      <input type="hidden" name="sem" value="<?= (int)$sem ?>">
      <label>
        <span>Nouveau Groupe (ex: G11)</span>
        <input type="text" name="nomGroupe" required placeholder="Nom du groupe">
      </label>
      <button class="btn" type="submit" style="width: 100%;">+ Créer</button>
    </form>

    <hr style="margin: 20px 0; border: 0; border-top: 1px solid #eee;">

    <h3 style="font-size: 1em; margin-bottom: 10px; color: #ef4444;">Zone de danger</h3>
    <p class="muted" style="font-size: 0.85em;">Supprimer un groupe désaffecte tous ses étudiants.</p>
    
    <form method="post" action="<?= h(url_for('groupe/supprimer')) ?>" class="form" onsubmit="return confirm('Êtes-vous sûr de vouloir supprimer ce groupe ?');">
      <input type="hidden" name="idf" value="<?= (int)$idf ?>">
      <input type="hidden" name="sem" value="<?= (int)$sem ?>">
      <label>
        <span>Nom du groupe à supprimer</span>
        <input type="text" name="nomGroupe" placeholder="G3" required>
      </label>
      <button class="btn btn--danger" type="submit" style="width: 100%;">Supprimer</button>
    </form>
  </div>
  <?php endif; ?>
</div>