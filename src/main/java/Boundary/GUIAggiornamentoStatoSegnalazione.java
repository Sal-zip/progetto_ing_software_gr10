package Boundary;

import Control.ControllerServiziSistema;


import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Boundary per l'aggiornamento dello stato di una segnalazione.
 *
 * Applica il principio di riuso del software tramite COMPOSIZIONE: incorpora
 * e utilizza direttamente la GUIConsultazioneSegnalazioniRicevute per il caricamento,
 * il filtraggio visivo e la gestione della tabella delle segnalazioni, evitando
 * ridondanze nel livello Boundary.
 *
 * La classe è coerente con il modello BCED perché:
 * - riceve solo dati semplici di sessione;
 * - non mantiene istanze Entity dell'utente autenticato;
 * - comunica solo con ControllerServiziSistema;
 * - delega il caricamento visivo dell'elenco alla Boundary di consultazione associata;
 * - mostra il prossimo stato solo a scopo informativo, senza inviarlo al Control.
 */
public class GUIAggiornamentoStatoSegnalazione {

    private static final String STATO_INVIATA = "inviata";
    private static final String STATO_PRESA_IN_CARICO = "presa_in_carico";
    private static final String STATO_IN_LAVORAZIONE = "in_lavorazione";
    private static final String STATO_RISOLTA = "risolta";

    private JPanel contentPanel;

    private JLabel logoLabel;
    private JLabel titoloSistemaLabel;
    private JLabel titoloPaginaLabel;
    private JLabel descrizioneLabel;
    private JLabel idSegnalazioneLabel;
    private JLabel nuovoStatoLabel;
    private JLabel esitoLabel;
    private JLabel statoCorrenteLabel;
    private JLabel messaggioConfermaLabel;


    private JComboBox<String> boxNuovoStato;

    private JButton confermaAggiornamentoButton;
    private JButton annullaButton;

    private ControllerServiziSistema controllerServiziSistema;


    /**
     * Dati semplici della sessione dell'utente autenticato.
     */
    private Map<String, Object> datiSessioneUtente;

    /**
     * Identificativo dell'utente autenticato estratto dalla Map di sessione.
     */
    private Long idUtenteAutenticato;

    private Long idSegnalazione;
    private String statoCorrente;

    /**
     * Costruisce la GUI di aggiornamento stato per una segnalazione già selezionata.
     *
     * La GUI riceve solo dati semplici di sessione e l'identificativo della
     * segnalazione selezionata nella GUIConsultazioneSegnalazioniRicevute.
     * Non riceve né conserva istanze Entity, mantenendo separata la Boundary
     * dal livello di dominio.
     *
     * @param datiSessioneUtente mappa contenente almeno idUtente, nome, cognome e ruolo
     * @param idSegnalazione identificativo della segnalazione selezionata
     */
    public GUIAggiornamentoStatoSegnalazione(
            Map<String, Object> datiSessioneUtente,
            Long idSegnalazione
    ) {
        this.datiSessioneUtente = datiSessioneUtente;
        this.idUtenteAutenticato = estraiIdUtenteDaSessione(datiSessioneUtente);
        this.idSegnalazione = idSegnalazione;
        this.statoCorrente = null;

        this.controllerServiziSistema = new ControllerServiziSistema();

        inizializzaComponenti();
        configuraLayout();
        configuraAzioni();

        caricaStatoCorrenteSegnalazione();
        richiediAggiornamentoStato(idSegnalazione);
    }

    /**
     * Restituisce il pannello principale della GUI.
     *
     * @return pannello principale
     */
    public JPanel getContentPanel() {
        return contentPanel;
    }


    public void richiediAggiornamentoStato(Long idSegnalazione) {
        this.idSegnalazione = idSegnalazione;

        if (idSegnalazione == null) {
            idSegnalazioneLabel.setText("ID Segnalazione: -");
        } else {
            idSegnalazioneLabel.setText("ID Segnalazione: " + idSegnalazione);
        }

        if (isVuoto(statoCorrente)) {
            statoCorrenteLabel.setText("Stato corrente: -");
        } else {
            statoCorrenteLabel.setText("Stato corrente: " + statoCorrente);
        }

        aggiornaStatiApplicabili();
    }

