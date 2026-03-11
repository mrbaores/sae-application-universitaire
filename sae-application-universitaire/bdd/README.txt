PROJET R307 : 
===========================================================
Auteurs : Maxence, Mohamed, Damien
Objet   : Instructions d'installation et d'utilisation
===========================================================
README – Ordre d’exécution des scripts SQL
==========================================

Ce projet contient plusieurs scripts SQL permettant l’implémentation complète
d’une base de données (structure, données, traitements et règles métier).

Afin d’éviter toute erreur liée aux dépendances entre objets SQL, les scripts
doivent impérativement être exécutés dans l’ordre suivant.


1. Création des tables
---------------------
Fichier : creation-tables.sql

Ce script crée l’ensemble des tables de la base de données, ainsi que leurs
contraintes (clés primaires, clés étrangères, contraintes d’intégrité).

Aucun autre script ne peut être exécuté tant que les tables n’existent pas.


2. Insertion des données
-----------------------
Fichier : insert-données.sql

Ce script insère les données initiales dans les tables précédemment créées.
Il suppose que la structure de la base est déjà en place.


3. Procédures stockées
---------------------
Fichier : procedure.sql

Ce script définit les procédures stockées correspondant aux fonctionnalités
métier du projet. Les procédures manipulent les tables et les données existantes,
elles doivent donc être créées après les tables et les insertions.


4. Fonctions
------------
Fichier : functions.sql

Ce script contient les fonctions SQL utilisées pour effectuer des calculs ou
retourner des informations spécifiques. Les fonctions peuvent être utilisées
dans les requêtes, les vues ou les procédures.


5. Vues
-------
Fichier : vues.sql

Ce script crée les vues permettant une consultation simplifiée et structurée
des données. Les vues reposent sur les tables et éventuellement sur les fonctions.


6. Triggers
-----------
Fichier : Trigger.sql

Ce script définit les triggers assurant le respect automatique des règles
métier et de l’intégrité des données. Les triggers sont exécutés automatiquement
lors de certaines opérations (INSERT, UPDATE, DELETE) et doivent donc être
créés en dernier afin d’éviter des déclenchements prématurés.


Important
---------
Le non-respect de cet ordre d’exécution peut entraîner des erreurs SQL ou des
comportements inattendus liés aux dépendances entre les différents objets
de la base de données.

3. FONCTIONNALITÉS CLÉS À TESTER
--------------------------------
- Calcul de Moyenne : Consultez la vue 'Vue_Bulletin_Notes'.
- Sécurité : Tentez d'insérer une note supérieure à la note maximale 
  d'un contrôle pour voir le Trigger 'trg_possede_note_check_ins' agir.
- Sondages : Utilisez la procédure 'sp_creer_sondage' pour générer 
  un questionnaire complet en une seule ligne.

4. REMARQUES TECHNIQUES
-----------------------
- Les scripts utilisent des DELIMITER ($$ ou //) pour les procédures.
- Veillez à bien sélectionner votre base de données avant l'exécution.
- La vue 'v_qualite_groupes' utilise des fonctions statistiques 
  (STDDEV) pour l'analyse des écarts-types demandée dans le sujet.