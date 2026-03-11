package fr.iut.sae.app.view;

import fr.iut.sae.app.model.dto.EtudiantDTO;
import fr.iut.sae.app.view.components.GroupsTableModel;
import fr.iut.sae.app.view.components.StudentsTableModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class ManualGroupsPanel extends JPanel {

    // ===================== THEME =====================
    private static final Color VIOLET = new Color(88, 24, 124);
    private static final Color VIOLET_SOFT = new Color(236, 228, 244);
    private static final Color BG = new Color(246, 244, 248);
    private static final Color CARD = Color.WHITE;
    private static final Color BORDER = new Color(228, 224, 236);
    private static final Color TEXT = new Color(35, 35, 40);
    private static final Color MUTED = new Color(110, 110, 120);
    private static final Color DANGER = new Color(163, 40, 140);

    // ===================== MODELS =====================
    private final StudentsTableModel studentsModel = new StudentsTableModel();
    private final GroupsTableModel groupsModel = new GroupsTableModel();

    private final JTable tbStudents = new JTable(studentsModel);
    private final JTable tbGroups = new JTable(groupsModel);

    private final JTextArea taDetails = new JTextArea();

    // ===================== ACTIONS =====================
    private final JButton btnShowGroup = new JButton("Voir membres");
    private final JButton btnDeleteGroup = new JButton("Supprimer");
    private final JButton btnRefresh = new JButton("Rafraîchir");
    private final JButton btnAssign = new JButton("Ajouter");
    private final JButton btnUnassign = new JButton("Retirer");
    private final JTextField tfCreateGroup = new JTextField();
    private final JButton btnCreateGroup = new JButton("Créer groupe");

    public ManualGroupsPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(14, 14, 14, 14));
        setBackground(BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        styleButtons();
        styleTables();

        tbStudents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbGroups.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    // ===================== HEADER =====================
    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(CARD);
        header.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("Gestion manuelle des groupes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel("Affectation, consultation et gestion des groupes");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(subtitle);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        tfCreateGroup.setColumns(10);
        styleField(tfCreateGroup);

        JLabel lblNew = new JLabel("Nouveau groupe :");
        lblNew.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNew.setForeground(MUTED);

        right.add(btnRefresh);
        right.add(new JSeparator(SwingConstants.VERTICAL));
        right.add(lblNew);
        right.add(tfCreateGroup);
        right.add(btnCreateGroup);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    // ===================== CENTER =====================
    private JComponent buildCenter() {

        // -------- LEFT : STUDENTS --------
        JPanel studentsCard = buildCard("Étudiants", "Sélectionne un étudiant");
        studentsCard.add(new JScrollPane(tbStudents), BorderLayout.CENTER);

        // -------- RIGHT TOP : DETAILS --------
        JPanel detailsCard = buildCard("Détails étudiant", "Informations détaillées");

        taDetails.setEditable(false);
        taDetails.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        taDetails.setBorder(new EmptyBorder(10, 10, 10, 10));
        taDetails.setBackground(new Color(250, 249, 252));
        taDetails.setForeground(TEXT);
        taDetails.setText("Sélectionne un étudiant pour voir ses informations.");

        JScrollPane spDetails = new JScrollPane(taDetails);
        spDetails.setBorder(new LineBorder(BORDER, 1, true));

        // ✅ hauteur minimale pour ne jamais être “invisible”
        spDetails.setMinimumSize(new Dimension(250, 180));
        detailsCard.add(spDetails, BorderLayout.CENTER);

        // -------- RIGHT BOTTOM : GROUPS --------
        JPanel groupsCard = buildCard("Groupes", "Gestion des groupes");
        groupsCard.add(new JScrollPane(tbGroups), BorderLayout.CENTER);

        JPanel groupActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        groupActions.setOpaque(false);
        groupActions.add(btnAssign);
        groupActions.add(btnUnassign);
        groupActions.add(new JSeparator(SwingConstants.VERTICAL));
        groupActions.add(btnShowGroup);
        groupActions.add(btnDeleteGroup);

        groupsCard.add(groupActions, BorderLayout.SOUTH);

        // ✅ FIX IMPORTANT : split vertical (Détails en haut, Groupes en bas)
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, detailsCard, groupsCard);
        rightSplit.setResizeWeight(0.40);          // 40% pour les détails
        rightSplit.setDividerSize(8);
        rightSplit.setBorder(null);
        rightSplit.setContinuousLayout(true);

        // Position initiale du divider (sinon selon Look&Feel ça peut mal tomber)
        SwingUtilities.invokeLater(() -> rightSplit.setDividerLocation(0.40));

        // -------- SPLIT PRINCIPAL --------
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, studentsCard, rightSplit);
        split.setResizeWeight(0.60);
        split.setDividerSize(8);
        split.setBorder(null);
        split.setContinuousLayout(true);

        return split;
    }

    private JPanel buildCard(String title, String subtitle) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 14));
        t.setForeground(TEXT);

        JLabel s = new JLabel(subtitle);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        s.setForeground(MUTED);

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.add(t);
        head.add(Box.createVerticalStrut(2));
        head.add(s);

        card.add(head, BorderLayout.NORTH);
        return card;
    }

    // ===================== PUBLIC API =====================
    public JTable getTbStudents() { return tbStudents; }
    public JTable getTbGroups() { return tbGroups; }
    public StudentsTableModel getStudentsModel() { return studentsModel; }
    public GroupsTableModel getGroupsModel() { return groupsModel; }

    public JButton getBtnShowGroup() { return btnShowGroup; }
    public JButton getBtnDeleteGroup() { return btnDeleteGroup; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnAssign() { return btnAssign; }
    public JButton getBtnUnassign() { return btnUnassign; }
    public JButton getBtnCreateGroup() { return btnCreateGroup; }
    public JTextField getTfCreateGroup() { return tfCreateGroup; }

    // ===================== DETAILS =====================
    public void showStudentDetails(EtudiantDTO e) {
        if (e == null) {
            taDetails.setText("Sélectionne un étudiant.");
            return;
        }
        taDetails.setText(
                "ID : " + e.getIdEtu() + "\n"
                        + "Nom : " + e.getNomEtu() + "\n"
                        + "Prénom : " + e.getPrenomEtu() + "\n"
                        + "Email : " + e.getEmailUniEtu() + "\n"
                        + "Bac : " + e.getTypeBac() + "\n"
                        + "Genre : " + (e.getGenreEtu() == 1 ? "F" : "M") + "\n"
                        + "Redoublant : " + (e.getEstRedoublant() == 1 ? "Oui" : "Non") + "\n"
                        + "Anglophone : " + (e.getEstAnglophone() == 1 ? "Oui" : "Non") + "\n"
                        + "Apprenti : " + (e.getEstApprenti() == 1 ? "Oui" : "Non") + "\n"
                        + "Indice covoit : " + e.getIndiceCovoiturage() + "\n"
                        + "Groupe : " + (e.getNomGroupe() == null || e.getNomGroupe().isBlank()
                        ? "(aucun)" : e.getNomGroupe())
        );
        taDetails.setCaretPosition(0);
    }

    // ===================== POPUP MEMBRES GROUPE =====================
    public void showGroupMembersPopup(String nomGroupe, List<EtudiantDTO> etudiants) {

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Membres — " + nomGroupe,
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setSize(520, 380);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));
        root.setBackground(Color.WHITE);

        JLabel title = new JLabel("Membres du groupe " + nomGroupe);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(VIOLET);

        JLabel count = new JLabel((etudiants == null ? 0 : etudiants.size()) + " étudiant(s)");
        count.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        count.setForeground(MUTED);

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.add(title);
        head.add(Box.createVerticalStrut(2));
        head.add(count);

        root.add(head, BorderLayout.NORTH);

        String[] cols = {"Prénom", "Nom", "Email"};
        Object[][] data;

        if (etudiants == null || etudiants.isEmpty()) {
            data = new Object[][]{{"(aucun)", "", ""}};
        } else {
            data = new Object[etudiants.size()][3];
            for (int i = 0; i < etudiants.size(); i++) {
                EtudiantDTO e = etudiants.get(i);
                data[i][0] = safe(e.getPrenomEtu());
                data[i][1] = safe(e.getNomEtu());
                data[i][2] = safe(e.getEmailUniEtu());
            }
        }

        JTable table = new JTable(data, cols);
        styleTable(table);
        table.setEnabled(false);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(BORDER, 1, true));
        root.add(sp, BorderLayout.CENTER);

        JButton btnClose = new JButton("Fermer");
        stylePrimary(btnClose);
        btnClose.addActionListener(e -> dialog.dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(btnClose);

        root.add(bottom, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private String safe(String s) { return s == null ? "" : s; }

    // ===================== STYLES =====================
    private void styleButtons() {
        stylePrimary(btnShowGroup);
        styleDanger(btnDeleteGroup);
        styleSecondary(btnRefresh);
        stylePrimary(btnAssign);
        styleSecondary(btnUnassign);
        stylePrimary(btnCreateGroup);
    }

    private void stylePrimary(JButton b) {
        b.setBackground(VIOLET);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(new EmptyBorder(8, 12, 8, 12));
    }

    private void styleSecondary(JButton b) {
        b.setBackground(Color.WHITE);
        b.setForeground(VIOLET);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
    }

    private void styleDanger(JButton b) {
        b.setBackground(DANGER);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(new EmptyBorder(8, 12, 8, 12));
    }

    private void styleField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setForeground(TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        f.setBackground(Color.WHITE);
    }

    private void styleTables() {
        styleTable(tbStudents);
        styleTable(tbGroups);
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(26);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(VIOLET_SOFT);

        JTableHeader h = table.getTableHeader();
        h.setBackground(VIOLET);
        h.setForeground(Color.WHITE);
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean focus, int row, int col) {

                super.getTableCellRendererComponent(t, v, sel, focus, row, col);

                if (!sel) setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 249, 252));
                setBorder(new EmptyBorder(0, 8, 0, 8));
                setForeground(TEXT);
                return this;
            }
        });
    }

    // ===================== MESSAGES =====================
    public void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}



