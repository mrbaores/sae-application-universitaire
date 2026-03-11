package fr.iut.sae.app.algo.s1;

import fr.iut.sae.app.algo.commun.modeleAlgo;
import fr.iut.sae.app.model.dto.EtudiantDTO;
import java.util.ArrayList;
import java.util.List;

public class GloutonS1_2 implements modeleAlgo.AlgorithmeGroupes {

    private int nbGroupes;

    // 1. Constructeur OBLIGATOIRE pour le contrôleur
    public GloutonS1_2(int nbGroupes) {
        this.nbGroupes = nbGroupes;
    }

    // 2. Nom qui s'affichera dans l'application
    @Override
    public String nom() {
        return "Glouton Quotas (Bac)";
    }

    // 3. Point d'entrée standard
    @Override
    public List<List<EtudiantDTO>> construireGroupes(List<EtudiantDTO> etudiants) {
        return executer(etudiants, this.nbGroupes);
    }

    // --- LOGIQUE DE L'ALGORITHME ---
    public List<List<EtudiantDTO>> executer(List<EtudiantDTO> etudiants, int nbGroupes) {
        
        // A. Création des groupes vides
        List<List<EtudiantDTO>> groupes = new ArrayList<>();
        for (int i = 0; i < nbGroupes; i++) groupes.add(new ArrayList<>());

        // B. Tri des étudiants en 4 listes
        List<EtudiantDTO> fillesTechno = new ArrayList<>();
        List<EtudiantDTO> fillesGeneral = new ArrayList<>();
        List<EtudiantDTO> garconsTechno = new ArrayList<>();
        List<EtudiantDTO> garconsGeneral = new ArrayList<>();

        for (EtudiantDTO e : etudiants) {
            boolean estFille = (e.getGenreEtu() == 1);
            // Vérification simple si le bac contient "techno" (insensible à la casse)
            boolean estTechno = (e.getTypeBac() != null && e.getTypeBac().toLowerCase().contains("techno"));

            if (estFille && estTechno) fillesTechno.add(e);
            else if (estFille) fillesGeneral.add(e);
            else if (estTechno) garconsTechno.add(e);
            else garconsGeneral.add(e);
        }

        // C. ÉTAPE 1 (CONTRAINTE) : Sécuriser 3 filles par groupe
        for (List<EtudiantDTO> g : groupes) {
            for (int k = 0; k < 3; k++) {
                // On privilégie les filles techno pour aider le quota, sinon filles général
                if (!fillesTechno.isEmpty()) g.add(fillesTechno.remove(0));
                else if (!fillesGeneral.isEmpty()) g.add(fillesGeneral.remove(0));
            }
        }

        // D. ÉTAPE 2 (OPTIMISATION) : Atteindre le quota de Techno (Cible = 8)
        int cibleTechno = 8; 

        for (List<EtudiantDTO> g : groupes) {
            // Tant qu'on n'a pas 8 technos et qu'il reste de la place
            while (compterTechno(g) < cibleTechno) {
                if (!garconsTechno.isEmpty()) {
                    g.add(garconsTechno.remove(0));
                } else {
                    break; // Plus de garçons techno disponibles
                }
                // Sécurité taille max
                if (g.size() >= 17) break; 
            }
        }

        // E. ÉTAPE 3 (REMPLISSAGE) : On met tout le reste
        List<EtudiantDTO> restants = new ArrayList<>();
        restants.addAll(garconsGeneral);
        restants.addAll(fillesGeneral); 
        restants.addAll(fillesTechno);
        restants.addAll(garconsTechno);

        int index = 0;
        for (EtudiantDTO e : restants) {
            // On cherche un groupe qui a de la place (< 17)
            // On utilise un modulo pour tourner entre les groupes (0, 1, 2, 0, 1...)
            List<EtudiantDTO> g = groupes.get(index % nbGroupes);
            
            if (g.size() < 17) {
                g.add(e);
            }
            index++;
        }

        return groupes;
    }

    // Petite méthode pour compter les technos dans un groupe en cours de construction
    private int compterTechno(List<EtudiantDTO> g) {
        int nb = 0;
        for (EtudiantDTO e : g) {
            if (e.getTypeBac() != null && e.getTypeBac().toLowerCase().contains("techno")) nb++;
        }
        return nb;
    }
}