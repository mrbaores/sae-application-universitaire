package fr.iut.sae.app.algo.s2;

import fr.iut.sae.app.algo.commun.modeleAlgo;
import fr.iut.sae.app.model.dto.EtudiantDTO;
import fr.iut.sae.app.algo.s2.AlgoUtil;

import java.util.ArrayList;
import java.util.List;

public class GloutonS2_1 implements modeleAlgo.AlgorithmeGroupes {

	private final int nombreGroupes;
	private final static int maxTailleCovoit = 4;
	public GloutonS2_1(int nombreGroupes) {
		this.nombreGroupes = nombreGroupes;
	}

	@Override
	public String nom() {
		return "Glouton 1 - Damien";
	}

	public List<List<EtudiantDTO>> construireGroupes(List<EtudiantDTO> formation) {
		if (formation == null || formation.isEmpty()) {
			return new ArrayList<>();
		}

		List<EtudiantDTO> etudiants = new ArrayList<>(formation);
		
		int tailleGroupe = (etudiants.size() / nombreGroupes)+1;	
		int erreur = AlgoUtil.checkNbrGroupes(nombreGroupes, etudiants);
		if (erreur == 1) 
			throw new IllegalArgumentException("Veuillez choisir un nombre de groupe plus elevee, la taille du groupe le plus remplis est " + tailleGroupe+1);
		if(erreur == 2)
			throw new IllegalArgumentException("Veuillez choisir un nobre de groupe plus bas, la taille du groupe le moins remplis est " + tailleGroupe);

		List<List<EtudiantDTO>> groupesCovoit = AlgoUtil.groupesCovoiturage(etudiants, maxTailleCovoit);
		for (List<EtudiantDTO> covoit : groupesCovoit) {
			etudiants.removeAll(covoit);
		}

		List<EtudiantDTO> etudiantsAnglo = AlgoUtil.groupeAnglo(etudiants);
		etudiants.removeAll(etudiantsAnglo);

		List<List<EtudiantDTO>> groupes = new ArrayList<>();
		for (int i = 0; i < nombreGroupes; i++) {
			groupes.add(new ArrayList<>());
		}
		
		List<List<EtudiantDTO>> groupesAnglo = new ArrayList<>();
		for(int i = 0; i < 2; i++)
			groupesAnglo.add(groupes.get(i));
		
		for (List<EtudiantDTO> covoit : groupesCovoit) {
			int index = 0;
			if(AlgoUtil.compterAnglophones(covoit) > 0)
				index = AlgoUtil.indiceGroupeScoreMinimal(formation, tailleGroupe, false, groupesAnglo, covoit);
			else
				index = AlgoUtil.indiceGroupeScoreMinimal(formation, tailleGroupe, false, groupes, covoit);
			groupes.get(index).addAll(covoit);
		}

		for (EtudiantDTO e : etudiantsAnglo) {
			int index = AlgoUtil.indiceGroupeScoreMinimal(formation, tailleGroupe, false, groupesAnglo, e);
			groupes.get(index).add(e);
		}

		for (EtudiantDTO e : etudiants) {
			int index = AlgoUtil.indiceGroupeScoreMinimal(formation, tailleGroupe, true, groupes, e);
			groupes.get(index).add(e);
		}

		return groupes;
	}
}
