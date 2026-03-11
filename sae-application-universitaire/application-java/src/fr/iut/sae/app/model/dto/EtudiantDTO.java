package fr.iut.sae.app.model.dto;

public class EtudiantDTO {
    private int idEtu;
    private String nomEtu;
    private String prenomEtu;
    private int genreEtu; // 0/1
    private String typeBac;
    private int indiceCovoiturage;
    private int estAnglophone; // 0/1
    private int estRedoublant; // 0/1
    private int estApprenti;   // 0/1
    private String nomGroupe;  // peut être null/vide
    private String emailUniEtu;
    private float moyenne;

    public int getIdEtu() { return idEtu; }
    public void setIdEtu(int idEtu) { this.idEtu = idEtu; }

    public String getNomEtu() { return nomEtu; }
    public void setNomEtu(String nomEtu) { this.nomEtu = nomEtu; }

    public String getPrenomEtu() { return prenomEtu; }
    public void setPrenomEtu(String prenomEtu) { this.prenomEtu = prenomEtu; }

    public int getGenreEtu() { return genreEtu; }
    public void setGenreEtu(int genreEtu) { this.genreEtu = genreEtu; }

    public String getTypeBac() { return typeBac; }
    public void setTypeBac(String typeBac) { this.typeBac = typeBac; }

    public int getIndiceCovoiturage() { return indiceCovoiturage; }
    public void setIndiceCovoiturage(int indiceCovoiturage) { this.indiceCovoiturage = indiceCovoiturage; }

    public int getEstAnglophone() { return estAnglophone; }
    public void setEstAnglophone(int estAnglophone) { this.estAnglophone = estAnglophone; }

    public int getEstRedoublant() { return estRedoublant; }
    public void setEstRedoublant(int estRedoublant) { this.estRedoublant = estRedoublant; }

    public int getEstApprenti() { return estApprenti; }
    public void setEstApprenti(int estApprenti) { this.estApprenti = estApprenti; }

    public String getNomGroupe() { return nomGroupe; }
    public void setNomGroupe(String nomGroupe) { this.nomGroupe = nomGroupe; }

    public String getEmailUniEtu() { return emailUniEtu; }
    public void setEmailUniEtu(String emailUniEtu) { this.emailUniEtu = emailUniEtu; }

    public float getMoyenne() { return moyenne; }
    public void setMoyenne(float moyenne) { this.moyenne = moyenne; }

    public String getNomComplet() {
        return (prenomEtu == null ? "" : prenomEtu) + " " + (nomEtu == null ? "" : nomEtu);
    }
}
