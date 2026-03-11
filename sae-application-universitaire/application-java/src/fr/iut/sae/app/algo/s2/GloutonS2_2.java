package fr.iut.sae.app.algo.s2;

import fr.iut.sae.app.algo.commun.modeleAlgo;
import fr.iut.sae.app.model.dto.EtudiantDTO;

import java.util.List;
import java.util.ArrayList;

public class GloutonS2_2 implements modeleAlgo.AlgorithmeGroupes {

	private final int nombreGroupes;
	private final int tailleMaxCovoiturage = 4;

	public GloutonS2_2(int nombreGroupes) {
		this.nombreGroupes = nombreGroupes;
	}

	@Override
	public String nom() {
		return "Glouton 2 - Damien";
	}

	@Override
	public List<List<EtudiantDTO>> construireGroupes(List<EtudiantDTO> formation) {
		if (formation == null || formation.isEmpty()) {
			return new ArrayList<>();
		}

		List<EtudiantDTO> etudiants = new ArrayList<>(formation);

		int tailleGroupe = etudiants.size() / nombreGroupes;

		int erreur = AlgoUtil.checkNbrGroupes(nombreGroupes, etudiants);
		if (erreur == 1) 
			throw new IllegalArgumentException("Veuillez choisir un nombre de groupe plus elevee, la taille du groupe le plus remplis est " + tailleGroupe+1);
		if(erreur == 2)
			throw new IllegalArgumentException("Veuillez choisir un nobre de groupe plus bas, la taille du groupe le moins remplis est " + tailleGroupe);
		
		List<List<EtudiantDTO>> groupesCovoit = AlgoUtil.groupesCovoiturage(etudiants, tailleMaxCovoiturage);
		for (List<EtudiantDTO> covoit : groupesCovoit) {
			etudiants.removeAll(covoit);
		}

		List<EtudiantDTO> etudiantsAnglo = AlgoUtil.groupeAnglo(etudiants);
		etudiants.removeAll(etudiantsAnglo);

		List<EtudiantDTO> filles = new ArrayList<>();
		List<EtudiantDTO> garcons = new ArrayList<>();

		for (EtudiantDTO e : etudiants) {
			if (e.getGenreEtu() == 1) 
				filles.add(e);
			else 
				garcons.add(e);
		}

		List<List<EtudiantDTO>> groupes = new ArrayList<>();
		for (int i = 0; i < nombreGroupes; i++) {
			groupes.add(new ArrayList<>());
		}

		List<List<EtudiantDTO>> groupesAnglo = new ArrayList<>();
		for(int i = 0; i < 2; i++)
			groupesAnglo.add(groupes.get(i));
	
		List<List<EtudiantDTO>> groupesFilles = new ArrayList<>();
		for(int i = 0; i < 4; i++)
			groupesFilles.add(groupes.get(i));

		for (List<EtudiantDTO> covoit : groupesCovoit) {
			int index = AlgoUtil.indiceGroupeScoreMinimal(formation, tailleGroupe, false, groupes, covoit);
			groupes.get(index).addAll(covoit);
		}
		
		for (EtudiantDTO e : etudiantsAnglo) {
			int index = AlgoUtil.indiceGroupeScoreMinimal(formation, tailleGroupe, false, groupesAnglo, e);
			groupes.get(index).add(e);
		}
		
		for(EtudiantDTO e : filles){
			int index = AlgoUtil.indiceGroupeScoreMinimal(formation, tailleGroupe, false, groupesFilles, e);
			groupes.get(index).add(e);
		}
		
		for(EtudiantDTO e : garcons){
			int index = AlgoUtil.indiceGroupeScoreMinimal(formation, tailleGroupe, true, groupes, e);
			groupes.get(index).add(e);
		}
		return groupes;
	}
}
