<h1>Tableau de bord</h1>
<p class="muted">Accès enseignant / responsables.</p>

<div class="grid">
  <div class="card">
    <h2>Groupes</h2>
    <p>Consulter la liste des groupes et le détail.</p>
    <a class="btn" href="<?= h(url_for('groupe/liste')) ?>">Ouvrir</a>
  </div>

  <div class="card">
    <h2>Sondages</h2>
    <p>Créer un sondage et suivre la participation.</p>
    <a class="btn" href="<?= h(url_for('sondage/creer')) ?>">Créer</a>
  </div>

  <div class="card">
    <h2>Notes</h2>
    <p>Importer des notes au format CSV.</p>
    <a class="btn" href="<?= h(url_for('notes/import')) ?>">Importer</a>
  </div>
   <div class="card">
    <h2>📂 Export des données</h2>
    <p>Télécharger la liste des étudiants et leurs groupes au format CSV.</p>
    
    <form method="get" action="index.php" class="form">
        <input type="hidden" name="r" value="notes/export">
        
        <div style="display: flex; gap: 10px; margin-bottom: 10px;">
            <label style="flex: 1;">
                <span>Formation (ID)</span>
                <input type="number" name="idf" value="1" min="1" required>
            </label>

            <label style="flex: 1;">
                <span>Semestre</span>
                <select name="sem">
                    <option value="1">S1</option>
                    <option value="2">S2</option>
                    <option value="3">S3</option>
                    <option value="4">S4</option>
                    <option value="5">S5</option>
                    <option value="6">S6</option>
                </select>
            </label>
        </div>

        <button class="btn" type="submit">
            📥 Télécharger le CSV
        </button>
    </form>
  </div>
  
  
</div>
