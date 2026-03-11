package fr.iut.sae.app.service;

import fr.iut.sae.app.model.dto.EtudiantDTO;
import fr.iut.sae.app.model.dto.GroupeDTO;
import fr.iut.sae.app.util.JsonMini;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupeService {

    private final ApiClient apiClient;

    public GroupeService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<GroupeDTO> listGroupes(int idFormation, int numSemestre) throws IOException, InterruptedException {
        String json = apiClient.get("/formations/" + idFormation + "/semestres/" + numSemestre + "/groupes");
        List<Map<String, String>> rows = JsonMini.parseArrayOfObjects(json);
        List<GroupeDTO> out = new ArrayList<>();
        for (Map<String, String> r : rows) {
            GroupeDTO g = new GroupeDTO();
            g.setNomGroupe(r.getOrDefault("nomGroupe", ""));
            g.setEffectif(toInt(r.get("effectif")));
            out.add(g);
        }
        return out;
    }

    public void createGroupe(int idFormation, int numSemestre, String nomGroupe) throws IOException, InterruptedException {
        String body = "{\"nomGroupe\":\"" + escape(nomGroupe) + "\"}";
        apiClient.postJson("/formations/" + idFormation + "/semestres/" + numSemestre + "/groupes", body);
    }

    // ✅ LISTE les étudiants d'un groupe
    // GET /formations/{idf}/semestres/{sem}/groupes/{nom}/etudiants
    public List<EtudiantDTO> listEtudiantsDuGroupe(int idFormation, int numSemestre, String nomGroupe)
            throws IOException, InterruptedException {

        String nomEnc = encodePath(nomGroupe);

        String json = apiClient.get("/formations/" + idFormation + "/semestres/" + numSemestre
                + "/groupes/" + nomEnc + "/etudiants");

        List<Map<String, String>> rows = JsonMini.parseArrayOfObjects(json);
        List<EtudiantDTO> out = new ArrayList<>();

        for (Map<String, String> r : rows) {
            EtudiantDTO e = new EtudiantDTO();
            e.setIdEtu(toInt(r.get("idEtu")));
            e.setNomEtu(r.getOrDefault("nomEtu", ""));
            e.setPrenomEtu(r.getOrDefault("prenomEtu", ""));
            e.setEmailUniEtu(r.getOrDefault("emailUniEtu", ""));
            out.add(e);
        }
        return out;
    }

    // ✅ DELETE /formations/{idf}/semestres/{sem}/groupes/{nom}
    public void deleteGroupe(int idFormation, int numSemestre, String nomGroupe)
            throws IOException, InterruptedException {

        String nomEnc = encodePath(nomGroupe);

        apiClient.delete("/formations/" + idFormation
                + "/semestres/" + numSemestre
                + "/groupes/" + nomEnc);
    }

    private int toInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String encodePath(String s) {
        if (s == null) return "";
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}

