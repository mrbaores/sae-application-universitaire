package fr.iut.sae.app.controller;

import fr.iut.sae.app.model.state.AppSession;
import fr.iut.sae.app.service.ApiClient;
import fr.iut.sae.app.service.AuthService;
import fr.iut.sae.app.view.LoginFrame;
import fr.iut.sae.app.view.MainFrame;

import javax.swing.*;

public class LoginController {

    private final LoginFrame view;

    public LoginController(LoginFrame view) {
        this.view = view;

        view.getBtnLogin().addActionListener(e -> doLogin());
        view.getRootPane().setDefaultButton(view.getBtnLogin());
    }

    private void doLogin() {
        String baseUrl = view.getBaseUrl().trim();
        String login = view.getLogin().trim();
        String password = new String(view.getPassword());

        if (baseUrl.isBlank() || login.isBlank() || password.isBlank()) {
            view.showError("Base URL, login et mot de passe requis.");
            return;
        }

        view.setLoading(true);

        new SwingWorker<AuthService.LoginResult, Void>() {
            private AppSession session;
            private ApiClient apiClient;
            @Override
            protected AuthService.LoginResult doInBackground() throws Exception {
                session = new AppSession();
                apiClient = new ApiClient(baseUrl, session);
                AuthService authService = new AuthService(apiClient);
                AuthService.LoginResult res = authService.login(login, password);

                // stocker session
                session.setToken(res.getToken());
                session.setUser(res.getUser());

                return res;
            }

            @Override
            protected void done() {
                view.setLoading(false);
                try {
                    get();
                    MainFrame main = new MainFrame();
                    main.setUser(session.getUser());
                    new MainController(main, session, apiClient);
                    main.setVisible(true);
                    view.dispose();
                } catch (Exception ex) {
                    view.showError(ex.getMessage());
                }
            }
        }.execute();
    }
}
