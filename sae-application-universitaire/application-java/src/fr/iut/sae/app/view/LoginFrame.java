package fr.iut.sae.app.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    // ================== COULEURS ==================
    private static final Color VIOLET = new Color(88, 24, 124);
    private static final Color VIOLET_SOFT = new Color(230, 220, 240);
    private static final Color BACKGROUND = new Color(243, 241, 247);
    private static final Color TEXT_DARK = new Color(45, 45, 45);

    // ================== COMPOSANTS ==================
    private final JTextField tfBaseUrl =
            new JTextField("https://projets.iut-orsay.fr/saes3-mbourem/api1");
    private final JTextField tfLogin = new JTextField();
    private final JPasswordField pfPassword = new JPasswordField();
    private final JButton btnLogin = new JButton("Connexion");
    private final JLabel lblStatus = new JLabel(" ");

    public LoginFrame() {
        setTitle("Plateforme Groupes TD / TP");
        setSize(600, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setContentPane(buildUI());
        getRootPane().setDefaultButton(btnLogin);
    }

    // ================== UI PRINCIPALE ==================
    private JPanel buildUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BACKGROUND);

        JPanel card = buildCard();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 60, 20, 60);

        root.add(card, gbc);
        return root;
    }

    // ================== CARTE ==================
    private JPanel buildCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(VIOLET_SOFT, 1, true),
                new EmptyBorder(25, 30, 25, 30)
        ));

        card.add(buildHeader());
        card.add(Box.createVerticalStrut(18));
        card.add(buildForm());
        card.add(Box.createVerticalStrut(16));
        card.add(buildButton());
        card.add(Box.createVerticalStrut(12));
        card.add(buildStatus());

        return card;
    }

    // ================== HEADER ==================
    private JPanel buildHeader() {
        JLabel title = new JLabel("Connexion");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(VIOLET);
        title.setAlignmentX(Component.CENTER_ALIGNMENT); // <-- CENTRÉ

        JLabel subtitle = new JLabel("Gestion des groupes TD / TP");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_DARK);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT); // <-- CENTRÉ

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(subtitle);

        return panel;
    }

    // ================== FORMULAIRE ==================
    private JPanel buildForm() {
        JPanel form = new JPanel(new GridLayout(6, 1, 0, 6));
        form.setBackground(Color.WHITE);

        form.add(createLabel("URL de l’API"));
        form.add(styleField(tfBaseUrl));

        form.add(createLabel("Identifiant"));
        form.add(styleField(tfLogin));

        form.add(createLabel("Mot de passe"));
        form.add(styleField(pfPassword));

        return form;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_DARK);
        return label;
    }

    private JComponent styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(VIOLET_SOFT, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return field;
    }

    // ================== BOUTON ==================
    private JPanel buildButton() {
        btnLogin.setBackground(VIOLET);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorder(new EmptyBorder(10, 0, 10, 0));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.add(btnLogin, BorderLayout.CENTER);

        return panel;
    }

    // ================== STATUS ==================
    private JLabel buildStatus() {
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(VIOLET);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        return lblStatus;
    }

    // ================== API CONTROLEUR ==================
    public JButton getBtnLogin() { return btnLogin; }
    public String getBaseUrl() { return tfBaseUrl.getText(); }
    public String getLogin() { return tfLogin.getText(); }
    public char[] getPassword() { return pfPassword.getPassword(); }

    public void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        tfBaseUrl.setEnabled(!loading);
        tfLogin.setEnabled(!loading);
        pfPassword.setEnabled(!loading);
        lblStatus.setText(loading ? "Connexion en cours..." : " ");
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Erreur de connexion",
                JOptionPane.ERROR_MESSAGE
        );
    }
}

