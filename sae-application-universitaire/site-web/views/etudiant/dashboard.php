<h1>Mon espace étudiant</h1>

<?php if (!$etu): ?>
  <p class="muted">Profil non disponible (idEtu manquant dans le token).</p>
<?php else: ?>
  <div class="grid">
    <div class="card">
      <h2>Mon profil</h2>
      <div class="kv">
        <div><b>Nom</b><span><?= h($etu['nomEtu'] ?? '') ?></span></div>
        <div><b>Prénom</b><span><?= h($etu['prenomEtu'] ?? '') ?></span></div>
        <div><b>Email</b><span><?= h($etu['emailUniEtu'] ?? '') ?></span></div>
        <div><b>Type bac</b><span><?= h($etu['typeBac'] ?? '') ?></span></div>
        <div><b>Groupe</b><span><?= h($etu['nomGroupe'] ?? '—') ?></span></div>
      </div>
      <a class="btn btn--ghost" href="<?= h(url_for('etudiant/signaler')) ?>">Signaler une erreur (mail)</a>
    </div>

    <div class="card">
      <h2>🚗 Covoiturage</h2>
      
      <?php 
        $monIndice = $etu['indiceCovoiturage'] ?? null;
      ?>

      <?php if ($monIndice !== null): ?>
        
        <div class="flash flash--success" style="margin: 0 0 20px 0;">
            Vous êtes dans le covoiturage <strong>#<?= $monIndice ?></strong>
        </div>

        <h3>Mes partenaires de route :</h3>
        <ul class="list">
          <?php foreach ($monGroupeCovoit as $p): ?>
            <li style="padding: 8px 0;">
                <span><?= h($p['prenomEtu'] . ' ' . $p['nomEtu']) ?></span>
                <?php if($p['idEtu'] == $etu['idEtu']): ?>
                    <span class="pill">Moi</span>
                <?php endif; ?>
            </li>
          <?php endforeach; ?>
        </ul>

        <hr style="border: 0; border-top: 1px solid var(--border); margin: 20px 0;">

        <form method="post" action="<?= h(url_for('etudiant/covoit')) ?>">
            <input type="hidden" name="idFormation" value="<?= (int)$idf ?>">
            <input type="hidden" name="numSemestre" value="<?= (int)$sem ?>">
            <button class="btn btn--danger" type="submit" name="indiceCovoiturage" value="">
                Quitter ce covoiturage
            </button>
        </form>

      <?php else: ?>
        
        <p class="muted">Rejoignez un groupe existant ou créez-en un nouveau.</p>

        <form method="post" action="<?= h(url_for('etudiant/covoit')) ?>" class="form">
            <input type="hidden" name="idFormation" value="<?= (int)$idf ?>">
            <input type="hidden" name="numSemestre" value="<?= (int)$sem ?>">

            <label>
                <span>Rejoindre un groupe existant :</span>
                <select name="indiceCovoiturage" class="form-control" style="font-family: monospace;">
                    <option value="">-- Choisir dans la liste --</option>
                    <?php foreach ($covoitsDispo as $ind => $membres): ?>
                        <?php 
                            $nb = count($membres);
                            $max = 4; // Supposons max 4 places
                            $dispo = $max - $nb;
                            $label = "Groupe #$ind ($nb/$max pers.)";
                            
                            // On affiche les prénoms pour aider à choisir
                            $noms = array_map(fn($m) => $m['prenomEtu'], $membres);
                            $desc = implode(', ', array_slice($noms, 0, 2)) . (count($noms)>2 ? '...' : '');
                        ?>
                        <option value="<?= $ind ?>" <?= $dispo <= 0 ? 'disabled' : '' ?>>
                            <?= $label ?> — avec <?= h($desc) ?>
                        </option>
                    <?php endforeach; ?>
                </select>
            </label>

            <div style="text-align: center; margin: 10px 0; font-weight: bold; color: var(--text-muted);">- OU -</div>

            <label>
                <span>Créer un nouveau groupe (numéro libre) :</span>
                <input type="number" name="indiceCovoiturage" placeholder="Ex: 999" min="1">
            </label>

            <button class="btn" type="submit">Valider mon choix</button>
        </form>

      <?php endif; ?>
    </div>

    <div class="card">
      <h2>Sondages actifs</h2>
      <?php if (empty($sondages)): ?>
        <p class="muted">Aucun sondage actif.</p>
      <?php else: ?>
        <ul class="list">
          <?php foreach ($sondages as $s): ?>
            <li>
              <a href="<?= h(url_for('sondage/voir', ['id' => $s['idSondage']])) ?>">
                <?= h($s['questionSondage'] ?? ('Sondage #' . ($s['idSondage'] ?? ''))) ?>
              </a>
            </li>
          <?php endforeach; ?>
        </ul>
      <?php endif; ?>
    </div>

  </div>
<?php endif; ?>
