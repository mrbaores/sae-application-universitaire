package fr.iut.sae.app.service;

import fr.iut.sae.app.model.dto.SemestreDTO;
import fr.iut.sae.app.util.JsonMini;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SemestreService {

    private final ApiClient apiClient;

    public SemestreService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<SemestreDTO> listSemestres(int idFormation) throws IOException, InterruptedException {
        String json = apiClient.get("/formations/" + idFormation + "/semestres");
        List<Map<String, String>> rows = JsonMini.parseArrayOfObjects(json);
        List<SemestreDTO> out = new ArrayList<>();
        for (Map<String, String> r : rows) {
            SemestreDTO s = new SemestreDTO();
            s.setNumSemestre(toInt(r.get("numSemestre")));
            s.setNombreMaxCovoit(toInt(r.get("nombreMaxCovoit")));
            s.setEstApprentissage(toInt(r.get("estApprentissage")));
            out.add(s);
        }
        return out;
    }

    private int toInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }
}
