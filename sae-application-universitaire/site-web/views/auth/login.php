<div style="display: flex; justify-content: center; padding-top: 50px;">
    
    <form method="post" class="card form" style="width: 100%; max-width: 500px; padding: 40px;">
        
        <h1 style="text-align: center; margin-bottom: 30px; color: var(--primary);">Connexion</h1>

        <label>
            <span style="font-size: 1.1em;">Login</span>
            <input type="text" name="login" value="<?= h($login ?? '') ?>" autocomplete="username" required style="padding: 15px;">
        </label>

        <label style="margin-top: 20px;">
            <span style="font-size: 1.1em;">Mot de passe</span>
            <input type="password" name="password" autocomplete="current-password" required style="padding: 15px;">
        </label>

        <button class="btn" type="submit" style="width: 100%; margin-top: 30px; padding: 15px; font-size: 1.2em;">
            Se connecter
        </button>

    </form>

</div>