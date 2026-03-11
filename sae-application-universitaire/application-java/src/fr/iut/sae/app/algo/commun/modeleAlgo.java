package fr.iut.sae.app.algo.commun;

import fr.iut.sae.app.model.dto.EtudiantDTO;
import java.util.List;

public class modeleAlgo {

    public interface AlgorithmeGroupes {
        String nom();
        List<List<EtudiantDTO>> construireGroupes(List<EtudiantDTO> etudiantsSansGroupe);
    }

    public interface FonctionScore {
        String nom();
        float scoreGroupe(List<EtudiantDTO> groupe);
        float scoreSolution(List<List<EtudiantDTO>> solution);
    }
}
