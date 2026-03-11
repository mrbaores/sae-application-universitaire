<?php

$moyenne = isset($etu['scoreGlobal']) ? number_format((float)$etu['scoreGlobal'], 2) : '—';


$styleMoyenne = ((float)$moyenne >= 10) ? 'color: var(--success);' : 'color: var(--danger);';
?>

<h1>mes resultats </h1>

<div class="grid">
  <div class="card" style="text-align: center; display: flex; flex-direction: column; justify-content: center; align-items: center;">
    <h2 style="border:none; margin-bottom: 5px; font-size: 1rem; text-transform: uppercase; color: var(--text-muted);">Moyenne Générale</h2>
    
    <div style="font-size: 3.5rem; font-weight: 800; line-height: 1; <?= $styleMoyenne ?>">
        <?= $moyenne ?>
    </div>
    <span style="font-size: 1.2rem; color: var(--text-muted); font-weight: bold;">/ 20</span>
    
    <p class="muted" style="margin-top: 15px; font-size: 0.85rem;">
        Moyenne calculée sur l'ensemble des modules.
    </p>
  </div>

  <div class="card" style="grid-column: span 2;"> <h2>Détail par matière</h2>
    
    <?php if (empty($notes)): ?>
        <p class="muted">Aucune note disponible pour le moment.</p>
    <?php else: ?>
        <div class="table-wrap">
            <table class="table">
                <thead>
                    <tr>
                        <th>Matière</th>
                        <th>Évaluation</th>
                        <th style="text-align: center;">Coef.</th>
                        <th style="text-align: right;">Note</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach ($notes as $n): ?>
                        <tr>
                            <td>
                                <div style="font-weight: bold; color: var(--primary);"><?= h($n['nomMatiere']) ?></div>
                                <span class="pill"><?= h($n['ue']) ?></span>
                            </td>
                            
                            <td><?= h($n['nomExamen']) ?></td>
                            
                            <td style="text-align: center; color: var(--text-muted);">
                                x<?= (int)$n['coefficient'] ?>
                            </td>
                            
                            <td style="text-align: right; font-weight: bold; font-size: 1.1em;">
                                <?php 
                                    $note = (float)$n['note'];
                                    $max = (int)$n['noteMaximale'];
                                    
                                    
                                    $color = ($note >= 10) ? '#10b981' : '#ef4444'; // Vert ou Rouge
                                    
                                    echo "<span style='color: $color;'>$note</span>";
                                    echo "<span style='font-size:0.8em; color:var(--text-muted); font-weight:normal;'> / $max</span>";
                                ?>
                            </td>
                        </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        </div>
    <?php endif; ?>
  </div>
</div>

<p style="margin-top: 20px;">
    <a href="<?= h(url_for('home/index')) ?>">← Retour à l'accueil</a>
</p>