package fr.iut.sae.app.model.dto;

public class FormationDTO {
    private int idFormation;
    private String nomFormation;

    public int getIdFormation() { return idFormation; }
    public void setIdFormation(int idFormation) { this.idFormation = idFormation; }

    public String getNomFormation() { return nomFormation; }
    public void setNomFormation(String nomFormation) { this.nomFormation = nomFormation; }

    @Override
    public String toString() {
        return idFormation + " - " + (nomFormation == null ? "" : nomFormation);
    }
}
