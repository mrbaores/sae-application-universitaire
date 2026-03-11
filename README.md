# SAE Application Universitaire

Projet universitaire de gestion et de constitution de groupes de TD/TP.

Le dépôt regroupe quatre briques complémentaires :
- une **base de données MySQL** ;
- une **API REST PHP** ;
- un **site web PHP MVC** ;
- une **application Java Swing** cliente.

## Architecture

```text
Application Java / Site web
          ↓
       API REST
          ↓
   Base de données MySQL
```

Le site web et l'application Java **ne communiquent pas directement** avec la base de données. Ils passent par l'API REST, qui centralise l'accès aux données.

## Structure du dépôt

```text
sae-application-universitaire/
├── README.md
├── .gitignore
├── docs/
├── bdd/
├── api-rest/
├── site-web/
└── application-java/
```

## Pré-requis

Pour lancer le projet, il faut au minimum :
- **MySQL** ou **MariaDB** ;
- **PHP** ;
- un **serveur web** compatible PHP (Apache, XAMPP, WAMP, MAMP, etc.) ;
- **Java 17+** ;
- un IDE Java ou une commande `javac/java`.

## Installation et exécution

### 1. Créer la base de données
Créer une base MySQL, par exemple :

```sql
CREATE DATABASE sae_groupes CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Importer ensuite les scripts du dossier `bdd/` dans cet ordre recommandé :

1. `creation-tables.sql`
2. `insert-donnees.sql`
3. `functions.sql`
4. `procedure.sql`
5. `procedure-complements.sql`
6. `vues.sql`
7. `triggers.sql`

### 2. Héberger l'API REST
Déployer le dossier `api-rest/` sur un serveur web PHP.

Exemples :
- XAMPP : `C:\xampp\htdocs\api-rest`
- Linux/Apache : `/var/www/html/api-rest`

Configurer ensuite `api-rest/config.php` avec vos paramètres MySQL :

```php
'host' => 'localhost',
'name' => 'sae_groupes',
'user' => 'root',
'pass' => '',
```

### 3. Tester l'API REST
Avant de lancer le site web ou l'application Java, vérifier que l'API répond :
- en ouvrant `api-rest/index.php` dans le navigateur ;
- ou en utilisant `api-rest/api-tester.html` ;
- ou en testant directement un endpoint de l'API.

### 4. Héberger le site web
Déployer le dossier `site-web/` sur un serveur web PHP.

Le point d'entrée du site est le dossier `public/`.

Configurer ensuite l'URL de l'API dans le fichier du site web prévu à cet effet (par exemple `config/config.php`) afin qu'elle pointe vers votre API REST déployée.

Exemple :

```php
define('API_BASE_URL', 'http://localhost/api-rest');
```

### 5. Lancer l'application Java
L'application Java est une cliente qui appelle l'API REST.

- Ouvrir le dossier `application-java/` dans votre IDE.
- Compiler le projet.
- Lancer la classe principale : `fr.iut.sae.app.main.App`.
- Au démarrage, renseigner l'URL de l'API si nécessaire.

L'écran de connexion contient déjà un champ d'URL d'API : cela permet d'adapter facilement l'application à votre environnement.

## Ordre recommandé de démarrage

1. Base de données MySQL
2. API REST PHP
3. Site web PHP
4. Application Java

## Comptes de test

Selon les jeux de données importés, le projet peut contenir des comptes de test. Vérifier la base de données ou les données d'initialisation importées pour connaître les identifiants disponibles.

## Conseils de vérification

Si quelque chose ne fonctionne pas, vérifier dans cet ordre :
- la base MySQL existe bien ;
- les scripts SQL ont été importés dans le bon ordre ;
- `api-rest/config.php` contient les bons identifiants ;
- l'API REST répond dans le navigateur ;
- le site web pointe vers la bonne URL d'API ;
- l'application Java utilise la bonne URL d'API.

## Remarque importante

Le fichier `api-rest/config.php` présent dans ce dépôt a été neutralisé pour GitHub : il faut y remettre vos propres paramètres de connexion avant exécution.