    /**
     * Invia la richiesta di aggiornamento al Facade Controller.
     *
     * La Boundary non decide il nuovo stato e non consente la selezione arbitraria
     * di uno stato qualsiasi. Il valore mostrato nella combo box ha solo funzione
     * informativa: l'avanzamento reale segue il ciclo di vita previsto della
     * Segnalazione ed è determinato nel livello Entity tramite il Pattern State.
     *
     * @param idSegnalazione identificativo della segnalazione da aggiornare
     */
    public void inviaRichiestaAggiornamento(Long idSegnalazione) {
        try {
            if (idUtenteAutenticato == null) {
                throw new IllegalArgumentException(
                        "Sessione utente non valida. Effettua nuovamente l'accesso."
                );
            }

            if (datiSessioneUtente == null) {
                throw new IllegalArgumentException(
                        "Dati di sessione non disponibili. Effettua nuovamente l'accesso."
                );
            }

            String ruolo = valoreTestuale(datiSessioneUtente.get("ruolo"));

            if ("Cittadino".equalsIgnoreCase(ruolo)) {
                throw new IllegalArgumentException(
                        "Privilegi insufficienti. Solo un Operatore Comunale può aggiornare lo stato."
                );
            }

            if (idSegnalazione == null) {
                throw new IllegalArgumentException(
                        "Seleziona una segnalazione prima di confermare l'aggiornamento."
                );
            }

            boolean aggiornamentoRiuscito =
                    controllerServiziSistema.aggiornaStatoSegnalazione(
                            idUtenteAutenticato,
                            idSegnalazione
                    );

            if (!aggiornamentoRiuscito) {
                throw new IllegalArgumentException(
                        "Aggiornamento non completato. Verifica che la segnalazione possa avanzare di stato."
                );
            }

            comunicaConfermaAggiornamento();
            tornaAllaDashboard();

        } catch (IllegalArgumentException e) {
            segnalaErroreAggiornamentoStato(e.getMessage());
        }
    }

    /**
     * Comunica la corretta conclusione dell'aggiornamento.
     */
    public void comunicaConfermaAggiornamento() {
        esitoLabel.setText("Stato aggiornato correttamente.");

        JOptionPane.showMessageDialog(
                contentPanel,
                "Stato della segnalazione aggiornato correttamente.",
                "Aggiornamento completato",
                JOptionPane.INFORMATION_MESSAGE,
                caricaIconaLogoPopup()
        );
    }

    /**
     * Comunica un errore grafico.
     */
    public void segnalaErroreAggiornamentoStato(String messaggio) {
        String messaggioDaMostrare = valoreTestuale(messaggio);
        if (isVuoto(messaggioDaMostrare)) {
            messaggioDaMostrare = "Seleziona una segnalazione valida e verifica che possa essere aggiornata.";
        }

        esitoLabel.setText("Errore durante l'aggiornamento dello stato.");

        JOptionPane.showMessageDialog(
                contentPanel,
                messaggioDaMostrare,
                "Errore aggiornamento stato",
                JOptionPane.ERROR_MESSAGE,
                caricaIconaLogoPopup()
        );
    }

    private void inizializzaComponenti() {
        contentPanel = new JPanel();
        logoLabel = new JLabel();
        titoloSistemaLabel = new JLabel("Sistema Comunale Gestione Segnalazioni");
        titoloPaginaLabel = new JLabel("<html>Aggiornamento <span style='color:#1E88E5;'>stato</span></html>");
        descrizioneLabel = new JLabel(
                "Conferma l'avanzamento dello stato della segnalazione selezionata."
        );

        idSegnalazioneLabel = new JLabel("ID Segnalazione: -");
        statoCorrenteLabel = new JLabel("Stato corrente: -");
        nuovoStatoLabel = new JLabel("Prossimo stato");
        esitoLabel = new JLabel("Utilizzo modulo consultazione condiviso attivo.");
        messaggioConfermaLabel = new JLabel("Seleziona una segnalazione per visualizzare l'azione disponibile.");
        messaggioConfermaLabel.setFont(new Font("Arial", Font.PLAIN, 14));


        boxNuovoStato = new JComboBox<>();
        confermaAggiornamentoButton = new JButton("Conferma Aggiornamento");
        annullaButton = new JButton("Torna alla Dashboard");

        boxNuovoStato.removeAllItems();
        boxNuovoStato.setEnabled(false);
        confermaAggiornamentoButton.setEnabled(false);

        caricaLogo();
    }

