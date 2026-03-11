package fr.iut.sae.app.model.state;

import fr.iut.sae.app.model.dto.UserDTO;

public class AppSession {
    private String token;
    private UserDTO user;

    private int idFormation = 1;
    private int numSemestre = 1;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public int getIdFormation() {
        return idFormation;
    }

    public void setIdFormation(int idFormation) {
        this.idFormation = idFormation;
    }

    public int getNumSemestre() {
        return numSemestre;
    }

    public void setNumSemestre(int numSemestre) {
        this.numSemestre = numSemestre;
    }
}
