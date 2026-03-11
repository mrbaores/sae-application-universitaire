<h1>Promotion — Formation <?= (int)$idf ?> / Semestre <?= (int)$sem ?></h1>

<div class="grid">
  <div class="card">
    <h2>📂 Groupes</h2>
    <?php if (empty($groupes)): ?>
      <p class="muted">Aucun groupe.</p>
    <?php else: ?>
      <ul class="list">
        <?php foreach ($groupes as $g): ?>
          <li>
            <a href="<?= h(url_for('groupe/detail', ['idf'=>$idf,'sem'=>$sem,'g'=>$g['nomGroupe']])) ?>">
              📂 <?= h($g['nomGroupe'] ?? '') ?>
            </a>
            <span class="pill"><?= (int)($g['effectif'] ?? 0) ?></span>
          </li>
        <?php endforeach; ?>
      </ul>
    <?php endif; ?>
  </div>

  <div class="card" style="grid-column: span 2;">
    
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:15px;">
        <h2>🎓 Liste des Étudiants</h2>
        
        <?php if (has_role(['RESP_FORMATION', 'RESP_SEMESTRE'])): ?>
            <a href="<?= h(url_for('etudiant/creer', ['idf'=>$idf, 'sem'=>$sem])) ?>" class="btn btn--sm">
                + Ajouter un étudiant
            </a>
        <?php endif; ?>
    </div>
    
    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>N° Etu</th>
            <th>Genre</th>
            <th>Nom Prénom</th>
            <th>Email Univ.</th>
            <th>Bac</th>
            <th>Groupe</th>
            
            <?php if (has_role(['ENSEIGNANT', 'RESP_FORMATION', 'RESP_SEMESTRE'])): ?>
                <th style="text-align:right;">Actions</th>
            <?php endif; ?>
          </tr>
        </thead>
        <tbody>
          <?php foreach ($etudiants as $e): ?>
            <tr>
              <td><strong><?= (int)($e['idEtu'] ?? 0) ?></strong></td>
              
              <td>
                <?php 
                   $g = (int)($e['genreEtu'] ?? 0);
                   if ($g === 1) {
                       echo '<span class="pill">M</span>';
                   } else {
                       echo '<span class="pill" style="background:#fce7f3; color:#be185d;">F</span>';
                   }
                ?>
              </td>

              <td>
                <span style="font-weight:bold; text-transform:uppercase;"><?= h($e['nomEtu'] ?? '') ?></span>
                <?= h($e['prenomEtu'] ?? '') ?>
              </td>

              <td>
                <a href="mailto:<?= h($e['emailUniEtu'] ?? '') ?>" style="font-weight:normal; font-size:0.9em; text-decoration: underline;">
                    <?= h($e['emailUniEtu'] ?? '—') ?>
                </a>
              </td>

              <td><?= h($e['typeBac'] ?? '') ?></td>

              <td>
                <?php if (!empty($e['nomGroupe'])): ?>
                    <span class="pill"><?= h($e['nomGroupe']) ?></span>
                <?php else: ?>
                    <span class="muted">—</span>
                <?php endif; ?>
              </td>

              <?php if (has_role(['ENSEIGNANT', 'RESP_FORMATION', 'RESP_SEMESTRE'])): ?>
                <td style="text-align:right; white-space: nowrap;">
                    
                    <a href="<?= h(url_for('notes/voir', ['id' => $e['idEtu']])) ?>" 
                       class="btn btn--sm" 
                       style="background-color: var(--primary); color: white; margin-right: 5px; text-decoration:none;"
                       title="Voir le bulletin">
                        📊
                    </a>

                    <?php if (has_role(['RESP_FORMATION', 'RESP_SEMESTRE'])): ?>
                        <form method="post" action="<?= h(url_for('etudiant/supprimer')) ?>" onsubmit="return confirm('Êtes-vous sûr de vouloir supprimer cet étudiant ?');" style="display:inline;">
                            <input type="hidden" name="idEtu" value="<?= (int)$e['idEtu'] ?>">
                            <input type="hidden" name="idf" value="<?= (int)$idf ?>">
                            <input type="hidden" name="sem" value="<?= (int)$sem ?>">
                            <button type="submit" class="btn btn--danger btn--sm" style="padding: 4px 10px;" title="Supprimer">
                                ✕
                            </button>
                        </form>
                    <?php endif; ?>

                </td>
              <?php endif; ?>

            </tr>
          <?php endforeach; ?>
        </tbody>
      </table>
    </div>
  </div>
</div>