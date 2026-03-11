package fr.iut.sae.app.algo.s3;

import fr.iut.sae.app.algo.commun.modeleAlgo;
import fr.iut.sae.app.model.dto.EtudiantDTO;

import java.util.List;

public class scoreS3 implements modeleAlgo.FonctionScore {

    public static final int TAILLE_MIN = 14, TAILLE_MAX = 18;
    public static final int FILLES_MIN = 5, GARCONS_MIN = 5;
    private static final int REDOUBLANTS_MAX = 3;

    public scoreS3() {}

    @Override public String nom() { return "Score S3"; }

    @Override public float scoreGroupe(List<EtudiantDTO> groupe) { return clamp100(100 - penaliteGroupe(groupe)); }

    public float scoreSolution(List<List<EtudiantDTO>> solution) {
        verifierStatut(solution);
        if (solution == null || solution.isEmpty()) return 0;
        int score = 100;
        for (List<EtudiantDTO> g : solution) score -= penaliteGroupe(g);
        return clamp100(score);
    }

    private static int penaliteGroupe(List<EtudiantDTO> g) {
        if (g == null) return 30;
        int n = g.size(), pen = 0;

        if (n < TAILLE_MIN) pen += (TAILLE_MIN - n) * 6;
        if (n > TAILLE_MAX) pen += (n - TAILLE_MAX) * 6;

        int filles = compterFilles(g);
        if (filles < FILLES_MIN) pen += (FILLES_MIN - filles) * 8;

        int garcons = compterGarcons(g);
        if (garcons < GARCONS_MIN) pen += (GARCONS_MIN - garcons) * 8;

        int red = compterRedoublants(g);
        if (red > REDOUBLANTS_MAX) pen += (red - REDOUBLANTS_MAX) * 10;

        return pen;
    }

    public static void verifierStatut(List<List<EtudiantDTO>> solution) {
        if (solution == null) return;
        boolean aVuApprentis = false, aVuInitiaux = false;

        for (List<EtudiantDTO> g : solution) {
            if (g == null) continue;
            for (EtudiantDTO e : g) {
                if (e == null) continue;
                if (e.getEstApprenti() == 1) aVuApprentis = true;
                else aVuInitiaux = true;
            }
        }
        if (aVuApprentis && aVuInitiaux) throw new IllegalArgumentException("Mélange alternants et initiaux.");
    }

    public static int compterFilles(List<EtudiantDTO> g) { return compterGenre(g, 1); }
    public static int compterGarcons(List<EtudiantDTO> g) { return compterGenre(g, 0); }

    private static int compterGenre(List<EtudiantDTO> g, int genreAttendu) {
        int c = 0;
        if (g == null) return 0;
        for (EtudiantDTO e : g) if (e != null && (genreAttendu == 1 ? e.getGenreEtu() == 1 : e.getGenreEtu() != 1)) c++;
        return c;
    }

    public static int compterRedoublants(List<EtudiantDTO> g) {
        int c = 0;
        if (g == null) return 0;
        for (EtudiantDTO e : g) if (e != null && e.getEstRedoublant() == 1) c++;
        return c;
    }

    public static int compterAnglo(List<EtudiantDTO> g) {
        int c = 0;
        if (g == null) return 0;
        for (EtudiantDTO e : g) if (e != null && e.getEstAnglophone() == 1) c++;
        return c;
    }

    private static int clamp100(int x) { return x < 0 ? 0 : Math.min(x, 100); }
}




