package fr.iut.sae.app.controller;

import fr.iut.sae.app.model.dto.EtudiantDTO;
import fr.iut.sae.app.model.dto.GroupeDTO;
import fr.iut.sae.app.model.state.AppSession;
import fr.iut.sae.app.service.EtudiantService;
import fr.iut.sae.app.service.GroupeService;
import fr.iut.sae.app.view.ManualGroupsPanel;

import javax.swing.*;
import java.util.List;

public class ManualGroupsController {

    private final ManualGroupsPanel view;
    private final AppSession session;
    private final EtudiantService etudiantService;
    private final GroupeService groupeService;

    public ManualGroupsController(ManualGroupsPanel view, AppSession session,
                                  EtudiantService etudiantService, GroupeService groupeService) {
        this.view = view;
        this.session = session;
        this.etudiantService = etudiantService;
        this.groupeService = groupeService;

        // actions
        view.getBtnRefresh().addActionListener(e -> reloadAsync());
        view.getBtnAssign().addActionListener(e -> assignSelected());
        view.getBtnUnassign().addActionListener(e -> unassignSelected());
        view.getBtnCreateGroup().addActionListener(e -> createGroupAsync());
        view.getBtnDeleteGroup().addActionListener(e -> deleteSelectedGroupAsync());

        // ✅ Voir membres réparé
        view.getBtnShowGroup().addActionListener(e -> showSelectedGroupMembersAsync());

        // sélection étudiant -> détails
        view.getTbStudents().getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            EtudiantDTO sel = getSelectedStudent();
            view.showStudentDetails(sel);
        });

        // premier chargement
        reloadAsync();
    }

    public void reloadAsync() {
        setBusy(true);
        new SwingWorker<Void, Void>() {
            private List<EtudiantDTO> students;
            private List<GroupeDTO> groups;
            private Exception error;

            @Override
            protected Void doInBackground() {
                try {
                    int f = session.getIdFormation();
                    int s = session.getNumSemestre();
                    students = etudiantService.listEtudiants(f, s);
                    groups = groupeService.listGroupes(f, s);
                } catch (Exception ex) {
                    error = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                setBusy(false);
                if (error != null) {
                    view.showError(error.getMessage());
                    return;
                }
                view.getStudentsModel().setData(students);
                view.getGroupsModel().setData(groups);
            }
        }.execute();
    }

    // ===================== VOIR MEMBRES =====================
    private void showSelectedGroupMembersAsync() {
        GroupeDTO grp = getSelectedGroup();
        if (grp == null || grp.getNomGroupe() == null || grp.getNomGroupe().isBlank()) {
            view.showError("Sélectionne un groupe puis clique sur 'Voir membres'.");
            return;
        }

        String nom = grp.getNomGroupe().trim();
        setBusy(true);

        new SwingWorker<Void, Void>() {
            Exception error;
            List<EtudiantDTO> members;

            @Override
            protected Void doInBackground() {
                try {
                    int f = session.getIdFormation();
                    int s = session.getNumSemestre();
                    members = groupeService.listEtudiantsDuGroupe(f, s, nom);
                } catch (Exception ex) {
                    error = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                setBusy(false);
                if (error != null) {
                    view.showError(error.getMessage());
                    return;
                }
                view.showGroupMembersPopup(nom, members);
            }
        }.execute();
    }

    // ===================== SUPPRIMER GROUPE =====================
    private void deleteSelectedGroupAsync() {
        GroupeDTO grp = getSelectedGroup();
        if (grp == null || grp.getNomGroupe() == null || grp.getNomGroupe().isBlank()) {
            view.showError("Sélectionne un groupe.");
            return;
        }
        String nom = grp.getNomGroupe().trim();

        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Supprimer le groupe " + nom + " ?\n\n"
                        + "Action :\n"
                        + "- désaffecter tous les étudiants du groupe\n"
                        + "- supprimer le groupe en base\n",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        setBusy(true);

        new SwingWorker<Void, Void>() {
            Exception error;
            int nbUnassigned = 0;

            @Override
            protected Void doInBackground() {
                try {
                    int f = session.getIdFormation();
                    int s = session.getNumSemestre();

                    // 1) récupérer les membres
                    List<EtudiantDTO> members = groupeService.listEtudiantsDuGroupe(f, s, nom);

                    // 2) désaffecter
                    for (EtudiantDTO e : members) {
                        etudiantService.setGroupe(e.getIdEtu(), f, s, null);
                        nbUnassigned++;
                    }

                    // 3) supprimer le groupe
                    groupeService.deleteGroupe(f, s, nom);

                } catch (Exception ex) {
                    error = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                setBusy(false);
                if (error != null) {
                    view.showError(error.getMessage());
                    return;
                }
                view.showInfo("Groupe " + nom + " supprimé. Étudiants désaffectés: " + nbUnassigned);
                reloadAsync();
            }
        }.execute();
    }

    // ===================== ASSIGN / UNASSIGN / CREATE =====================
    private void assignSelected() {
        EtudiantDTO etu = getSelectedStudent();
        GroupeDTO grp = getSelectedGroup();
        if (etu == null) {
            view.showError("Sélectionne un étudiant.");
            return;
        }
        if (grp == null || grp.getNomGroupe() == null || grp.getNomGroupe().isBlank()) {
            view.showError("Sélectionne un groupe.");
            return;
        }

        setBusy(true);
        new SwingWorker<Void, Void>() {
            Exception error;

            @Override
            protected Void doInBackground() {
                try {
                    etudiantService.setGroupe(
                            etu.getIdEtu(),
                            session.getIdFormation(),
                            session.getNumSemestre(),
                            grp.getNomGroupe()
                    );
                } catch (Exception ex) {
                    error = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                setBusy(false);
                if (error != null) {
                    view.showError(error.getMessage());
                    return;
                }
                reloadAsync();
            }
        }.execute();
    }

    private void unassignSelected() {
        EtudiantDTO etu = getSelectedStudent();
        if (etu == null) {
            view.showError("Sélectionne un étudiant.");
            return;
        }

        setBusy(true);
        new SwingWorker<Void, Void>() {
            Exception error;

            @Override
            protected Void doInBackground() {
                try {
                    etudiantService.setGroupe(
                            etu.getIdEtu(),
                            session.getIdFormation(),
                            session.getNumSemestre(),
                            null
                    );
                } catch (Exception ex) {
                    error = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                setBusy(false);
                if (error != null) {
                    view.showError(error.getMessage());
                    return;
                }
                reloadAsync();
            }
        }.execute();
    }

    private void createGroupAsync() {
        String nom = view.getTfCreateGroup().getText();
        if (nom == null || nom.isBlank()) {
            view.showError("Entre un nom de groupe (ex: G11).");
            return;
        }

        setBusy(true);
        new SwingWorker<Void, Void>() {
            Exception error;

            @Override
            protected Void doInBackground() {
                try {
                    groupeService.createGroupe(session.getIdFormation(), session.getNumSemestre(), nom.trim());
                } catch (Exception ex) {
                    error = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                setBusy(false);
                if (error != null) {
                    view.showError(error.getMessage());
                    return;
                }
                view.getTfCreateGroup().setText("");
                reloadAsync();
            }
        }.execute();
    }

    // ===================== SELECT HELPERS =====================
    private EtudiantDTO getSelectedStudent() {
        int row = view.getTbStudents().getSelectedRow();
        if (row < 0) return null;
        int modelRow = view.getTbStudents().convertRowIndexToModel(row);
        return view.getStudentsModel().getAt(modelRow);
    }

    private GroupeDTO getSelectedGroup() {
        int row = view.getTbGroups().getSelectedRow();
        if (row < 0) return null;
        int modelRow = view.getTbGroups().convertRowIndexToModel(row);
        return view.getGroupsModel().getAt(modelRow);
    }

    private void setBusy(boolean busy) {
        view.getBtnShowGroup().setEnabled(!busy);
        view.getBtnDeleteGroup().setEnabled(!busy);
        view.getBtnRefresh().setEnabled(!busy);
        view.getBtnAssign().setEnabled(!busy);
        view.getBtnUnassign().setEnabled(!busy);
        view.getBtnCreateGroup().setEnabled(!busy);
        view.getTfCreateGroup().setEnabled(!busy);
    }
}


