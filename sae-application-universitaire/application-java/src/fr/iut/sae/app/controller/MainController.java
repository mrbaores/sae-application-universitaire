package fr.iut.sae.app.controller;

import fr.iut.sae.app.model.dto.FormationDTO;
import fr.iut.sae.app.model.dto.SemestreDTO;
import fr.iut.sae.app.model.state.AppSession;
import fr.iut.sae.app.service.*;
import fr.iut.sae.app.view.MainFrame;

import javax.swing.*;
import java.util.List;

public class MainController {

    private final MainFrame view;
    private final AppSession session;

    private final FormationService formationService;
    private final SemestreService semestreService;

    private final ManualGroupsController manualController;
    private final AutoGroupsController autoController;

    public MainController(MainFrame view, AppSession session, ApiClient apiClient) {
        this.view = view;
        this.session = session;

        this.formationService = new FormationService(apiClient);
        this.semestreService = new SemestreService(apiClient);

        EtudiantService etudiantService = new EtudiantService(apiClient);
        GroupeService groupeService = new GroupeService(apiClient);

        this.manualController = new ManualGroupsController(view.getManualPanel(), session, etudiantService, groupeService);
        this.autoController = new AutoGroupsController(view.getAutoPanel(), session, etudiantService, groupeService);

        wire();
        loadFormationsAsync();
    }

    private void wire() {
        view.getBtnReload().addActionListener(e -> reloadCurrent());

        view.getCbFormation().addActionListener(e -> {
            FormationDTO f = (FormationDTO) view.getCbFormation().getSelectedItem();
            if (f != null) {
                session.setIdFormation(f.getIdFormation());
                loadSemestresAsync(f.getIdFormation());
            }
        });

        view.getCbSemestre().addActionListener(e -> {
            SemestreDTO s = (SemestreDTO) view.getCbSemestre().getSelectedItem();
            if (s != null) {
                session.setNumSemestre(s.getNumSemestre());
                manualController.reloadAsync();
                autoController.onContextChanged();
            }
        });
    }

    private void reloadCurrent() {
        manualController.reloadAsync();
        autoController.onContextChanged();
    }

    private void loadFormationsAsync() {
        new SwingWorker<List<FormationDTO>, Void>() {
            @Override
            protected List<FormationDTO> doInBackground() throws Exception {
                return formationService.listFormations();
            }

            @Override
            protected void done() {
                try {
                    List<FormationDTO> formations = get();
                    view.setFormations(formations);
                    view.selectFormationById(session.getIdFormation());
                    loadSemestresAsync(session.getIdFormation());
                } catch (Exception ex) {
                    view.showError(ex.getMessage());
                }
            }
        }.execute();
    }

    private void loadSemestresAsync(int idFormation) {
        new SwingWorker<List<SemestreDTO>, Void>() {
            @Override
            protected List<SemestreDTO> doInBackground() throws Exception {
                return semestreService.listSemestres(idFormation);
            }

            @Override
            protected void done() {
                try {
                    List<SemestreDTO> semestres = get();
                    view.setSemestres(semestres);
                    view.selectSemestreByNum(session.getNumSemestre());

                    manualController.reloadAsync();
                    autoController.onContextChanged();
                } catch (Exception ex) {
                    view.showError(ex.getMessage());
                }
            }
        }.execute();
    }
}
