<?php $flashMsgs = flash_take(); ?>
<!doctype html>
<html lang="fr">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title><?= h(APP_NAME) ?></title>
  <link rel="stylesheet" href="css/app.css">
</head>
<body>
<header class="topbar">
  <div class="container topbar__inner">
    <div class="brand">
      <div class="brand__title"><?= h(APP_NAME) ?></div>
      <div class="brand__subtitle">Plateforme groupes TD/TP</div>
    </div>
  
    <nav class="nav">
      <?php if (!empty($_SESSION['token'])): ?>
        <a href="<?= h(url_for('home/index')) ?>">Accueil</a>
        <a href="<?= h(url_for('groupe/liste')) ?>">Groupes</a>
        <a href="<?= h(url_for('etudiant/promo')) ?>">Promo</a>
        <a href="<?= h(url_for('sondage/actifs')) ?>">Sondages</a>

        <?php if (has_role(['ETUDIANT'])): ?>
             <a href="<?= h(url_for('notes/mesNotes')) ?>" style="color: var(--primary);"> Mes Notes</a>
        <?php endif; ?>
        <?php if (has_role(['ENSEIGNANT','RESP_SEMESTRE','RESP_FORMATION'])): ?>
          <a href="<?= h(url_for('notes/import')) ?>">Notes (Import)</a>
          <a href="<?= h(url_for('admin/dashboard')) ?>">Admin</a>
        <?php endif; ?>

        <a class="nav__right" href="<?= h(url_for('auth/logout')) ?>">Déconnexion</a>
      <?php else: ?>
        <a href="<?= h(url_for('auth/login')) ?>">Connexion</a>
      <?php endif; ?>
    </nav>
  </div>
</header>

<main class="container">
  <?php foreach ($flashMsgs as $m): ?>
    <div class="flash flash--<?= h($m['type']) ?>"><?= h($m['message']) ?></div>
  <?php endforeach; ?>