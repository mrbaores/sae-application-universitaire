README – Création de groupes d’étudiants en Java
Veuillez attendre 13h pour avoir l'explication pour lancer l'application. 
Le compte admin est "responsable" et à pour mot de passe "test 123".
1. Modes de création de groupes
1.1 Création gloutonne avec contraintes complètes

Méthode :
    Glouton1(int nbrGroupes, Semestre semestre, ArrayList<Etudiant> etudiantsCpy) par Damien Braconnier :

Principe :
    Cet algorithme utilise une approche gloutonne, minimisant la somme des différences de score entre chaque groupe et les étudiants.

Étapes principales :

    Vérification de la validité du nombre de groupes

    Calcul de la taille cible des groupes

    Identification des groupes de covoiturage (qui doivent rester ensemble)
    
    Séparation des covoiturages possedant au moins un étudiant anglophone

    Ajout des coivoiturages anglophone dans les deux premiers groupes et les autres dans tout les groupes. La façon dont le score est calculé oblige les groupes non anglophone a allé dans les groupes vide.

    Ajout des etudiants restant de la meme façon.

    Retourne les groupes créer

Placement :

    des groupes de covoiturage en priorité,

    des étudiants anglophones,

    puis des autres étudiants

    Le choix du groupe se fait à chaque fois via la méthode Semestre.GroupeScoreMinimal(...) qui sélectionne le groupe optimisant les critères définis.

Avantages :

    Rapide

    Respecte les contraintes

    Bon équilibrage global

Limites :

    Ne garantit pas une solution optimale globale
    
    contrainte élevée pour les groupes anglo et peut ne pas trouver de solution car les filles sont aussi mises dedans
    
1.2 Création gloutonne avec contraintes allégées par Damien Braconnier :

Méthode :
    Glouton2(int nbrGroupes, int nbrGroupesFilles, Semestre semestre, ArrayList<Etudiant> etudiantsCpy)

Différence avec la version précédente :
    Cette version introduit une contrainte plus souple sur la répartition des filles, en autorisant un nombre maximal de groupes contenant des filles.

Fonctionnement :

    Identique à la version gloutonne classique

    La méthode GroupeScoreMinimal prend en compte le nombre de groupes autorisés avec des filles

Objectif :

    Améliorer la faisabilité dans des cas où les contraintes strictes rendent la création impossible ou une baisse de score.

1.3 Création par force brute (brute force) par Damien Braconnier 

Méthode :
    bruteForce(ArrayList<Etudiant> etudiants, int nbrGroupes, Semestre semestre)

Principe :
    Cet algorithme teste toutes les partitions possibles des étudiants en groupes, puis sélectionne celle ayant le meilleur score global.

Étapes :

    Génération de toutes les partitions possibles (generatePartitions)

    Vérification des contraintes pour chaque partition :

        taille minimale des groupes,

        répartition des filles au moins 4 ou aucune,

        respect du covoiturage (RespectCovoiturage)

        Calcul du score global

Sélection de la meilleure partition

Avantages :

Garantit la solution optimale

Limites majeures :

Inutilisable


3. Conclusion

    Ce projet compare une approche gloutonne efficace à une approche optimale mais coûteuse, permettant de mieux comprendre les compromis entre performance et optimalité dans les algorithmes de création de groupes.








README — Algorithmes S3 (constitution automatique de groupes)

Ce dossier contient 3 algorithmes de génération de groupes pour le semestre S3 : deux heuristiques gloutonnes (rapides) et un brute force (très coûteux, limité à un petit nombre d’étudiants).
Chaque algorithme implémente l’interface modeleAlgo.AlgorithmeGroupes et renvoie une structure List<List<EtudiantDTO>> (liste de groupes).

Les tailles de groupes respectent la contrainte générale du projet : entre 14 et 18 étudiants (scoreS3.TAILLE_MIN et scoreS3.TAILLE_MAX).
Les contraintes “filles/garçons minimum” sont des objectifs : si ce n’est pas possible avec les étudiants restants, l’algorithme complète quand même les groupes.

1) GloutonS3_1 — Glouton simple (remplissage équilibré)

But : créer rapidement nombreGroupes groupes en essayant de les équilibrer en taille.

Principe :

- On copie les étudiants dans une liste restants.
- On construit les groupes un par un.

Pour chaque groupe, on calcule une taille cible (entre 14 et 18) pour éviter que les derniers groupes soient trop petits.

On ajoute les étudiants en priorité pour tendre vers :

- MIN_FILLES = 5
- MIN_GARCONS = 5

On limite les redoublants à MAX_REDOUBLANTS_PAR_GROUPE = 3 (si possible).

