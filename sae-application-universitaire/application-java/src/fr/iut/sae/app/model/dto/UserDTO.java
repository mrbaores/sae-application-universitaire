package fr.iut.sae.app.model.dto;

public class UserDTO {
    private int idUser;
    private String login;
    private String role;
    private Integer idEnseignant; // nullable
    private Integer idEtu;        // nullable

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getIdEnseignant() {
        return idEnseignant;
    }

    public void setIdEnseignant(Integer idEnseignant) {
        this.idEnseignant = idEnseignant;
    }

    public Integer getIdEtu() {
        return idEtu;
    }

    public void setIdEtu(Integer idEtu) {
        this.idEtu = idEtu;
    }
}
