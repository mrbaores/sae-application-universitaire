package fr.iut.sae.app.view;

import fr.iut.sae.app.model.dto.FormationDTO;
import fr.iut.sae.app.model.dto.SemestreDTO;
import fr.iut.sae.app.model.dto.UserDTO;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {

    private final JLabel lbUser = new JLabel();
    private final JComboBox<FormationDTO> cbFormation = new JComboBox<>();
    private final JComboBox<SemestreDTO> cbSemestre = new JComboBox<>();
    private final JButton btnReload = new JButton("Recharger");
    private final JButton btnLogout = new JButton("Déconnexion");

    private final ManualGroupsPanel manualPanel = new ManualGroupsPanel();
    private final AutoGroupsPanel autoPanel = new AutoGroupsPanel();

    public MainFrame() {
        super("SAÉ - Création de groupes TD/TP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Manuel", manualPanel);
        tabs.addTab("Automatique", autoPanel);

        JPanel top = new JPanel(new GridBagLayout());
        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 6, 0, 6);
        c.gridy = 0;

        c.gridx = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        top.add(lbUser, c);

        c.weightx = 0;
        c.gridx = 1;
        top.add(new JLabel("Formation :"), c);
        c.gridx = 2;
        cbFormation.setPreferredSize(new Dimension(220, 28));
        top.add(cbFormation, c);

        c.gridx = 3;
        top.add(new JLabel("Semestre :"), c);
        c.gridx = 4;
        cbSemestre.setPreferredSize(new Dimension(120, 28));
        top.add(cbSemestre, c);

        c.gridx = 5;
        top.add(btnReload, c);
        c.gridx = 6;
        top.add(btnLogout, c);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    public void setUser(UserDTO user) {
        lbUser.setText("Connecté : " + (user == null ? "?" : user.getLogin() + " (" + user.getRole() + ")"));
    }

    public void setFormations(List<FormationDTO> formations) {
        cbFormation.removeAllItems();
        for (FormationDTO f : formations) {
            cbFormation.addItem(f);
        }
        cbFormation.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel l = new JLabel(value == null ? "" : value.getIdFormation() + " - " + value.getNomFormation());
            l.setOpaque(true);
            if (isSelected) {
                l.setBackground(list.getSelectionBackground());
                l.setForeground(list.getSelectionForeground());
            }
            return l;
        });
    }

    public void selectFormationById(int idFormation) {
        ComboBoxModel<FormationDTO> m = cbFormation.getModel();
        for (int i = 0; i < m.getSize(); i++) {
            FormationDTO f = m.getElementAt(i);
            if (f != null && f.getIdFormation() == idFormation) {
                cbFormation.setSelectedIndex(i);
                return;
            }
        }
    }

    public void setSemestres(List<SemestreDTO> semestres) {
        cbSemestre.removeAllItems();
        for (SemestreDTO s : semestres) {
            cbSemestre.addItem(s);
        }
        cbSemestre.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel l = new JLabel(value == null ? "" : ("S" + value.getNumSemestre()));
            l.setOpaque(true);
            if (isSelected) {
                l.setBackground(list.getSelectionBackground());
                l.setForeground(list.getSelectionForeground());
            }
            return l;
        });
    }

    public void selectSemestreByNum(int numSemestre) {
        ComboBoxModel<SemestreDTO> m = cbSemestre.getModel();
        for (int i = 0; i < m.getSize(); i++) {
            SemestreDTO s = m.getElementAt(i);
            if (s != null && s.getNumSemestre() == numSemestre) {
                cbSemestre.setSelectedIndex(i);
                return;
            }
        }
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public JComboBox<FormationDTO> getCbFormation() { return cbFormation; }
    public JComboBox<SemestreDTO> getCbSemestre() { return cbSemestre; }
    public JButton getBtnReload() { return btnReload; }
    public JButton getBtnLogout() { return btnLogout; }

    public ManualGroupsPanel getManualPanel() { return manualPanel; }
    public AutoGroupsPanel getAutoPanel() { return autoPanel; }
}
