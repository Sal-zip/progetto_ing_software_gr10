package Boundary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * Boundary mostrata dopo l'autenticazione dell'utente.
 *
 * La dashboard non gestisce logica di dominio, non accede direttamente
 * alla persistenza e non mantiene istanze Entity.
 *
 * Per mantenere chiusa l'architettura BCED, riceve solo dati semplici
 * di sessione, come idUtente, nome, cognome e ruolo.
 *
 * Nel modello BCED la dashboard espone solo selezionaFunzionalita(),
 * mentre i metodi di apertura delle singole GUI sono dettagli tecnici
 * privati della Boundary.
 */
public class GUIDashboardUtente {

    private static final String RUOLO_CITTADINO = "Cittadino";
    private static final String RUOLO_OPERATORE_COMUNALE = "Operatore Comunale";

    private JPanel contentPanel;

    private JLabel logoLabel;
    private JLabel titoloSistemaLabel;
    private JLabel dashboardLabel;
    private JLabel benvenutoLabel;
    private JLabel ruoloLabel;
    private JLabel descrizioneLabel;
    private JLabel funzionalitaLabel;
    private JLabel footerLabel;

    private JButton creaSegnalazioneButton;
    private JButton consultaInviateButton;
    private JButton consultaRicevuteButton;
    private JButton aggiornaStatoButton;
    private JButton monitoraAttivitaButton;
    private JButton logoutButton;

    private JPanel creaSegnalazionePanel;
    private JPanel consultaInviatePanel;
    private JPanel consultaRicevutePanel;
    private JPanel aggiornaStatoPanel;
    private JPanel monitoraAttivitaPanel;
    private JPanel logoutPanel;

    private Long idUtenteAutenticato;
    private String nomeUtenteAutenticato;
    private String cognomeUtenteAutenticato;
    private String ruoloUtenteAutenticato;

    /**
     * Dati semplici della sessione dell'utente autenticato.
     *
     * La Boundary conserva questa Map solo per poter passare idUtente, nome,
     * cognome e ruolo alle GUI successive, senza mantenere o propagare istanze
     * Entity tra Boundary.
     */
    private Map<String, Object> datiSessioneUtente;

    /**
     * Costruisce la dashboard dell'utente autenticato.
     *
     * La dashboard riceve solo dati semplici di sessione prodotti dal flusso
     * di accesso, evitando il passaggio diretto di istanze Entity alla Boundary.
     *
     * I dati attesi nella mappa sono:
     * - idUtente;
     * - nome;
     * - cognome;
     * - ruolo.
     *
     * @param datiSessioneUtente mappa contenente i dati semplici dell'utente autenticato
     */
    public GUIDashboardUtente(Map<String, Object> datiSessioneUtente) {
        acquisisciDatiSessione(datiSessioneUtente);

        inizializzaComponenti();
        configuraLayout();
        configuraDashboard();
        selezionaFunzionalita();
    }

    /**
     * Acquisisce dalla mappa i dati semplici dell'utente autenticato.
     *
     * Il metodo non accede a Entity e non recupera dati dalla persistenza.
     * Legge soltanto le informazioni di sessione già prodotte dal livello
     * Control/Facade Entity durante l'accesso.
     *
     * @param datiSessioneUtente mappa contenente idUtente, nome, cognome e ruolo
     */
    private void acquisisciDatiSessione(Map<String, Object> datiSessioneUtente) {

        this.datiSessioneUtente = datiSessioneUtente;

        if (datiSessioneUtente == null) {
            this.idUtenteAutenticato = null;
            this.nomeUtenteAutenticato = "";
            this.cognomeUtenteAutenticato = "";
            this.ruoloUtenteAutenticato = "";
            return;
        }

        this.idUtenteAutenticato = estraiLong(datiSessioneUtente.get("idUtente"));
        this.nomeUtenteAutenticato = valoreTestuale(datiSessioneUtente.get("nome"));
        this.cognomeUtenteAutenticato = valoreTestuale(datiSessioneUtente.get("cognome"));
        this.ruoloUtenteAutenticato = valoreTestuale(datiSessioneUtente.get("ruolo"));
    }