    private void configuraLayout() {
        contentPanel.setLayout(new BorderLayout(20, 20));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
        contentPanel.setPreferredSize(new Dimension(850, 520));

        contentPanel.add(creaHeaderPanel(), BorderLayout.NORTH);
        contentPanel.add(creaCentroPanel(), BorderLayout.CENTER);
        contentPanel.add(creaFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel creaHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JPanel titoloConLogoPanel = new JPanel(new BorderLayout());
        JLabel spazioSinistro = new JLabel();
        spazioSinistro.setPreferredSize(new Dimension(75, 75));

        titoloSistemaLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titoloSistemaLabel.setHorizontalAlignment(SwingConstants.CENTER);

        titoloConLogoPanel.add(spazioSinistro, BorderLayout.WEST);
        titoloConLogoPanel.add(titoloSistemaLabel, BorderLayout.CENTER);
        titoloConLogoPanel.add(logoLabel, BorderLayout.EAST);

        titoloPaginaLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titoloPaginaLabel.setHorizontalAlignment(SwingConstants.CENTER);
        descrizioneLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        descrizioneLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel titoloPaginaPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titoloPaginaPanel.add(titoloPaginaLabel);
        JPanel descrizionePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        descrizionePanel.add(descrizioneLabel);

        headerPanel.add(titoloConLogoPanel);
        headerPanel.add(Box.createVerticalStrut(12));
        headerPanel.add(new JSeparator());
        headerPanel.add(Box.createVerticalStrut(18));
        headerPanel.add(titoloPaginaPanel);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(descrizionePanel);

        return headerPanel;
    }

    /**
     * Crea la parte centrale della GUI di aggiornamento stato.
     *
     * Poiché la segnalazione è già stata selezionata nella GUI di consultazione
     * delle segnalazioni ricevute, questo pannello mostra solo le informazioni
     * necessarie all'aggiornamento e non incorpora più la tabella delle segnalazioni.
     *
     * @return pannello centrale della GUI
     */
    private JPanel creaCentroPanel() {
        JPanel centroPanel = new JPanel(new BorderLayout(12, 12));

        JPanel contenitoreAggiornamento = new JPanel(new BorderLayout());
        contenitoreAggiornamento.add(creaPannelloAggiornamento(), BorderLayout.NORTH);

        centroPanel.add(contenitoreAggiornamento, BorderLayout.NORTH);

        return centroPanel;
    }

    private JPanel creaPannelloAggiornamento() {
        JPanel pannelloAggiornamento = new JPanel(new GridBagLayout());
        pannelloAggiornamento.setBorder(BorderFactory.createTitledBorder("Aggiornamento stato"));
        GridBagConstraints vincoli = new GridBagConstraints();
        vincoli.insets = new Insets(6, 8, 6, 8);
        vincoli.fill = GridBagConstraints.HORIZONTAL;

        idSegnalazioneLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statoCorrenteLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nuovoStatoLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        vincoli.gridx = 0; vincoli.gridy = 0; vincoli.gridwidth = 2; vincoli.weightx = 1.0;
        pannelloAggiornamento.add(idSegnalazioneLabel, vincoli);

        vincoli.gridx = 0; vincoli.gridy = 1; vincoli.gridwidth = 2; vincoli.weightx = 1.0;
        pannelloAggiornamento.add(statoCorrenteLabel, vincoli);

        vincoli.gridx = 0; vincoli.gridy = 2; vincoli.gridwidth = 1; vincoli.weightx = 0.25;
        pannelloAggiornamento.add(nuovoStatoLabel, vincoli);

        vincoli.gridx = 1;
        vincoli.gridy = 2;
        vincoli.weightx = 0.75;
        boxNuovoStato.setPreferredSize(new Dimension(260, 32));
        pannelloAggiornamento.add(boxNuovoStato, vincoli);

        vincoli.gridx = 0;
        vincoli.gridy = 3;
        vincoli.gridwidth = 2;
        vincoli.weightx = 1.0;
        messaggioConfermaLabel.setHorizontalAlignment(SwingConstants.LEFT);
        pannelloAggiornamento.add(messaggioConfermaLabel, vincoli);

        return pannelloAggiornamento;
    }

    private JPanel creaFooterPanel() {
        JPanel footerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        footerPanel.add(confermaAggiornamentoButton);
        footerPanel.add(annullaButton);
        return footerPanel;
    }

    /**
     * Collega i pulsanti della GUI alle rispettive azioni.
     *
     * La GUI non seleziona direttamente la segnalazione, poiché riceve
     * idSegnalazione dalla GUIConsultazioneSegnalazioniRicevute.
     */
    private void configuraAzioni() {
        confermaAggiornamentoButton.addActionListener(
                e -> inviaRichiestaAggiornamento(idSegnalazione)
        );

        annullaButton.addActionListener(
                e -> tornaAllaDashboard()
        );
    }

    private void tornaAllaDashboard() {
        if (datiSessioneUtente == null || idUtenteAutenticato == null) {
            segnalaErroreAggiornamentoStato("Sessione utente non valida. Impossibile tornare alla dashboard.");
            return;
        }
        GUIDashboardUtente guiDashboard = new GUIDashboardUtente(datiSessioneUtente);
        JFrame frameDashboard = new JFrame("Dashboard Utente");
        frameDashboard.setContentPane(guiDashboard.getContentPanel());
        frameDashboard.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameDashboard.pack();
        frameDashboard.setLocationRelativeTo(null);
        frameDashboard.setVisible(true);

        Window finestraCorrente = SwingUtilities.getWindowAncestor(contentPanel);
        if (finestraCorrente != null) {
            finestraCorrente.dispose();
        }
    }

    /**
     * Aggiorna gli stati applicabili e il messaggio esplicativo mostrato
     * all'operatore prima della conferma.
     *
     * La GUI mostra il prossimo stato solo a scopo informativo: la transizione
     * effettiva resta gestita nel livello Entity tramite il Pattern State.
     */
    private void aggiornaStatiApplicabili() {
        boxNuovoStato.removeAllItems();

        if (isVuoto(statoCorrente)) {
            boxNuovoStato.setEnabled(false);
            confermaAggiornamentoButton.setEnabled(false);
            messaggioConfermaLabel.setText(
                    "Seleziona una segnalazione per visualizzare l'azione disponibile."
            );
            return;
        }

        String prossimoStato = recuperaProssimoStatoVisualizzabile(statoCorrente);

        if (isVuoto(prossimoStato)) {
            boxNuovoStato.setEnabled(false);
            confermaAggiornamentoButton.setEnabled(false);
            esitoLabel.setText("La segnalazione selezionata non può essere ulteriormente aggiornata.");
            messaggioConfermaLabel.setText(
                    "La segnalazione selezionata risulta già risolta e non può essere ulteriormente aggiornata."
            );
            return;
        }

        boxNuovoStato.addItem(prossimoStato);
        boxNuovoStato.setSelectedIndex(0);
        boxNuovoStato.setEnabled(false);
        confermaAggiornamentoButton.setEnabled(true);

        esitoLabel.setText("La segnalazione sarà aggiornata allo stato: " + prossimoStato);
        messaggioConfermaLabel.setText(generaMessaggioConfermaAvanzamento(statoCorrente));
    }

    private String recuperaProssimoStatoVisualizzabile(String statoCorrente) {
        if (STATO_INVIATA.equals(statoCorrente)) return STATO_PRESA_IN_CARICO;
        if (STATO_PRESA_IN_CARICO.equals(statoCorrente)) return STATO_IN_LAVORAZIONE;
        if (STATO_IN_LAVORAZIONE.equals(statoCorrente)) return STATO_RISOLTA;
        return null;
    }

    private void caricaLogo() {
        java.net.URL urlLogo = getClass().getResource("/images/logo.png");
        if (urlLogo == null) {
            logoLabel.setText("");
            return;
        }
        logoLabel.setIcon(new ImageIcon(new ImageIcon(urlLogo).getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH)));
    }

