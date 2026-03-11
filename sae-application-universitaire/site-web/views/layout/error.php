<h1><?= h($title ?? 'Erreur') ?></h1>
<p class="muted"><?= h($message ?? 'Une erreur est survenue.') ?></p>
<p><a href="<?= h(url_for('home/index')) ?>">Retour</a></p>