Si on n’arrive pas à atteindre 14 avec les contraintes, on autorise un ajout “secours” pour compléter le groupe.

Complexité : rapide (adapté à une vraie promotion).

2) GloutonS3_2 — Glouton avec filtre apprentis + covoiturage

But : même objectif que le glouton 1, mais avec des contraintes supplémentaires.

Règles ajoutées :

Homogénéité apprentissage :
- On choisit un “mode” selon le premier étudiant :

soit on garde uniquement les apprentis

soit on garde uniquement les initiaux
(donc le résultat est homogène sur ce statut)

Covoiturage :
- Si un étudiant a un indiceCovoiturage > 0, on cherche tous ceux qui ont le même indice et on tente d’ajouter le pack (au moins 2 personnes) en une fois, si ça  -- rentre dans le groupe (sans dépasser TAILLE_MAX et en respectant la limite redoublants).

Ensuite, comme GloutonS3_1, on essaie d’atteindre :

MIN_FILLES = 5

MIN_GARCONS = 5
et on limite les redoublants.

Complexité : rapide, un peu plus lourd que S3_1 mais utilisable en production.

3) BruteForceS3 — Recherche exhaustive (très coûteuse)

But : tester un maximum de répartitions possibles pour trouver la solution avec le meilleur score (via scoreS3.scoreSolution).

Principe :

On explore récursivement toutes les affectations possibles :

soit un étudiant n’est pas placé (-1)

soit il est placé dans un des groupes 0..nbGroupes-1

On respecte TAILLE_MAX pendant la construction (pas plus de 18).

À la fin (quand tous les étudiants sont décidés), on :

- construit la solution (liste de groupes)
- calcule le score
- garde la meilleure affectation trouvée

Limite importante :

LIMITE_ETUDIANTS = 18 obligatoire, sinon le nombre de possibilités explose (freeze assuré).

Complexité : exponentielle, utilisable uniquement sur de petits tests.


README – Algorithmes S1 bouremani Mohamed 
Ce module contient 3 algorithmes de génération de groupes pour le semestre S1. Ils implémentent l'interface AlgorithmeGroupes et sont évalués par la classe ScoreS1 (règles : taille entre 14-17, min 3 filles, idéalement 8 bacheliers technologiques).

1. Glouton S1 (Distribution Équilibrée)
Classe : GloutonS1.java

Principe : Cet algorithme privilégie l'équilibre parfait des tailles de groupes et assure la contrainte de mixité minimale de manière simple.

Étapes principales :

Séparation : On divise la liste principale en deux listes : les Filles et les Autres.

Distribution des Filles : On distribue les filles une par une dans chaque groupe (méthode circulaire) pour maximiser les chances d'atteindre le quota de 3 par groupe.

Remplissage (Équilibrage) : Pour tous les étudiants restants, l'algorithme cherche systématiquement le groupe le plus petit actuellement pour y ajouter l'étudiant.

Avantages :

Très rapide.

Garantit des groupes de tailles quasi-identiques.

Assure une répartition homogène des filles.

Limites :

Ne prend pas du tout en compte l'origine scolaire (Bac Techno vs Général). Le score sera faible sur le critère "Diversité".

2. Glouton Quotas (Priorité au Bac Techno)
Classe : GloutonQuotas.java

Principe : Cette version est plus complexe et tente de "forcer" les contraintes spécifiques du barème S1 (notamment le nombre de bacheliers technologiques).

Étapes principales :

Catégorisation : Les étudiants sont classés en 4 listes : Filles Techno, Filles Général, Garçons Techno, Garçons Général.

Sécuriser la mixité : On insère d'abord 3 filles dans chaque groupe, en puisant en priorité dans les "Filles Techno" pour commencer à remplir le quota "Bac".

Cible Techno : On ajoute des "Garçons Techno" dans les groupes jusqu'à atteindre le chiffre idéal de 8 technos par groupe.

Remplissage final : Tous les étudiants restants sont distribués circulairement dans les groupes ayant encore de la place (< 17).


3. Brute Force S1 (Recherche Exhaustive)
Classe : BruteForceS1.java

Principe : Cet algorithme teste toutes les combinaisons possibles d'affectation pour trouver la solution mathématiquement parfaite selon le ScoreS1.

Méthode :

Utilise le Backtracking (récursivité).

Place un étudiant, teste la suite, revient en arrière si nécessaire.

Mémorise la solution ayant le meilleur score rencontré.

Avantages :

Trouve la solution optimale absolue (Score maximal possible).

Limites majeures :

Complexité exponentielle.

Inutilisable sur une classe entière.

Sécurité activée : L'algorithme renvoie null si la liste contient plus de 20 étudiants, pour éviter de faire planter l'application (freeze infini)



