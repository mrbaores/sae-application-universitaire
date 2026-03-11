package fr.iut.sae.app.algo.s1;

import fr.iut.sae.app.algo.commun.modeleAlgo;
import fr.iut.sae.app.model.dto.EtudiantDTO;
import java.util.ArrayList;
import java.util.List;

// J'ai renommé la classe en "GloutonS1"
public class GloutonS1_1 implements modeleAlgo.AlgorithmeGroupes {

    private int nbGroupes;

    public GloutonS1_1(int nbGroupes) {
        this.nbGroupes = nbGroupes;
    }

    @Override
    public String nom() {
        return "Glouton S1 (Distribution)";
    }

    @Override
    public List<List<EtudiantDTO>> construireGroupes(List<EtudiantDTO> etudiants) {
        return executer(etudiants, this.nbGroupes);
    }

    public List<List<EtudiantDTO>> executer(List<EtudiantDTO> etudiants, int nbGroupes) {
        // 1. Création des groupes
        List<List<EtudiantDTO>> groupes = new ArrayList<>();
        for (int i = 0; i < nbGroupes; i++) groupes.add(new ArrayList<>());

        //  Séparation
        List<EtudiantDTO> filles = new ArrayList<>();
        List<EtudiantDTO> autres = new ArrayList<>();

        for (EtudiantDTO e : etudiants) {
            if (e.getGenreEtu() == 1) filles.add(e);
            else autres.add(e);
        }

        // 3. CONTRAINTE : 3 Filles minimum 
        int indexGroupe = 0;
        for (EtudiantDTO fille : filles) {
            groupes.get(indexGroupe).add(fille);
            indexGroupe++; 
            if (indexGroupe >= nbGroupes) indexGroupe = 0;
        }

        //  Remplir les trous (Équilibre taille)
        for (EtudiantDTO e : autres) {
            List<EtudiantDTO> plusPetitGroupe = groupes.get(0);
            for (List<EtudiantDTO> g : groupes) {
                if (g.size() < plusPetitGroupe.size()) {
                    plusPetitGroupe = g;
                }
            }
            // CONTRAINTE : Taille Max 17
            if (plusPetitGroupe.size() < 17) {
                plusPetitGroupe.add(e);
            }
        }
        return groupes;
    }
}