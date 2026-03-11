package fr.iut.sae.app.algo.s1;

import fr.iut.sae.app.algo.commun.modeleAlgo;
import fr.iut.sae.app.model.dto.EtudiantDTO;
import java.util.ArrayList;
import java.util.List;

// CLASSE RENOMMÉE EN "BruteForceS1"
public class BruteForceS1 implements modeleAlgo.AlgorithmeGroupes {

    private int nbGroupes;
    private List<List<EtudiantDTO>> meilleureSolution = null;
    private float meilleurScore = -1;
    
    
    private scoreS1 scoreJudge = new scoreS1(); 

    public BruteForceS1(int nbGroupes) {
        this.nbGroupes = nbGroupes;
    }

    @Override
    public String nom() {
        return "Brute Force S1 (Simple)";
    }

    @Override
    public List<List<EtudiantDTO>> construireGroupes(List<EtudiantDTO> etudiants) {
        return executer(etudiants, this.nbGroupes);
    }

    public List<List<EtudiantDTO>> executer(List<EtudiantDTO> etudiants, int nbGroupes) {
        // Sécurité : 20 étudiants max pour ne pas bloquer l'ordi
        if (etudiants.size() > 20) return null; 

        List<List<EtudiantDTO>> groupes = new ArrayList<>();
        for (int i = 0; i < nbGroupes; i++) groupes.add(new ArrayList<>());

        meilleureSolution = null;
        meilleurScore = -1;

        backtrack(etudiants, 0, groupes);
        
        return meilleureSolution;
    }

    private void backtrack(List<EtudiantDTO> listeEtudiants, int index, List<List<EtudiantDTO>> groupes) {
        // Cas de base
        if (index == listeEtudiants.size()) {
            float note = scoreJudge.calculer(groupes);
            if (note > meilleurScore) {
                meilleurScore = note;
                meilleureSolution = copier(groupes);
            }
            return;
        }

        // Récursivité
        EtudiantDTO etudiant = listeEtudiants.get(index);

        for (List<EtudiantDTO> unGroupe : groupes) {
            if (unGroupe.size() >= 17) continue;

            unGroupe.add(etudiant);
            backtrack(listeEtudiants, index + 1, groupes);
            unGroupe.remove(unGroupe.size() - 1);
        }
    }

    private List<List<EtudiantDTO>> copier(List<List<EtudiantDTO>> original) {
        List<List<EtudiantDTO>> copie = new ArrayList<>();
        for (List<EtudiantDTO> g : original) {
            copie.add(new ArrayList<>(g));
        }
        return copie;
    }
}