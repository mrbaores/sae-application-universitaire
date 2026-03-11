package fr.iut.sae.app.algo.s2;

import fr.iut.sae.app.model.dto.EtudiantDTO;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public final class AlgoUtil{
	
	private final static int TAILLE_MAX = 19;
	private final static int TAILLE_MIN = 15;
	private AlgoUtil(){}
	
	public static List<List<EtudiantDTO>> groupesCovoiturage(List<EtudiantDTO> formation, int maxTailleCovoit) throws IllegalArgumentException{
		int maxCovoit = 0;
		int minCovoit = Integer.MAX_VALUE;
		for(EtudiantDTO e : formation){
			if(maxCovoit<e.getIndiceCovoiturage())
				maxCovoit = e.getIndiceCovoiturage();
			if(minCovoit > e.getIndiceCovoiturage() && e.getIndiceCovoiturage() != 0)
				minCovoit = e.getIndiceCovoiturage();
		}
		int stop = maxCovoit-minCovoit;

		List<List<EtudiantDTO>> groupesCovoit = new ArrayList<>();

		for(int i = 0; i <= stop; i++)
			groupesCovoit.add(new ArrayList<EtudiantDTO>());

		for(EtudiantDTO e : formation){
			if(e.getIndiceCovoiturage() != 0){
				if(groupesCovoit.get(e.getIndiceCovoiturage()-minCovoit).size()>=maxTailleCovoit){
					throw new IllegalArgumentException("Le covoiturage " + e.getIndiceCovoiturage() + " a trop de membre par rapport a ce qui est autorise (" + maxTailleCovoit + ")");
				}
				groupesCovoit.get(e.getIndiceCovoiturage()-minCovoit).add(e);
			}
		}

		return groupesCovoit;
	}

	public static int indiceGroupeScoreMinimal(List<EtudiantDTO> formation, int tailleGroupe, boolean autoriseAugmentationTailleGroupe, List<List<EtudiantDTO>> listeGroupes, List<EtudiantDTO> etudiant) {
	
	return indiceGroupeScoreMinimal(formation, tailleGroupe, autoriseAugmentationTailleGroupe, listeGroupes, etudiant.toArray(new EtudiantDTO[0])); 
	}

	public static int indiceGroupeScoreMinimal(List<EtudiantDTO> formation, int tailleGroupes, boolean autoriseAugmentationTailleGroupe, List<List<EtudiantDTO>> listeGroupes, EtudiantDTO... etudiant) {
		if(etudiant.length == 0)
			throw new IllegalArgumentException("Etudiant null");
			
		int meilleurIndex = -1;
		int meilleurScore = Integer.MAX_VALUE;

		for (int i = 0; i < listeGroupes.size(); i++) {
			List<EtudiantDTO> groupe = listeGroupes.get(i);
			
			if (groupe.size() + etudiant.length > tailleGroupes) 
				continue;

			int score = scoreSemestre(listeGroupes, formation);
			if (score < meilleurScore) {
				meilleurScore = score;
				meilleurIndex = i;
			}
		}

		if (meilleurIndex == -1 && autoriseAugmentationTailleGroupe)
			return indiceGroupeScoreMinimal(formation, tailleGroupes+1, false, listeGroupes, etudiant);	

		return meilleurIndex;
	}

	private static int scoreMinimal(List<List<EtudiantDTO>> groupes, List<EtudiantDTO> formation, int tailleGroupe, List<EtudiantDTO> etus){
		return scoreMinimal(groupes, formation, tailleGroupe, etus.toArray(new EtudiantDTO[0]));
	}

	private static int scoreMinimal(List<List<EtudiantDTO>> groupes, List<EtudiantDTO> formation, int tailleGroupes, EtudiantDTO... etus){
		
		if(etus.length == 0)
			return Integer.MAX_VALUE;

		int indiceGroupeOptimal = -1;
		int scoreMinimal = Integer.MAX_VALUE;

		for(int i = 0; i < groupes.size(); i++){

			if(groupes.get(i).size() + etus.length > tailleGroupes)
				continue;

			int testScore = scoreSemestre(groupes, formation);
			if(testScore < scoreMinimal){
				scoreMinimal = testScore;
				indiceGroupeOptimal = i;
			}
		}
		return scoreMinimal;
	}

	private static boolean respectCovoiturage(List<List<EtudiantDTO>> groupes, List<EtudiantDTO> groupeCible, EtudiantDTO e) {
		if (e == null) return true;

		int covoit = e.getIndiceCovoiturage();
		if (covoit == 0) return true;

		for (int j = 0; j < groupes.size(); j++) {
			List<EtudiantDTO> g = groupes.get(j);
			if (g == groupeCible) continue;

			for (int x = 0; x < g.size(); x++) {
				EtudiantDTO autre = g.get(x);
				if (autre != null && autre.getIndiceCovoiturage() == covoit) {
					return false;
				}
			}
		}
		return true;
	}

	public static List<EtudiantDTO> groupeAnglo(List<EtudiantDTO> etu) {
		List<EtudiantDTO> anglo = new ArrayList<>();
		for (int i = 0; i < etu.size(); i++) {
			EtudiantDTO e = etu.get(i);
			if (e != null && e.getEstAnglophone() == 1) {
				anglo.add(e);
			}
		}
		return anglo;
	}

	private static float moyenneGroupe(List<EtudiantDTO> groupe){
		float moyenne = 0;
		for(EtudiantDTO etudiant : groupe)
			moyenne += etudiant.getMoyenne();
		moyenne /= groupe.size();
		if(moyenne == 0)
			throw new IllegalArgumentException("Les etudiants n'ont pas de moyenne");
		return moyenne;

	}

	private static int scoreSemestre(List<List<EtudiantDTO>> groupes, List<EtudiantDTO> formation){
		int scoreEtus = 0;
		int scoreSem = 0;

		for(int i = 0; i < formation.size(); i++)
			scoreEtus += moyenneGroupe(formation);	
		scoreEtus /= formation.size();

		for(int i = 0; i < groupes.size(); i++)
			scoreSem += Math.abs(moyenneGroupe(groupes.get(i)) - scoreEtus);	
		scoreSem /= groupes.size();

		return scoreSem;
	}

	private static int compterFilles(List<EtudiantDTO> groupe) {
		int nb = 0;
		for (int i = 0; i < groupe.size(); i++) {
			EtudiantDTO e = groupe.get(i);
			if (e != null && e.getGenreEtu() == 1) nb++;
		}
		return nb;
	}

	public static int compterAnglophones(List<EtudiantDTO> groupe) {
		int nb = 0;
		for (int i = 0; i < groupe.size(); i++) {
			EtudiantDTO e = groupe.get(i);
			if (e != null && e.getEstAnglophone() == 1) nb++;
		}
		return nb;
	}

	
	public static int checkNbrGroupes(int nbrGroupes, List<EtudiantDTO> etudiants) {
		if (nbrGroupes <= 0) return 2;

		int tailleGroupes = etudiants.size() / nbrGroupes;

		if (tailleGroupes > TAILLE_MAX) return 1;
		if (tailleGroupes < TAILLE_MIN) return 2;

		return 0;
	}
}

