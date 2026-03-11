package fr.iut.sae.app.algo.s2;

import fr.iut.sae.app.algo.commun.modeleAlgo;
import fr.iut.sae.app.model.dto.EtudiantDTO;

import java.util.List;
import java.util.ArrayList;
import java.lang.Math;
public class scoreS2 implements modeleAlgo.FonctionScore {

    @Override
    public String nom() {
        return "Score S2";
    }

    @Override
    public float scoreGroupe(List<EtudiantDTO> groupe) {

	    float moyenne = 0;
	    for(EtudiantDTO etudiant : groupe)
		    moyenne += etudiant.getMoyenne();
	    moyenne /= groupe.size();
	    if(moyenne != moyenne)
		    return 0;
	    return moyenne;
    }

    @Override
    public float scoreSolution(List<List<EtudiantDTO>> groupes) {

	    int scoreEtus = 0;
	    int scoreSem = 0;

	    int tailleFormation = 0;
	    List<EtudiantDTO> formation = new ArrayList<>();
	    for(List<EtudiantDTO> groupe : groupes){
		    tailleFormation += groupe.size();
		    formation.addAll(groupe);
	    }

	    for(int i = 0; i < tailleFormation; i++)
		    scoreEtus += scoreGroupe(formation);	
	    scoreEtus /= tailleFormation;

	    for(int i = 0; i < groupes.size(); i++)
		    scoreSem += Math.abs(scoreGroupe(groupes.get(i)) - scoreEtus);	
	    scoreSem /= groupes.size();

	    if(scoreSem != scoreSem)
		    scoreSem = 0;

	    return scoreSem;
    }

}

