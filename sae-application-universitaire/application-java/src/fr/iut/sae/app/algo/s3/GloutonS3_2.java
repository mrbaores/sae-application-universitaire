package fr.iut.sae.app.algo.s3;

import fr.iut.sae.app.algo.commun.modeleAlgo;
import fr.iut.sae.app.model.dto.EtudiantDTO;

import java.util.ArrayList;
import java.util.List;

public class GloutonS3_2 implements modeleAlgo.AlgorithmeGroupes {

    private final int nombreGroupes;
    private static final int TMIN = 14, TMAX = 18, MIN_FILLES = 5, MIN_GARCONS = 5, MAX_REDOUBLANTS = 3;

    public GloutonS3_2(int nombreGroupes) { this.nombreGroupes = nombreGroupes; }
    @Override public String nom() { return "Glouton S3_2 "; }

    @Override
    public List<List<EtudiantDTO>> construireGroupes(List<EtudiantDTO> etudiants) {
        List<List<EtudiantDTO>> groupes = new ArrayList<>();
        if (etudiants == null || etudiants.isEmpty() || nombreGroupes <= 0) return groupes;

        boolean apprentis = etudiants.get(0) != null && etudiants.get(0).getEstApprenti() == 1;
        List<EtudiantDTO> restants = new ArrayList<>();
        for (EtudiantDTO e : etudiants) if (e != null && (e.getEstApprenti() == 1) == apprentis) restants.add(e);

        for (int g = 0; g < nombreGroupes && !restants.isEmpty(); g++) {
            int tailleVisee = (int) Math.ceil(restants.size() / (double) (nombreGroupes - g));
            if (tailleVisee < TMIN) tailleVisee = TMIN;
            if (tailleVisee > TMAX) tailleVisee = TMAX;

            List<EtudiantDTO> groupe = new ArrayList<>();
            while (groupe.size() < tailleVisee && !restants.isEmpty()) {
                boolean besoinFille = compterFilles(groupe) < MIN_FILLES;
                boolean besoinGarcon = compterGarcons(groupe) < MIN_GARCONS;
                int i = choisir(restants, groupe, besoinFille, besoinGarcon, true);
                if (i < 0) i = choisir(restants, groupe, false, false, true);
                if (i < 0 && groupe.size() < TMIN) i = choisir(restants, groupe, false, false, false);
                if (i < 0) break;
                groupe.add(restants.remove(i));
            }
            if (!groupe.isEmpty()) groupes.add(groupe);
        }
        return groupes;
    }

    private int choisir(List<EtudiantDTO> restants, List<EtudiantDTO> groupe, boolean wantF, boolean wantG, boolean limitRed) {
        int red = compterRedoublants(groupe);
        for (int i = 0; i < restants.size(); i++) {
            EtudiantDTO e = restants.get(i);
            if (e == null) continue;
            if (limitRed && e.getEstRedoublant() == 1 && red >= MAX_REDOUBLANTS) continue;
            if (wantF && e.getGenreEtu() != 1) continue;
            if (wantG && e.getGenreEtu() == 1) continue;
            return i;
        }
        return -1;
    }

    private int compterFilles(List<EtudiantDTO> g) { int c = 0; for (EtudiantDTO e : g) if (e != null && e.getGenreEtu() == 1) c++; return c; }
    private int compterGarcons(List<EtudiantDTO> g) { int c = 0; for (EtudiantDTO e : g) if (e != null && e.getGenreEtu() != 1) c++; return c; }
    private int compterRedoublants(List<EtudiantDTO> g) { int c = 0; for (EtudiantDTO e : g) if (e != null && e.getEstRedoublant() == 1) c++; return c; }
}