    /**
     * Inizializza tutti i componenti grafici della dashboard.
     *
     * Il metodo costruisce solo componenti Swing e non contiene logica
     * applicativa o di dominio.
     */
    private void inizializzaComponenti() {
        contentPanel = new JPanel();

        logoLabel = new JLabel();
        titoloSistemaLabel = new JLabel("Sistema Comunale Gestione Segnalazioni");
        dashboardLabel = new JLabel("Dashboard Utente");
        benvenutoLabel = new JLabel();
        ruoloLabel = new JLabel();
        descrizioneLabel = new JLabel();
        funzionalitaLabel = new JLabel("Funzionalità disponibili");
        footerLabel = new JLabel("Progetto di Ingegneria del Software - GR10");

        creaSegnalazioneButton = new JButton("Crea nuova segnalazione");
        consultaInviateButton = new JButton("Consulta segnalazioni inviate");
        consultaRicevuteButton = new JButton("Consulta segnalazioni ricevute");
        aggiornaStatoButton = new JButton("Aggiorna stato segnalazione");
        monitoraAttivitaButton = new JButton("Monitora andamento attività");
        logoutButton = new JButton("Logout");

        caricaLogo();
    }

    /**
     * Carica il logo dalle risorse del progetto.
     *
     * Il metodo ha solo responsabilità grafica. Non accede a Control,
     * Entity o Database.
     */
    private void caricaLogo() {
        java.net.URL urlLogo = getClass().getResource("/images/logo.png");

        if (urlLogo == null) {
            System.err.println("[GUIDashboardUtente] Logo non trovato: /images/logo.png");
            logoLabel.setText("");
            return;
        }

        ImageIcon logo = new ImageIcon(urlLogo);
        Image immagine = logo.getImage();

        Image immagineRidimensionata = immagine.getScaledInstance(
                80,
                80,
                Image.SCALE_SMOOTH
        );

        logoLabel.setIcon(new ImageIcon(immagineRidimensionata));
        logoLabel.setText("");
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    /**
     * Definisce la struttura grafica principale della dashboard.
     *
     * Il metodo organizza i pannelli Swing senza accedere al dominio
     * applicativo.
     */
    private void configuraLayout() {
        contentPanel.setLayout(new BorderLayout(20, 20));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 40, 20, 40));
        contentPanel.setPreferredSize(new Dimension(650, 760));