    private ImageIcon caricaIconaLogoPopup() {
        java.net.URL urlLogo = getClass().getResource("/images/logo.png");
        return urlLogo == null ? null : new ImageIcon(new ImageIcon(urlLogo).getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH));
    }

    private Long estraiLong(Object valore) {
        if (valore == null) return null;
        if (valore instanceof Long) return (Long) valore;
        if (valore instanceof Number) return ((Number) valore).longValue();
        try { return Long.parseLong(valore.toString().trim()); } catch (NumberFormatException e) { return null; }
    }

    private String valoreTestuale(Object valore) {
        return valore == null ? "" : valore.toString().trim();
    }

    private boolean isVuoto(String valore) {
        return valore == null || valore.trim().isEmpty();
    }

    private Long estraiIdUtenteDaSessione(Map<String, Object> datiSessioneUtente) {
        if (datiSessioneUtente == null) return null;
        return estraiLong(datiSessioneUtente.get("idUtente"));
    }

    /**
     * Recupera lo stato corrente della segnalazione selezionata.
     *
     * La Boundary non accede direttamente a Entity o Database. Richiede al
     * Facade Controller i dettagli della segnalazione e legge dalla Map ricevuta
     * solo il valore testuale dello stato corrente.
     *
     * Lo stato viene usato solo per mostrare all'operatore il prossimo avanzamento
     * previsto. La transizione reale resta comunque gestita dal livello Entity
     * tramite il Pattern State.
     */
    private void caricaStatoCorrenteSegnalazione() {

        if (idUtenteAutenticato == null || idSegnalazione == null) {
            statoCorrente = null;
            return;
        }

        Map<String, Object> dettaglio =
                controllerServiziSistema.visualizzaDettagliSegnalazione(
                        idUtenteAutenticato,
                        idSegnalazione
                );

        if (dettaglio == null || dettaglio.isEmpty()) {
            statoCorrente = null;
            return;
        }

        String stato = valoreTestuale(dettaglio.get("statoCorrente"));

        if (isVuoto(stato)) {
            stato = valoreTestuale(dettaglio.get("nomeStatoCorrente"));
        }

        statoCorrente = stato;
    }

    /**
     * Genera il messaggio esplicativo relativo all'avanzamento di stato.
     *
     * Il messaggio viene usato solo dalla Boundary per chiarire all'operatore
     * l'azione che sta confermando. Non determina la transizione reale, che resta
     * responsabilità della Segnalazione tramite il Pattern State.
     *
     * @param statoCorrente stato corrente della segnalazione selezionata
     * @return messaggio di conferma da mostrare nella GUI
     */
    private String generaMessaggioConfermaAvanzamento(String statoCorrente) {

        String stato = valoreTestuale(statoCorrente);

        if (STATO_INVIATA.equals(stato)) {
            return "La segnalazione selezionata si trova nello stato \"inviata\", "
                    + "vuoi prenderla in carico per la gestione?";
        }

        if (STATO_PRESA_IN_CARICO.equals(stato)) {
            return "La segnalazione selezionata si trova nello stato \"presa_in_carico\", "
                    + "vuoi dare inizio all'intervento?";
        }

        if (STATO_IN_LAVORAZIONE.equals(stato)) {
            return "La segnalazione selezionata si trova nello stato \"in_lavorazione\", "
                    + "confermi la risoluzione dell'intervento?";
        }

        if (STATO_RISOLTA.equals(stato)) {
            return "La segnalazione selezionata si trova nello stato \"risolta\" "
                    + "e non può essere ulteriormente aggiornata.";
        }

        return "Verifica lo stato della segnalazione prima di procedere.";
    }
}