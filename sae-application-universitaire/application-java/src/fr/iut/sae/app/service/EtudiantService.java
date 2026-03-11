package fr.iut.sae.app.service;

import fr.iut.sae.app.model.dto.EtudiantDTO;
import fr.iut.sae.app.util.JsonMini;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EtudiantService {

    private final ApiClient apiClient;

    public EtudiantService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<EtudiantDTO> listEtudiants(int idFormation, int numSemestre) throws IOException, InterruptedException {
        String json = apiClient.get("/formations/" + idFormation + "/semestres/" + numSemestre + "/etudiants");
        List<Map<String, String>> rows = JsonMini.parseArrayOfObjects(json);
        List<EtudiantDTO> out = new ArrayList<>();
        for (Map<String, String> r : rows) {
            EtudiantDTO e = new EtudiantDTO();
            e.setIdEtu(toInt(r.get("idEtu")));
            e.setNomEtu(r.getOrDefault("nomEtu", ""));
            e.setPrenomEtu(r.getOrDefault("prenomEtu", ""));
            e.setGenreEtu(toInt(r.get("genreEtu")));
            e.setTypeBac(r.getOrDefault("typeBac", ""));
            e.setIndiceCovoiturage(toInt(r.get("indiceCovoiturage")));
            e.setEstAnglophone(toInt(r.get("estAnglophone")));
            e.setEstRedoublant(toInt(r.get("estRedoublant")));
            e.setEstApprenti(toInt(r.get("estApprenti")));
            e.setNomGroupe(r.getOrDefault("nomGroupe", ""));
            e.setEmailUniEtu(r.getOrDefault("emailUniEtu", ""));
            e.setMoyenne(toFloat(r.get("scoreGlobal")));
	    out.add(e);
        }
        return out;
    }

    /**
     * Assigne (ou désassigne si nomGroupe == null/blank) un étudiant à un groupe.
     * Route API : PATCH /etudiants/{id}/groupe
     */
    public void setGroupe(int idEtu, int idFormation, int numSemestre, String nomGroupe) throws IOException, InterruptedException {
        String ng = (nomGroupe == null || nomGroupe.isBlank()) ? "null" : "\"" + escape(nomGroupe) + "\"";
        String body = "{" +
                "\"idFormation\":" + idFormation + "," +
                "\"numSemestre\":" + numSemestre + "," +
                "\"nomGroupe\":" + ng +
                "}";
        apiClient.patchJson("/etudiants/" + idEtu + "/groupe", body);
    }

    
    private float toFloat(String s) {
        try {
            return Float.parseFloat(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private int toInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
