<div class="card" style="max-width: 600px; margin: 0 auto;">
    <h1>Ajouter un étudiant</h1>
    <p class="muted">Formation <?= (int)$idf ?> / Semestre <?= (int)$sem ?></p>

    <form method="post" class="form">
        
        <div style="display:flex; gap:15px;">
            <label style="flex:1;">
                <span>Nom</span>
                <input type="text" name="nomEtu" required placeholder="Ex: DUPONT">
            </label>
            <label style="flex:1;">
                <span>Prénom</span>
                <input type="text" name="prenomEtu" required placeholder="Ex: Marie">
            </label>
        </div>

        <div style="display:flex; gap:15px;">
            <label style="flex:1;">
                <span>Email Universitaire</span>
                <input type="email" name="emailUniEtu" required placeholder="marie.dupont@univ.fr">
            </label>
            <label style="flex:1;">
                <span>Email Perso</span>
                <input type="email" name="emailPersoEtu" required placeholder="marie@gmail.com">
            </label>
        </div>

        <div style="display:flex; gap:15px;">
             <label style="flex:1;">
                <span>Genre</span>
                <select name="genreEtu">
                    <option value="1">Homme</option>
                    <option value="0">Femme</option>
                </select>
            </label>
            <label style="flex:1;">
                <span>Type de Bac</span>
                <input type="text" name="typeBac" required placeholder="Général, STI2D...">
            </label>
        </div>

        <label>
            <span>Date de Naissance</span>
            <input type="date" name="dateNaissance" required>
        </label>
        
        <label>
            <span>Adresse Postale</span>
            <input type="text" name="addresseEtu" placeholder="12 rue de la Paix..." required>
        </label>
         <label>
            <span>Téléphone</span>
            <input type="text" name="tel" placeholder="06..." required>
        </label>

        <div style="margin-top: 20px; display: flex; gap: 10px;">
            <button class="btn" type="submit">Enregistrer l'étudiant</button>
            <a href="<?= h(url_for('etudiant/promo', ['idf'=>$idf,'sem'=>$sem])) ?>" class="btn btn--ghost">Annuler</a>
        </div>
    </form>
</div>