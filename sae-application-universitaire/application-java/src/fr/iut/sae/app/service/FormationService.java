package fr.iut.sae.app.service;

import fr.iut.sae.app.model.dto.FormationDTO;
import fr.iut.sae.app.util.JsonMini;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FormationService {

    private final ApiClient apiClient;

    public FormationService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<FormationDTO> listFormations() throws IOException, InterruptedException {
        String json = apiClient.get("/formations");
        List<Map<String, String>> items = JsonMini.parseArrayOfObjects(json);
        List<FormationDTO> out = new ArrayList<>();
        for (Map<String, String> it : items) {
            FormationDTO f = new FormationDTO();
            f.setIdFormation(toInt(it.get("idFormation")));
            f.setNomFormation(it.getOrDefault("nomFormation", ""));
            out.add(f);
        }
        return out;
    }

    private int toInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }
}
