package fr.iut.sae.app.algo.s1;

import fr.iut.sae.app.algo.commun.modeleAlgo;
import fr.iut.sae.app.model.dto.EtudiantDTO;
import java.util.List;

// J'ai renommé la classe en "ScoreS1"
public class scoreS1 implements modeleAlgo.FonctionScore {

    @Override
    public String nom() {
        return "Score S1";
    }

    @Override
    public float scoreGroupe(List<EtudiantDTO> groupe) {
        if (groupe == null || groupe.isEmpty()) return 0;

        int penalite = 0;

        // 1. Règle Taille (14-17)
        if (groupe.size() < 14) penalite += 50;
        if (groupe.size() > 17) penalite += 50;

        // 2. Règle Filles (min 3)
        int nbFilles = 0;
        for (EtudiantDTO e : groupe) {
            if (e.getGenreEtu() == 1) nbFilles++;
        }
        if (nbFilles < 3) penalite += 30;

        // 3. Règle Techno (cible 8)
        int nbTechno = 0;
        for (EtudiantDTO e : groupe) {
            if (e.getTypeBac() != null && e.getTypeBac().toLowerCase().contains("techno")) nbTechno++;
        }
        // Si écart > 3 par rapport à 8 (donc moins de 5 ou plus de 11), pénalité
        if (Math.abs(nbTechno - 8) > 3) penalite += 10;

        return Math.max(0, 100 - penalite);
    }

    @Override
    public float scoreSolution(List<List<EtudiantDTO>> solution) {
        if (solution == null || solution.isEmpty()) return 0;

        double total = 0;
        for (List<EtudiantDTO> groupe : solution) {
            total += scoreGroupe(groupe);
        }
        
        return Math.round(total / solution.size());
    }
    
    public float calculer(List<List<EtudiantDTO>> solution) {
        return scoreSolution(solution);
    }
}