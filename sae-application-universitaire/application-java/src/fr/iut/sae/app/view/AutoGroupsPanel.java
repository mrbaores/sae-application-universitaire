package fr.iut.sae.app.view;

import fr.iut.sae.app.algo.commun.modeleAlgo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

public class AutoGroupsPanel extends JPanel {

    private static final Color VIOLET = new Color(88, 24, 124);
    private static final Color VIOLET_SOFT = new Color(230, 220, 240);
    private static final Color BACKGROUND = new Color(243, 241, 247);

    private final JLabel lblTitle = new JLabel("Génération automatique");

    private final JComboBox<modeleAlgo.AlgorithmeGroupes> cbAlgo = new JComboBox<>();
    private final JTextField tfNbGroupes = new JTextField("10");

    private final JButton btnRafraichir = new JButton("Rafraîchir");
    private final JButton btnLancer = new JButton("Créer et affecter (Base)");

    private final JTextArea taLog = new JTextArea();

    public AutoGroupsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(buildTop(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        setupComboRenderer();
        styleButtons();
    }

    private JPanel buildTop() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(VIOLET_SOFT, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(VIOLET);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 6;
        gbc.weightx = 1.0;
        card.add(lblTitle, gbc);

        gbc.gridwidth = 1;
        gbc.weightx = 0.0;

        JLabel lblAlgo = new JLabel("Algorithme :");
        lblAlgo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        gbc.gridy = 1;
        gbc.gridx = 0;
        card.add(lblAlgo, gbc);

        cbAlgo.setPreferredSize(new Dimension(240, 30));
        cbAlgo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbAlgo.setBorder(new LineBorder(VIOLET_SOFT, 1, true));

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        card.add(cbAlgo, gbc);

        JLabel lblNb = new JLabel("Nombre de groupes :");
        lblNb.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        card.add(lblNb, gbc);

        tfNbGroupes.setColumns(4);
        tfNbGroupes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tfNbGroupes.setBorder(new LineBorder(VIOLET_SOFT, 1, true));

        gbc.gridx = 3;
        card.add(tfNbGroupes, gbc);

        gbc.gridx = 4;
        card.add(btnRafraichir, gbc);

        gbc.gridx = 5;
        card.add(btnLancer, gbc);

        return card;
    }

    private JPanel buildCenter() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(VIOLET_SOFT, 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel lbl = new JLabel("Journal");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(VIOLET);
        card.add(lbl, BorderLayout.NORTH);

        taLog.setEditable(false);
        taLog.setLineWrap(true);
        taLog.setWrapStyleWord(true);
        taLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        taLog.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane sp = new JScrollPane(taLog);
        sp.setBorder(new LineBorder(VIOLET_SOFT, 1, true));

        card.add(sp, BorderLayout.CENTER);

        return card;
    }

    private void setupComboRenderer() {
        cbAlgo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof modeleAlgo.AlgorithmeGroupes) {
                    modeleAlgo.AlgorithmeGroupes a = (modeleAlgo.AlgorithmeGroupes) value;
                    setText(a.nom());
                } else if (value == null) {
                    setText("(aucun)");
                } else {
                    setText(value.toString());
                }

                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                return this;
            }
        });
    }

    private void styleButtons() {
        styleButtonPrimary(btnLancer);
        styleButtonSecondary(btnRafraichir);
    }

    private void styleButtonPrimary(JButton b) {
        b.setBackground(VIOLET);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(new EmptyBorder(8, 12, 8, 12));
    }

    private void styleButtonSecondary(JButton b) {
        b.setBackground(new Color(250, 250, 252));
        b.setForeground(VIOLET);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(VIOLET_SOFT, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
    }

    // ✅ AJOUTÉ : permet au controller de changer le titre selon le semestre
    public void setTitre(String titre) {
        if (titre == null) return;
        String t = titre.trim();
        if (t.isEmpty()) return;
        lblTitle.setText(t);
    }

    public JButton getBtnRafraichir() { return btnRafraichir; }
    public JButton getBtnLancer() { return btnLancer; }

    public void setBusy(boolean busy) {
        btnRafraichir.setEnabled(!busy);
        btnLancer.setEnabled(!busy);
        cbAlgo.setEnabled(!busy);
        tfNbGroupes.setEnabled(!busy);
    }

    public void setAlgorithmes(List<modeleAlgo.AlgorithmeGroupes> algos) {
        cbAlgo.removeAllItems();
        if (algos == null) return;

        for (int i = 0; i < algos.size(); i++) {
            cbAlgo.addItem(algos.get(i));
        }

        if (cbAlgo.getItemCount() > 0) {
            cbAlgo.setSelectedIndex(0);
        }
    }

    public int getAlgoSelectionneIndex() {
        return cbAlgo.getSelectedIndex();
    }

    public int getNombreGroupesSaisi() {
        try {
            return Integer.parseInt(tfNbGroupes.getText().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public void clearLog() {
        taLog.setText("");
    }

    public void appendLog(String line) {
        taLog.append(line + "\n");
    }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}

