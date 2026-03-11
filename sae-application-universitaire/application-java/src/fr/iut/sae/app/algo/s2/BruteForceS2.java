package fr.iut.sae.app.algo.s2;

import fr.iut.sae.app.algo.commun.modeleAlgo;
import fr.iut.sae.app.model.dto.EtudiantDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BruteForceS2 implements modeleAlgo.AlgorithmeGroupes {

    private static final int LIMITE_ETUDIANTS = 18;

    private static final int TAILLE_MIN = 15;
    private static final int TAILLE_MAX = 19;

    private final int nombreGroupes;

    private List<List<EtudiantDTO>> meilleureSolution;
    private int meilleurScore;

    public BruteForceS2(int nombreGroupes) {
        this.nombreGroupes = nombreGroupes;
    }

    @Override
    public String nom() {
        return "Brute force S2 - Damien";
    }

    @Override
    public List<List<EtudiantDTO>> construireGroupes(List<EtudiantDTO> etudiants) {
        return executer(etudiants, nombreGroupes);
    }

    public List<List<EtudiantDTO>> executer(List<EtudiantDTO> etudiants, int nbGroupes) {
        if (etudiants == null || etudiants.isEmpty()) {
            throw new IllegalArgumentException("Liste d'étudiants vide.");
        }
        if (nbGroupes <= 0) {
            throw new IllegalArgumentException("Nombre de groupes invalide.");
        }
        if (etudiants.size() > LIMITE_ETUDIANTS) {
            throw new IllegalArgumentException("Brute force limité à " + LIMITE_ETUDIANTS + " étudiants.");
        }

        int n = etudiants.size();

        int minTotal = TAILLE_MIN * nbGroupes;
        int maxTotal = TAILLE_MAX * nbGroupes;

        if (n < minTotal || n > maxTotal) {
            throw new IllegalArgumentException(
                    "Impossible : " + n + " étudiants pour " + nbGroupes + " groupes. " +
                            "Il faut entre " + minTotal + " et " + maxTotal + "."
            );
        }

        List<EtudiantDTO> ordre = new ArrayList<>(etudiants);
        ordre.sort(Comparator.comparingInt((EtudiantDTO e) -> (e != null && e.getIndiceCovoiturage() != 0 ? 0 : 1)));

        List<List<EtudiantDTO>> groupes = creerGroupesVides(nbGroupes);

        this.meilleureSolution = null;
        this.meilleurScore = Integer.MIN_VALUE;

        Map<Integer, Integer> covoitVersGroupe = new HashMap<>();

        backtrack(ordre, 0, groupes, covoitVersGroupe);

        if (meilleureSolution == null) {
            throw new IllegalStateException("Aucune solution trouvée en brute force.");
        }
        return meilleureSolution;
    }

    private void backtrack(List<EtudiantDTO> ordre, int index, List<List<EtudiantDTO>> groupes, Map<Integer, Integer> covoitVersGroupe) {
        if (index == ordre.size()) {
            if (!contraintesFinalesOK(groupes)) return;

            int score = scoreSolution(groupes);
            if (score > meilleurScore) {
                meilleurScore = score;
                meilleureSolution = clonerSolution(groupes);
            }
            return;
        }

        EtudiantDTO e = ordre.get(index);

        List<Integer> indices = indicesGroupesTriesParTaille(groupes);

        for (int i = 0; i < indices.size(); i++) {
            int idxG = indices.get(i);
            List<EtudiantDTO> g = groupes.get(idxG);

            if (g.size() >= TAILLE_MAX) continue;

            if (!respectCovoitAvecMap(e, idxG, covoitVersGroupe)) continue;

            Integer old = appliquerCovoitMap(e, idxG, covoitVersGroupe);
            g.add(e);

            backtrack(ordre, index + 1, groupes, covoitVersGroupe);

            g.remove(g.size() - 1);
            annulerCovoitMap(e, old, covoitVersGroupe);
        }
    }

    private boolean respectCovoitAvecMap(EtudiantDTO e, int idxG, Map<Integer, Integer> covoitVersGroupe) {
        if (e == null) return true;
        int covoit = e.getIndiceCovoiturage();
        if (covoit == 0) return true;

        Integer deja = covoitVersGroupe.get(covoit);
        if (deja == null) return true;

        return deja == idxG;
    }

    private Integer appliquerCovoitMap(EtudiantDTO e, int idxG, Map<Integer, Integer> covoitVersGroupe) {
        if (e == null) return null;
        int covoit = e.getIndiceCovoiturage();
        if (covoit == 0) return null;

        Integer old = covoitVersGroupe.get(covoit);
        covoitVersGroupe.put(covoit, idxG);
        return old;
    }

    private void annulerCovoitMap(EtudiantDTO e, Integer old, Map<Integer, Integer> covoitVersGroupe) {
        if (e == null) return;
        int covoit = e.getIndiceCovoiturage();
        if (covoit == 0) return;

        if (old == null) covoitVersGroupe.remove(covoit);
        else covoitVersGroupe.put(covoit, old);
    }

    private boolean contraintesFinalesOK(List<List<EtudiantDTO>> groupes) {
        for (int i = 0; i < groupes.size(); i++) {
            int t = groupes.get(i).size();
            if (t < TAILLE_MIN || t > TAILLE_MAX) return false;
        }
        return true;
    }

    private int scoreSolution(List<List<EtudiantDTO>> groupes) {
        int total = 0;

        int tailleCible = 0;
        for (int i = 0; i < groupes.size(); i++) tailleCible += groupes.get(i).size();
        tailleCible = (int) Math.round(tailleCible / (double) groupes.size());

        int totalAnglo = 0;
        for (int i = 0; i < groupes.size(); i++) totalAnglo += compterAnglo(groupes.get(i));
        int angloCible = (int) Math.round(totalAnglo / (double) groupes.size());

        for (int i = 0; i < groupes.size(); i++) {
            List<EtudiantDTO> g = groupes.get(i);

            int score = 100;

            score -= Math.abs(g.size() - tailleCible) * 8;

            int filles = compterFilles(g);
            if (filles > 0 && filles < 4) score -= 25;

            int anglo = compterAnglo(g);
            score -= Math.abs(anglo - angloCible) * 6;

            total += score;
        }

        return total;
    }

    private int compterFilles(List<EtudiantDTO> groupe) {
        int nb = 0;
        for (int i = 0; i < groupe.size(); i++) {
            EtudiantDTO e = groupe.get(i);
            if (e != null && e.getGenreEtu() == 1) nb++;
        }
        return nb;
    }

    private int compterAnglo(List<EtudiantDTO> groupe) {
        int nb = 0;
        for (int i = 0; i < groupe.size(); i++) {
            EtudiantDTO e = groupe.get(i);
            if (e != null && e.getEstAnglophone() == 1) nb++;
        }
        return nb;
    }

    private List<List<EtudiantDTO>> creerGroupesVides(int nb) {
        List<List<EtudiantDTO>> out = new ArrayList<>();
        for (int i = 0; i < nb; i++) out.add(new ArrayList<>());
        return out;
    }

    private List<Integer> indicesGroupesTriesParTaille(List<List<EtudiantDTO>> groupes) {
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < groupes.size(); i++) idx.add(i);
        idx.sort(Comparator.comparingInt(i -> groupes.get(i).size()));
        return idx;
    }

    private List<List<EtudiantDTO>> clonerSolution(List<List<EtudiantDTO>> source) {
        List<List<EtudiantDTO>> copie = new ArrayList<>();
        for (int i = 0; i < source.size(); i++) {
            List<EtudiantDTO> g = source.get(i);
            List<EtudiantDTO> ng = new ArrayList<>();
            for (int j = 0; j < g.size(); j++) ng.add(g.get(j));
            copie.add(ng);
        }
        return copie;
    }
}
