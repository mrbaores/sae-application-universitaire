package fr.iut.sae.app.algo.s3;

import fr.iut.sae.app.algo.commun.modeleAlgo;
import fr.iut.sae.app.model.dto.EtudiantDTO;
import fr.iut.sae.app.algo.s3.scoreS3;

import java.util.ArrayList;
import java.util.List;

public class BruteForceS3 implements modeleAlgo.AlgorithmeGroupes {

    private static final int LIMITE = 18;

    private final int nombreGroupes;
    private List<Integer> meilleureAffectation;
    private float meilleurScore;

    public BruteForceS3(int nombreGroupes) { this.nombreGroupes = nombreGroupes; }

    @Override public String nom() { return "Brute force S3 (simple)"; }

    @Override
    public List<List<EtudiantDTO>> construireGroupes(List<EtudiantDTO> etudiants) {
        if (etudiants == null || etudiants.isEmpty()) throw new IllegalArgumentException("Liste d'étudiants vide.");
        if (nombreGroupes <= 0) throw new IllegalArgumentException("Nombre de groupes invalide : " + nombreGroupes);
        if (etudiants.size() > LIMITE) throw new IllegalArgumentException("Brute force limité à " + LIMITE + " étudiants.");

        int nbGroupes = nbGroupesReels(etudiants.size(), nombreGroupes);
        int n = etudiants.size();

        int[] affectation = new int[n];
        int[] tailles = new int[nbGroupes];
        for (int i = 0; i < n; i++) affectation[i] = -1;

        meilleureAffectation = null;
        meilleurScore = -1;

        explorer(etudiants, 0, affectation, tailles, nbGroupes);

        if (meilleureAffectation == null) throw new IllegalStateException("Aucune solution trouvée.");
        return construireDepuisAffectation(etudiants, meilleureAffectation, nbGroupes);
    }

    private void explorer(List<EtudiantDTO> etudiants, int i, int[] affectation, int[] tailles, int nbGroupes) {
        if (i == etudiants.size()) {
            List<Integer> a = new ArrayList<>();
            for (int k = 0; k < affectation.length; k++) a.add(affectation[k]);
            modeleAlgo.FonctionScore fc = new scoreS3();
            float sc = fc.scoreSolution(construireDepuisAffectation(etudiants, a, nbGroupes));
            if (meilleureAffectation == null || sc > meilleurScore) { meilleurScore = sc; meilleureAffectation = a; }
            return;
        }

        for (int g = 0; g < nbGroupes; g++) {
            if (tailles[g] >= scoreS3.TAILLE_MAX) continue;
            affectation[i] = g;
            tailles[g]++;
            explorer(etudiants, i + 1, affectation, tailles, nbGroupes);
            tailles[g]--;
        }
        affectation[i] = -1;
    }

    private List<List<EtudiantDTO>> construireDepuisAffectation(List<EtudiantDTO> etudiants, List<Integer> affectation, int nbGroupes) {
        List<List<EtudiantDTO>> groupes = new ArrayList<>();
        for (int i = 0; i < nbGroupes; i++) groupes.add(new ArrayList<>());
        for (int i = 0; i < etudiants.size(); i++) {
            int g = affectation.get(i);
            if (g >= 0 && g < nbGroupes) groupes.get(g).add(etudiants.get(i));
        }
        return groupes;
    }

    private int nbGroupesReels(int nbEtudiants, int demande) {
        int max = nbEtudiants / scoreS3.TAILLE_MIN;
        if (max <= 0) max = 1;
        return Math.min(demande, max);
    }

    public float getMeilleurScore() { return meilleurScore; }
}