        JPanel headerPanel = creaHeaderPanel();
        JPanel centroPanel = creaCentroPanel();
        JPanel footerPanel = creaFooterPanel();

        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(centroPanel, BorderLayout.CENTER);
        contentPanel.add(footerPanel, BorderLayout.SOUTH);
    }

    /**
     * Crea la parte superiore della dashboard.
     *
     * Il metodo costruisce solo componenti grafici e non accede a dati
     * di dominio.
     *
     * @return pannello superiore della dashboard
     */
    private JPanel creaHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        titoloSistemaLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titoloSistemaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(logoLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(titoloSistemaLabel);
        headerPanel.add(Box.createVerticalStrut(15));
        headerPanel.add(new JSeparator());

        return headerPanel;
    }

    /**
     * Crea la parte centrale della dashboard.
     *
     * Il pannello centrale contiene le informazioni dell'utente autenticato
     * e l'elenco delle funzionalità disponibili.
     *
     * @return pannello centrale della dashboard
     */
    private JPanel creaCentroPanel() {
        JPanel centroPanel = new JPanel();
        centroPanel.setLayout(new BoxLayout(centroPanel, BoxLayout.Y_AXIS));

        JPanel infoPanel = creaInfoPanel();
        JPanel funzionalitaPanel = creaFunzionalitaPanel();

        centroPanel.add(infoPanel);
        centroPanel.add(Box.createVerticalStrut(25));
        centroPanel.add(new JSeparator());
        centroPanel.add(Box.createVerticalStrut(20));
        centroPanel.add(funzionalitaPanel);

        return centroPanel;
    }

    /**
     * Crea il pannello informativo dell'utente autenticato.
     *
     * Il pannello mostra solo dati semplici di sessione già disponibili
     * nella Boundary.
     *
     * @return pannello informativo della dashboard
     */
    private JPanel creaInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        dashboardLabel.setFont(new Font("Arial", Font.BOLD, 18));
        benvenutoLabel.setFont(new Font("Arial", Font.BOLD, 15));
        ruoloLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        descrizioneLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        dashboardLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        benvenutoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        ruoloLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descrizioneLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(dashboardLabel);
        infoPanel.add(Box.createVerticalStrut(12));
        infoPanel.add(benvenutoLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(ruoloLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(descrizioneLabel);

        return infoPanel;
    }

    /**
     * Crea il pannello delle funzionalità disponibili.
     *
     * Tutti i pulsanti vengono creati, ma la loro visibilità viene regolata
     * in base al ruolo dell'utente autenticato.
     *
     * @return pannello delle funzionalità
     */
    private JPanel creaFunzionalitaPanel() {
        JPanel funzionalitaPanel = new JPanel();
        funzionalitaPanel.setLayout(new BoxLayout(funzionalitaPanel, BoxLayout.Y_AXIS));

        funzionalitaLabel.setFont(new Font("Arial", Font.BOLD, 16));
        funzionalitaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        funzionalitaPanel.add(funzionalitaLabel);
        funzionalitaPanel.add(Box.createVerticalStrut(15));

        creaSegnalazionePanel = creaPannelloBottone(creaSegnalazioneButton);
        consultaInviatePanel = creaPannelloBottone(consultaInviateButton);
        consultaRicevutePanel = creaPannelloBottone(consultaRicevuteButton);
        aggiornaStatoPanel = creaPannelloBottone(aggiornaStatoButton);
        monitoraAttivitaPanel = creaPannelloBottone(monitoraAttivitaButton);
        logoutPanel = creaPannelloBottone(logoutButton);

        funzionalitaPanel.add(creaSegnalazionePanel);
        funzionalitaPanel.add(consultaInviatePanel);
        funzionalitaPanel.add(consultaRicevutePanel);
        funzionalitaPanel.add(aggiornaStatoPanel);
        funzionalitaPanel.add(monitoraAttivitaPanel);
        funzionalitaPanel.add(Box.createVerticalStrut(10));
        funzionalitaPanel.add(logoutPanel);

        return funzionalitaPanel;
    }

    /**
     * Crea un pannello-riga contenente un pulsante.
     *
     * Il pannello permette di nascondere insieme pulsante e spazio verticale
     * associato.
     *
     * @param button pulsante da inserire nel pannello
     * @return pannello contenente il pulsante
     */
    private JPanel creaPannelloBottone(JButton button) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(290, 45));
        button.setMaximumSize(new Dimension(290, 45));
        button.setMinimumSize(new Dimension(290, 45));

        buttonPanel.add(button);
        buttonPanel.add(Box.createVerticalStrut(10));

        return buttonPanel;
    }

    /**
     * Crea il footer della dashboard.
     *
     * Il metodo costruisce solo la parte grafica inferiore della Boundary.
     *
     * @return pannello footer
     */
    private JPanel creaFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        footerLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        footerPanel.add(footerLabel);

        return footerPanel;
    }

    /**
     * Configura la dashboard in base ai dati semplici di sessione.
     *
     * Il metodo non usa instanceof su classi Entity e non accede al package
     * Entity. Il ruolo viene letto come valore semplice già determinato nel
     * flusso di accesso.
     */
    private void configuraDashboard() {

        if (idUtenteAutenticato == null || isVuoto(ruoloUtenteAutenticato)) {
            mostraErroreUtenteNonValido();
            disabilitaFunzionalita();
            return;
        }

        benvenutoLabel.setText(
                "Benvenuto/a, "
                        + valoreTestuale(nomeUtenteAutenticato)
                        + " "
                        + valoreTestuale(cognomeUtenteAutenticato)
        );

        if (isRuoloCittadino()) {
            configuraDashboardCittadino();
            return;
        }

        if (isRuoloOperatoreComunale()) {
            configuraDashboardOperatore();
            return;
        }

        mostraErroreUtenteNonValido();
        disabilitaFunzionalita();
    }

    /**
     * Configura le funzionalità disponibili per il cittadino.
     *
     * La Boundary mostra solo i pulsanti coerenti con il ruolo.
     */
    private void configuraDashboardCittadino() {
        ruoloLabel.setText("Ruolo: Cittadino");
        descrizioneLabel.setText(
                "Puoi creare nuove segnalazioni e consultare quelle già inviate."
        );

        creaSegnalazionePanel.setVisible(true);
        consultaInviatePanel.setVisible(true);

        consultaRicevutePanel.setVisible(false);
        aggiornaStatoPanel.setVisible(false);
        monitoraAttivitaPanel.setVisible(false);

        logoutPanel.setVisible(true);
    }

    /**
     * Configura le funzionalità disponibili per l'operatore comunale.
     *
     * La Boundary mostra solo i pulsanti coerenti con il ruolo.
     */
    private void configuraDashboardOperatore() {
        ruoloLabel.setText("Ruolo: Operatore Comunale");
        descrizioneLabel.setText(
                "Puoi consultare, gestire e monitorare le segnalazioni comunali."
        );

        creaSegnalazionePanel.setVisible(false);
        consultaInviatePanel.setVisible(false);

        consultaRicevutePanel.setVisible(true);
        aggiornaStatoPanel.setVisible(true);
        monitoraAttivitaPanel.setVisible(true);

        logoutPanel.setVisible(true);
    }

    /**
     * Disabilita le funzionalità operative in caso di sessione non valida.
     *
     * Il metodo modifica solo la visibilità dei componenti Swing.
     */
    private void disabilitaFunzionalita() {
        creaSegnalazionePanel.setVisible(false);
        consultaInviatePanel.setVisible(false);
        consultaRicevutePanel.setVisible(false);
        aggiornaStatoPanel.setVisible(false);
        monitoraAttivitaPanel.setVisible(false);
        logoutPanel.setVisible(true);
    }

    /**
     * Metodo pubblico previsto dal BCED.
     *
     * Rappresenta la scelta della funzionalità da parte dell'utente autenticato.
     * Nel codice collega i pulsanti della dashboard alle relative Boundary.
     *
     * La dashboard non esegue direttamente i casi d'uso applicativi: apre
     * solo la GUI specifica, che a sua volta delegherà al relativo controller.
     */
    public void selezionaFunzionalita() {
        creaSegnalazioneButton.addActionListener(new ActionListener() {

            /**
             * Apre la GUI di creazione segnalazione.
             *
             * @param e evento generato dalla pressione del pulsante
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                apriGUICreazioneSegnalazione();
            }
        });

        consultaInviateButton.addActionListener(new ActionListener() {

            /**
             * Apre la GUI di consultazione delle segnalazioni inviate.
             *
             * @param e evento generato dalla pressione del pulsante
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                apriGUIConsultazioneInviate();
            }
        });

        consultaRicevuteButton.addActionListener(new ActionListener() {

            /**
             * Apre la GUI di consultazione delle segnalazioni ricevute.
             *
             * @param e evento generato dalla pressione del pulsante
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                apriGUIConsultazioneRicevute();
            }
        });

        aggiornaStatoButton.addActionListener(new ActionListener() {

            /**
             * Apre la GUI di aggiornamento dello stato della segnalazione.
             *
             * @param e evento generato dalla pressione del pulsante
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                apriGUIAggiornamentoStato();
            }
        });

        monitoraAttivitaButton.addActionListener(new ActionListener() {

            /**
             * Apre la GUI di monitoraggio dell'andamento delle attività.
             *
             * @param e evento generato dalla pressione del pulsante
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                apriGUIMonitoraggioAttivita();
            }
        });

        logoutButton.addActionListener(new ActionListener() {

            /**
             * Riporta l'utente alla Home Page.
             *
             * @param e evento generato dalla pressione del pulsante
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                tornaAllaHomePage();
            }
        });
    }

    /**
     * Apre la GUI di creazione segnalazione.
     *
     * Il metodo passa alla GUI successiva solo i dati semplici di sessione,
     * evitando il passaggio di istanze Entity tra Boundary.
     *
     * La GUI di creazione userà l'idUtente per inoltrare la richiesta al
     * Facade Controller e conserverà la Map per poter tornare alla dashboard.
     */
    private void apriGUICreazioneSegnalazione() {

        if (datiSessioneUtente == null || idUtenteAutenticato == null) {
            segnalaErroreDashboard(
                    "Sessione utente non valida.\n"
                            + "Impossibile aprire la creazione della segnalazione."
            );
            return;
        }

        if (!isRuoloCittadino()) {
            segnalaErroreDashboard(
                    "Funzionalità non disponibile.\n"
                            + "La creazione di una segnalazione è riservata al cittadino."
            );
            return;
        }

        GUICreazioneSegnalazione guiCreazione = new GUICreazioneSegnalazione(datiSessioneUtente);

        apriFinestraEChiudiDashboard(
                "Crea Nuova Segnalazione",
                guiCreazione.getContentPanel()
        );
    }

    /**
     * Apre la GUI per la consultazione delle segnalazioni inviate dal cittadino.
     *
     * La dashboard passa alla Boundary specializzata solo i dati semplici
     * di sessione dell'utente autenticato, evitando il passaggio di istanze Entity.
     * La GUI di consultazione userà l'idUtenteAutenticato contenuto nella Map
     * per inoltrare la richiesta al Facade Controller.
     */
    private void apriGUIConsultazioneInviate() {

        if (datiSessioneUtente == null || idUtenteAutenticato == null) {
            segnalaErroreDashboard(
                    "Sessione utente non valida.\n"
                            + "Impossibile aprire la consultazione delle segnalazioni inviate."
            );
            return;
        }

        if (!isRuoloCittadino()) {
            segnalaErroreDashboard(
                    "Funzionalità non disponibile.\n"
                            + "La consultazione delle segnalazioni inviate è riservata al cittadino."
            );
            return;
        }

        GUIConsultazioneSegnalazioniInviate guiConsultazioneInviate = new GUIConsultazioneSegnalazioniInviate(datiSessioneUtente);

        apriFinestraEChiudiDashboard(
                "Segnalazioni Inviate",
                guiConsultazioneInviate.getContentPanel()
        );
    }

    /**
     * Apre la GUI per la consultazione ordinaria delle segnalazioni ricevute
     * dall'Operatore Comunale.

     * Il metodo verifica preliminarmente che la sessione utente sia valida e che
     * l'utente autenticato abbia ruolo Operatore Comunale. In caso contrario,
     * la Boundary segnala l'errore senza inoltrare richieste ai livelli successivi.

     * La dashboard passa alla GUI di consultazione solo dati semplici di sessione,
     * evitando il passaggio di istanze Entity tra Boundary. Il parametro booleano
     * impostato a false indica che la GUI viene aperta in modalità di consultazione
     * normale e non come fase preliminare alla funzionalità di aggiornamento stato.
     */

    private void apriGUIConsultazioneRicevute() {

        if (datiSessioneUtente == null || idUtenteAutenticato == null) {
            segnalaErroreDashboard(
                    "Sessione utente non valida.\n"
                            + "Impossibile aprire la consultazione delle segnalazioni ricevute."
            );
            return;
        }

        if (!isRuoloOperatoreComunale()) {
            segnalaErroreDashboard(
                    "Funzionalità non disponibile.\n"
                            + "La consultazione delle segnalazioni ricevute è riservata all'operatore comunale."
            );
            return;
        }

        GUIConsultazioneSegnalazioniRicevute guiConsultazioneRicevute =
                new GUIConsultazioneSegnalazioniRicevute(datiSessioneUtente, false);

        apriFinestraEChiudiDashboard(
                "Segnalazioni Ricevute",
                guiConsultazioneRicevute.getContentPanel()
        );
    }

    /**
     * Apre la GUI per il monitoraggio dell'andamento delle attività.
     *
     * La dashboard passa alla Boundary specializzata solo i dati semplici
     * di sessione dell'utente autenticato, evitando il passaggio di istanze Entity.
     * La GUI di monitoraggio userà l'idUtenteAutenticato contenuto nella Map
     * per inoltrare la richiesta al Facade Controller.
     */
    private void apriGUIMonitoraggioAttivita() {

        if (datiSessioneUtente == null || idUtenteAutenticato == null) {
            segnalaErroreDashboard(
                    "Sessione utente non valida.\n"
                            + "Impossibile aprire il monitoraggio delle attività."
            );
            return;
        }

        if (!isRuoloOperatoreComunale()) {
            segnalaErroreDashboard(
                    "Funzionalità non disponibile.\n"
                            + "Il monitoraggio delle attività è riservato all'operatore comunale."
            );
            return;
        }

        GUIMonitoraggioAttivita guiMonitoraggio =
                new GUIMonitoraggioAttivita(datiSessioneUtente);

        apriFinestraEChiudiDashboard(
                "Monitoraggio Attività",
                guiMonitoraggio.getContentPanel()
        );
    }

    /**
     * Avvia la funzionalità di aggiornamento dello stato di una segnalazione ricevuta.
     *
     * La dashboard non apre direttamente la GUI di aggiornamento stato, poiché
     * prima l'Operatore Comunale deve selezionare una segnalazione tra quelle ricevute.
     * Per questo motivo viene aperta la GUI di consultazione delle segnalazioni ricevute
     * in modalità selezione per aggiornamento.
     *
     * Il valore booleano passato alla GUI di consultazione indica che, dopo la selezione
     * della segnalazione, dovrà essere aperta la GUI di aggiornamento stato.
     */
    private void apriGUIAggiornamentoStato() {

        if (datiSessioneUtente == null || idUtenteAutenticato == null) {
            segnalaErroreDashboard(
                    "Sessione utente non valida.\n"
                            + "Impossibile avviare l'aggiornamento dello stato."
            );
            return;
        }

        if (!isRuoloOperatoreComunale()) {
            segnalaErroreDashboard(
                    "Funzionalità non disponibile.\n"
                            + "L'aggiornamento dello stato è riservato all'operatore comunale."
            );
            return;
        }

        GUIConsultazioneSegnalazioniRicevute guiConsultazioneRicevute =
                new GUIConsultazioneSegnalazioniRicevute(datiSessioneUtente, true);

        apriFinestraEChiudiDashboard(
                "Seleziona Segnalazione da Aggiornare",
                guiConsultazioneRicevute.getContentPanel()
        );
    }

    /**
     * Apre una nuova finestra e chiude la dashboard corrente.
     *
     * Metodo privato di supporto tecnico della Boundary. Non rappresenta una
     * responsabilità di dominio.
     *
     * @param titoloFinestra titolo della finestra da aprire
     * @param pannelloDestinazione pannello della Boundary da mostrare
     */
    private void apriFinestraEChiudiDashboard(String titoloFinestra, JPanel pannelloDestinazione) {

        JFrame nuovaFinestra = new JFrame(titoloFinestra);
        nuovaFinestra.setContentPane(pannelloDestinazione);
        nuovaFinestra.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        nuovaFinestra.setResizable(false);
        nuovaFinestra.pack();
        nuovaFinestra.setLocationRelativeTo(null);
        nuovaFinestra.setVisible(true);

        chiudiFinestraCorrente();
    }

    /**
     * Torna alla Home Page e chiude la dashboard.
     *
     * Il metodo è un supporto tecnico di navigazione e non esegue operazioni
     * di dominio.
     */
    private void tornaAllaHomePage() {
        GUIHomePage guiHomePage = new GUIHomePage();

        JFrame frameHome = new JFrame("Sistema Comunale Gestione Segnalazioni");
        frameHome.setContentPane(guiHomePage.getContentPanel());
        frameHome.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameHome.setResizable(false);
        frameHome.pack();
        frameHome.setLocationRelativeTo(null);
        frameHome.setVisible(true);

        chiudiFinestraCorrente();
    }

    /**
     * Chiude la finestra Swing associata alla dashboard corrente.
     *
     * Metodo privato di supporto tecnico della Boundary.
     */
    private void chiudiFinestraCorrente() {
        Window finestraCorrente = SwingUtilities.getWindowAncestor(contentPanel);

        if (finestraCorrente != null) {
            finestraCorrente.dispose();
        }
    }

    /**
     * Comunica all'utente che la dashboard non può essere caricata.
     *
     * Il metodo mostra solo un messaggio grafico e non accede al dominio.
     */
    private void mostraErroreUtenteNonValido() {
        segnalaErroreDashboard(
                "Impossibile caricare la dashboard: utente non valido."
        );
    }

    /**
     * Comunica un errore relativo alla dashboard.
     *
     * Il metodo appartiene alla Boundary e ha solo responsabilità grafica.
     * Non accede al Database, non manipola Entity e non contiene logica di dominio.
     *
     * @param messaggio messaggio da mostrare all'utente
     */
    public void segnalaErroreDashboard(String messaggio) {

        String messaggioDaMostrare = valoreTestuale(messaggio);

        if (isVuoto(messaggioDaMostrare)) {
            messaggioDaMostrare = "Impossibile completare l'operazione richiesta dalla dashboard.";
        }

        JOptionPane.showMessageDialog(
                contentPanel,
                messaggioDaMostrare,
                "Errore Dashboard",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Verifica se il ruolo di sessione corrisponde al cittadino.
     *
     * Il controllo usa solo una stringa di sessione e non classi Entity.
     *
     * @return true se il ruolo è Cittadino, false altrimenti
     */
    private boolean isRuoloCittadino() {
        return RUOLO_CITTADINO.equalsIgnoreCase(ruoloUtenteAutenticato);
    }

    /**
     * Verifica se il ruolo di sessione corrisponde all'operatore comunale.
     *
     * Il controllo usa solo una stringa di sessione e non classi Entity.
     *
     * @return true se il ruolo è Operatore Comunale, false altrimenti
     */
    private boolean isRuoloOperatoreComunale() {
        return RUOLO_OPERATORE_COMUNALE.equalsIgnoreCase(ruoloUtenteAutenticato)
                || "OperatoreComunale".equalsIgnoreCase(ruoloUtenteAutenticato);
    }

    /**
     * Converte un valore generico in una stringa normalizzata.
     *
     * Metodo privato di supporto della Boundary.
     *
     * @param valore valore da convertire
     * @return stringa senza spazi iniziali/finali, oppure stringa vuota se nullo
     */
    private String valoreTestuale(Object valore) {

        if (valore == null) {
            return "";
        }

        return valore.toString().trim();
    }

    /**
     * Converte un valore generico in Long.
     *
     * Metodo privato di supporto usato per leggere l'idUtente dai dati
     * semplici di sessione.
     *
     * @param valore valore da convertire
     * @return Long corrispondente, oppure null se il valore non è valido
     */
    private Long estraiLong(Object valore) {

        if (valore == null) {
            return null;
        }

        if (valore instanceof Long) {
            return (Long) valore;
        }

        if (valore instanceof Number) {
            return ((Number) valore).longValue();
        }

        try {
            return Long.valueOf(valore.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Verifica se una stringa è nulla o vuota.
     *
     * Metodo privato di supporto per la validazione dei dati di sessione.
     *
     * @param valore stringa da verificare
     * @return true se la stringa è nulla o vuota, false altrimenti
     */
    private boolean isVuoto(String valore) {
        return valore == null || valore.trim().isEmpty();
    }

    /**
     * Restituisce il pannello principale della dashboard.
     *
     * Il metodo permette alla finestra chiamante di mostrare questa Boundary.
     * Non espone Entity o oggetti contenuti.
     *
     * @return pannello principale della dashboard
     */
    public JPanel getContentPanel() {
        return contentPanel;
    }
}

