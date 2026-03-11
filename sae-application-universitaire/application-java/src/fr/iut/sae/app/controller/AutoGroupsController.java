package fr.iut.sae.app.controller;

import fr.iut.sae.app.algo.commun.modeleAlgo;
import fr.iut.sae.app.model.dto.EtudiantDTO;
import fr.iut.sae.app.model.dto.GroupeDTO;
import fr.iut.sae.app.model.state.AppSession;
import fr.iut.sae.app.service.EtudiantService;
import fr.iut.sae.app.service.GroupeService;
import fr.iut.sae.app.view.AutoGroupsPanel;

import javax.swing.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class AutoGroupsController {

    private static final int TAILLE_MIN = 14;
    private static final int LIMITE_BRUTE_FORCE = 22;

    private final AutoGroupsPanel view;
    private final AppSession session;
    private final EtudiantService etudiantService;
    private final GroupeService groupeService;

    private final List<modeleAlgo.AlgorithmeGroupes> algorithmes = new ArrayList<>();
    private modeleAlgo.FonctionScore score;

    public AutoGroupsController(AutoGroupsPanel view, AppSession session, EtudiantService etudiantService, GroupeService groupeService) {
        this.view = view;
        this.session = session;
        this.etudiantService = etudiantService;
        this.groupeService = groupeService;

        wire();
        onContextChanged();
    }

    private void wire() {
        view.getBtnRafraichir().addActionListener(e -> {
            view.clearLog();
            onContextChanged();
        });
        view.getBtnLancer().addActionListener(e -> lancerEtEcrireEnBaseAsync());
    }

    public void onContextChanged() {
        int semestre = session.getNumSemestre();

        algorithmes.clear();
        score = null;

        // IMPORTANT : ton AutoGroupsPanel n'a pas setTitre() chez toi actuellement.
        // Donc je n'appelle PAS view.setTitre(...) ici pour éviter l'erreur.
        // Si tu veux, je te réécris AutoGroupsPanel ensuite avec setTitre.

        if (semestre == 1) {
            chargerAlgorithmesParReflexion(
                    "Mohamed (S1)",
                    new String[]{
                            "fr.iut.sae.app.algo.s1.GloutonS1_1",
                            "fr.iut.sae.app.algo.s1.GloutonS1_2",
                            "fr.iut.sae.app.algo.s1.BruteForceS1"
                    },
                    "fr.iut.sae.app.algo.s1.scoreS1"
            );
        } else if (semestre == 2) {
            chargerAlgorithmesParReflexion(
                    "Damien (S2)",
                    new String[]{
                            "fr.iut.sae.app.algo.s2.GloutonS2_1",
                            "fr.iut.sae.app.algo.s2.GloutonS2_2",
                            "fr.iut.sae.app.algo.s2.BruteForceS2"
                    },
                    "fr.iut.sae.app.algo.s2.scoreS2"
            );
        } else if (semestre == 3 || semestre == 4 || semestre == 5 || semestre == 6) {
            chargerAlgorithmesParReflexion(
                    "Maxence (S3-S6)",
                    new String[]{
                            "fr.iut.sae.app.algo.s3.GloutonS3_1",
                            "fr.iut.sae.app.algo.s3.GloutonS3_2",
                            "fr.iut.sae.app.algo.s3.BruteForceS3"
                    },
                    "fr.iut.sae.app.algo.s3.scoreS3"
            );
        }

        view.setAlgorithmes(algorithmes);

        if (algorithmes.isEmpty()) {
            view.appendLog("ℹ️ Aucun algorithme disponible pour S" + semestre + ".");
            view.appendLog("➡️ Vérifie les noms de classes / packages pour ce semestre.");
        } else {
            view.appendLog("✅ Algorithmes chargés pour S" + semestre + " : " + algorithmes.size());
        }
    }

    private void chargerAlgorithmesParReflexion(String nomEquipe, String[] classesAlgo, String classeScore) {
        int nbGroupes = 1; // valeur par défaut (les vrais nb groupes seront passés au moment du lancement)

        for (int i = 0; i < classesAlgo.length; i++) {
            modeleAlgo.AlgorithmeGroupes a = creerAlgoParReflexion(classesAlgo[i], nbGroupes);
            if (a != null) {
                algorithmes.add(a);
            }
        }

        score = creerScoreParReflexion(classeScore);

        if (score == null) {
            view.appendLog("⚠️ Score introuvable pour " + nomEquipe + " : " + classeScore);
        }
    }

    private modeleAlgo.AlgorithmeGroupes creerAlgoParReflexion(String className, int nbGroupes) {
        try {
            Class<?> c = Class.forName(className);

            if (!modeleAlgo.AlgorithmeGroupes.class.isAssignableFrom(c)) {
                view.appendLog("⚠️ " + className + " n'implémente pas AlgorithmeGroupes.");
                return null;
            }

            // On tente constructeur(int)
            try {
                Constructor<?> ctor = c.getConstructor(int.class);
                Object obj = ctor.newInstance(nbGroupes);
                return (modeleAlgo.AlgorithmeGroupes) obj;
            } catch (NoSuchMethodException ex) {
                // Sinon constructeur vide
                Object obj = c.getConstructor().newInstance();
                return (modeleAlgo.AlgorithmeGroupes) obj;
            }

        } catch (Exception e) {
            // On ne casse pas l'application si un algo manque
            return null;
        }
    }

    private modeleAlgo.FonctionScore creerScoreParReflexion(String className) {
        try {
            Class<?> c = Class.forName(className);

            if (!modeleAlgo.FonctionScore.class.isAssignableFrom(c)) {
                return null;
            }

            Object obj = c.getConstructor().newInstance();
            return (modeleAlgo.FonctionScore) obj;

        } catch (Exception e) {
            return null;
        }
    }

    private void lancerEtEcrireEnBaseAsync() {
        int semestre = session.getNumSemestre();

        if (!(semestre == 1 || semestre == 2 || semestre == 3 || semestre == 4 || semestre == 5 || semestre == 6)) {
            view.showError("Le mode automatique n'est activé que pour S1 à S6 (selon tes algorithmes).");
            return;
        }

        int nbGroupesDemandes = view.getNombreGroupesSaisi();
        if (nbGroupesDemandes <= 0) {
            view.showError("Entre un nombre de groupes valide.");
            return;
        }

        if (algorithmes.isEmpty() || score == null) {
            view.showError("Aucun algorithme / score chargé pour ce semestre.");
            return;
        }

        view.setBusy(true);

        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {

            private Exception error;

            @Override
            protected Void doInBackground() {
                try {
                    int idFormation = session.getIdFormation();
                    int numSemestre = session.getNumSemestre();

                    int indexAlgo = view.getAlgoSelectionneIndex();
                    if (indexAlgo < 0) indexAlgo = 0;
                    if (indexAlgo >= algorithmes.size()) indexAlgo = 0;

                    // Recréer l'instance d'algo avec le bon nombre de groupes (important si tes algos ont un ctor(int))
                    modeleAlgo.AlgorithmeGroupes algo = recreerAlgoAvecNbGroupes(algorithmes.get(indexAlgo), nbGroupesDemandes);
                    if (algo == null) algo = algorithmes.get(indexAlgo);

                    List<EtudiantDTO> tous = etudiantService.listEtudiants(idFormation, numSemestre);
                    List<EtudiantDTO> sansGroupe = filtrerSansGroupe(tous);

                    if (sansGroupe.size() < TAILLE_MIN) {
                        throw new IllegalStateException("Seulement " + sansGroupe.size() + " étudiants sans groupe. Il faut au moins " + TAILLE_MIN + ".");
                    }

                    // Objectif : groupes de 14 pour correction manuelle ensuite
                    int maxGroupesPossibles = sansGroupe.size() / TAILLE_MIN;
                    int nbGroupesReels = nbGroupesDemandes;
                    if (nbGroupesReels > maxGroupesPossibles) nbGroupesReels = maxGroupesPossibles;
                    if (nbGroupesReels <= 0) nbGroupesReels = 1;

                    int nbEtudiantsAUtiliser = nbGroupesReels * TAILLE_MIN;

                    boolean estBruteForce = algo.nom().toLowerCase().contains("brute");
                    if (estBruteForce && nbEtudiantsAUtiliser > LIMITE_BRUTE_FORCE) {
                        nbEtudiantsAUtiliser = LIMITE_BRUTE_FORCE;
                        nbGroupesReels = Math.max(1, nbEtudiantsAUtiliser / TAILLE_MIN);
                        nbEtudiantsAUtiliser = nbGroupesReels * TAILLE_MIN;

                        algo = recreerAlgoAvecNbGroupes(algo, nbGroupesReels);
                        if (algo == null) {
                            throw new IllegalStateException("Brute force : impossible de recréer l'algo avec " + nbGroupesReels + " groupes.");
                        }
                    } else {
                        algo = recreerAlgoAvecNbGroupes(algo, nbGroupesReels);
                        if (algo == null) {
                            // si l'algo n'a pas ctor(int), tant pis
                            algo = algorithmes.get(indexAlgo);
                        }
                    }

                    List<EtudiantDTO> utilises = new ArrayList<>();
                    List<EtudiantDTO> restants = new ArrayList<>();

                    for (int i = 0; i < sansGroupe.size(); i++) {
                        EtudiantDTO e = sansGroupe.get(i);
                        if (utilises.size() < nbEtudiantsAUtiliser) utilises.add(e);
                        else restants.add(e);
                    }

                    List<GroupeDTO> groupesExistants = groupeService.listGroupes(idFormation, numSemestre);
                    int prochainNumeroG = trouverProchainNumero(groupesExistants);

                    publish("S" + numSemestre + " | Algo: " + algo.nom());
                    publish("Sans groupe: " + sansGroupe.size() + " | Groupes créés: " + nbGroupesReels + " | Affectés auto: " + utilises.size() + " | Restants: " + restants.size());

                    List<List<EtudiantDTO>> solution = algo.construireGroupes(tous);

                    if (solution == null || solution.isEmpty()) {
                        throw new IllegalStateException("Aucun groupe généré.");
                    }

                    float scoreSolution = score.scoreSolution(solution);
                    publish("Score solution : " + scoreSolution + " / 100");
                    publish("Détails :");

                    for (int i = 0; i < solution.size(); i++) {
                        String nomGroupe = "G" + (prochainNumeroG + i);
                        List<EtudiantDTO> g = solution.get(i);

                        float sc = score.scoreGroupe(g);
                        int taille = g.size();
                        int filles = compterFilles(g);
                        int redoublants = compterRedoublants(g);
                        int anglo = compterAnglophones(g);

                        publish("• " + nomGroupe + " | taille=" + taille + " | filles=" + filles + " | redoublants=" + redoublants + " | anglo=" + anglo + " | score=" + sc + "/100");

                        // Base : créer groupe + affecter
                        groupeService.createGroupe(idFormation, numSemestre, nomGroupe);
                        for (int j = 0; j < g.size(); j++) {
                            EtudiantDTO etu = g.get(j);
                            etudiantService.setGroupe(etu.getIdEtu(), idFormation, numSemestre, nomGroupe);
                        }
                    }

                    publish("✅ Base mise à jour : groupes créés + étudiants affectés.");
                    publish("ℹ️ Restants à corriger manuellement : " + restants.size());

                } catch (Exception ex) {
                    error = ex;
                }

                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (int i = 0; i < chunks.size(); i++) {
                    view.appendLog(chunks.get(i));
                }
            }

            @Override
            protected void done() {
                view.setBusy(false);

                if (error != null) {
                    view.showError(error.getMessage());
                    view.appendLog("Erreur : " + error.getMessage());
                }
            }
        };

        worker.execute();
    }

    private modeleAlgo.AlgorithmeGroupes recreerAlgoAvecNbGroupes(modeleAlgo.AlgorithmeGroupes algo, int nbGroupes) {
        if (algo == null) return null;

        try {
            Class<?> c = algo.getClass();

            if (!modeleAlgo.AlgorithmeGroupes.class.isAssignableFrom(c)) {
                return null;
            }

            try {
                Constructor<?> ctor = c.getConstructor(int.class);
                Object obj = ctor.newInstance(nbGroupes);
                return (modeleAlgo.AlgorithmeGroupes) obj;
            } catch (NoSuchMethodException ex) {
                return algo; // l'algo n'a pas ctor(int), on garde la même instance
            }

        } catch (Exception e) {
            return null;
        }
    }

    private List<EtudiantDTO> filtrerSansGroupe(List<EtudiantDTO> tous) {
        List<EtudiantDTO> out = new ArrayList<>();
        if (tous == null) return out;

        for (int i = 0; i < tous.size(); i++) {
            EtudiantDTO e = tous.get(i);
            if (e == null) continue;

            String g = e.getNomGroupe();
            if (g == null || g.isBlank()) out.add(e);
        }
        return out;
    }

    private int trouverProchainNumero(List<GroupeDTO> groupesExistants) {
        int max = 0;
        if (groupesExistants == null) return 1;

        for (int i = 0; i < groupesExistants.size(); i++) {
            GroupeDTO g = groupesExistants.get(i);
            if (g == null) continue;

            int num = extraireNumeroG(g.getNomGroupe());
            if (num > max) max = num;
        }
        return max + 1;
    }

    private int extraireNumeroG(String nomGroupe) {
        if (nomGroupe == null) return 0;

        String s = nomGroupe.trim();
        if (s.length() < 2) return 0;

        char c0 = s.charAt(0);
        if (c0 != 'G' && c0 != 'g') return 0;

        String reste = s.substring(1).trim();
        if (reste.isEmpty()) return 0;

        try {
            return Integer.parseInt(reste);
        } catch (Exception e) {
            return 0;
        }
    }

    private int compterFilles(List<EtudiantDTO> groupe) {
        int nb = 0;
        if (groupe == null) return 0;

        for (int i = 0; i < groupe.size(); i++) {
            EtudiantDTO e = groupe.get(i);
            if (e != null && e.getGenreEtu() == 1) nb++;
        }
        return nb;
    }

    private int compterRedoublants(List<EtudiantDTO> groupe) {
        int nb = 0;
        if (groupe == null) return 0;

        for (int i = 0; i < groupe.size(); i++) {
            EtudiantDTO e = groupe.get(i);
            if (e != null && e.getEstRedoublant() == 1) nb++;
        }
        return nb;
    }

    private int compterAnglophones(List<EtudiantDTO> groupe) {
        int nb = 0;
        if (groupe == null) return 0;

        for (int i = 0; i < groupe.size(); i++) {
            EtudiantDTO e = groupe.get(i);
            if (e != null && e.getEstAnglophone() == 1) nb++;
        }
        return nb;
    }
}
