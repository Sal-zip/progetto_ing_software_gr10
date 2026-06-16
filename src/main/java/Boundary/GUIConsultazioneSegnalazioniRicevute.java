package Boundary;

import Control.ControllerServiziSistema;
import Entity.CategoriaSegnalazione;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Boundary per la consultazione delle segnalazioni ricevute dall'operatore comunale.
 *
 * La GUI non accede direttamente alla persistenza, non manipola Entity e non
 * mantiene istanze Entity dell'utente autenticato.
 *
 * Per mantenere chiusa l'architettura BCED, la Boundary riceve solo dati
 * semplici di sessione, estrae l'id dell'utente autenticato e comunica
 * esclusivamente con ControllerServiziSistema.
 *
 * È coerente con il modello BCED perché:
 * - la Boundary comunica solo con il Facade Controller;
 * - i dati visualizzati arrivano già mappati come Map<String, Object>;
 * - la logica applicativa resta nei controller;
 * - il recupero delle Entity resta nei Facade Entity;
 * - lo stato viene trattato come valore testuale coerente con lo State Pattern.
 */
public class GUIConsultazioneSegnalazioniRicevute {

    private JPanel contentPanel;

    private JLabel logoLabel;
    private JLabel titoloSistemaLabel;
    private JLabel titoloPaginaLabel;
    private JLabel descrizioneLabel;
    private JLabel esitoLabel;

    private JComboBox<Object> boxCategoriaFiltro;
    private JComboBox<Object> boxStatoFiltro;
    private JTextField txtAreaComunaleFiltro;

    private JButton applicaFiltriButton;
    private JButton rimuoviFiltriButton;

    private JTable tabellaSegnalazioni;
    private JScrollPane scrollPaneSegnalazioni;

    private JButton visualizzaDettaglioButton;
    private JButton tornaDashboardButton;

    private ControllerServiziSistema controllerServiziSistema;

    /**
     * Indica se la GUI è stata aperta come fase preliminare
     * alla funzionalità di aggiornamento dello stato.
     *
     * Se true, la selezione di una segnalazione ricevuta apre la
     * GUIAggiornamentoStatoSegnalazione.
     * Se false, la selezione mantiene il comportamento ordinario
     * di visualizzazione del dettaglio.
     */
    private boolean modalitaAggiornamentoStato;

    /**
     * Dati semplici della sessione dell'utente autenticato.
     *
     * La Map viene conservata solo per riaprire la dashboard o altre Boundary
     * senza passare istanze Entity.
     */
    private Map<String, Object> datiSessioneUtente;

    /**
     * Identificativo dell'utente autenticato estratto dalla Map di sessione.
     */
    private Long idUtenteAutenticato;

    /**
     * Costruisce la GUI di consultazione delle segnalazioni ricevute
     * in modalità ordinaria.
     *
     * Questo costruttore mantiene il comportamento standard della GUI:
     * l'operatore consulta l'elenco delle segnalazioni ricevute e può
     * visualizzarne il dettaglio.
     *
     * @param datiSessioneUtente mappa contenente i dati semplici dell'utente autenticato
     */
    public GUIConsultazioneSegnalazioniRicevute(Map<String, Object> datiSessioneUtente) {
        this(datiSessioneUtente, false);
    }

    /**
     * Costruisce la GUI di consultazione delle segnalazioni ricevute.
     *
     * La Boundary riceve solo dati semplici di sessione e non mantiene
     * riferimenti a Entity. Il parametro modalitaAggiornamentoStato consente
     * di riutilizzare la stessa GUI sia per la consultazione ordinaria, sia come
     * fase preliminare alla selezione della segnalazione da aggiornare.
     *
     * @param datiSessioneUtente mappa contenente almeno idUtente, nome, cognome e ruolo
     * @param modalitaAggiornamentoStato true se la GUI è aperta per selezionare
     *                                   una segnalazione da aggiornare, false
     *                                   per la consultazione ordinaria
     */
    public GUIConsultazioneSegnalazioniRicevute(
            Map<String, Object> datiSessioneUtente,
            boolean modalitaAggiornamentoStato
    ) {
        this.datiSessioneUtente = datiSessioneUtente;
        this.idUtenteAutenticato = estraiIdUtenteDaSessione(datiSessioneUtente);
        this.modalitaAggiornamentoStato = modalitaAggiornamentoStato;

        this.controllerServiziSistema = new ControllerServiziSistema();

        inizializzaComponenti();
        configuraLayout();
        configuraTabella();
        configuraAzioni();
        configuraModalitaApertura();

        richiediConsultazioneSegnalazioniRicevute();
    }

    /**
     * Restituisce il pannello principale della GUI.
     *
     * @return pannello principale
     */
    public JPanel getContentPanel() {
        return contentPanel;
    }

    /**
     * Metodo pubblico previsto dalla Boundary specializzata.
     *
     * Avvia la consultazione delle segnalazioni ricevute senza filtri.
     */
    public void richiediConsultazioneSegnalazioniRicevute() {
        inviaRichiestaConsultazione();
    }

    /**
     * Invia la richiesta di consultazione senza filtri al Facade Controller.
     *
     * La Boundary non recupera Entity e non accede al Database.
     */
    public void inviaRichiestaConsultazione() {

        if (idUtenteAutenticato == null) {
            segnalaErroreConsultazione(
                    "Sessione utente non valida. Effettua nuovamente l'accesso."
            );
            return;
        }

        List<Map<String, Object>> elenco =
                controllerServiziSistema.consultaSegnalazioniRicevute(
                        idUtenteAutenticato,
                        null
                );

        if (elenco == null) {
            segnalaErroreConsultazione(
                    "Impossibile caricare le segnalazioni ricevute."
            );
            return;
        }

        comunicaElencoSegnalazioni(elenco);
    }

    /**
     * Acquisisce i criteri di filtro inseriti dall'operatore.
     *
     * Le chiavi della Map sono coerenti con i campi usati dal Facade Entity:
     * - categoria;
     * - nomeStatoCorrente;
     * - posizione.areaComunale.
     *
     * Lo stato è trattato come String perché, con lo State Pattern, lo stato
     * corrente della segnalazione viene persistito come nome testuale.
     *
     * @return mappa dei criteri di filtro
     */
    public Map<String, Object> acquisisciCriteriFiltro() {
        Map<String, Object> criteriFiltro = new HashMap<>();

        Object categoriaSelezionata = boxCategoriaFiltro.getSelectedItem();

        if (categoriaSelezionata instanceof CategoriaSegnalazione) {
            criteriFiltro.put("categoria", categoriaSelezionata);
        }

        Object statoSelezionato = boxStatoFiltro.getSelectedItem();

        if (statoSelezionato instanceof String) {
            String stato = valoreTestuale(statoSelezionato);

            if (!isVuoto(stato) && !"Tutti gli stati".equals(stato)) {
                criteriFiltro.put("nomeStatoCorrente", stato);
            }
        }

        String areaComunale = valoreTestuale(txtAreaComunaleFiltro.getText());

        if (!isVuoto(areaComunale)) {
            criteriFiltro.put("posizione.areaComunale", areaComunale);
        }

        return criteriFiltro;
    }

    /**
     * Invia i criteri di filtro al Facade Controller.
     *
     * @param criteriFiltro criteri di filtro acquisiti dalla Boundary
     */
    public void inviaCriteriFiltro(Map<String, Object> criteriFiltro) {

        if (idUtenteAutenticato == null) {
            segnalaErroreConsultazione(
                    "Sessione utente non valida. Effettua nuovamente l'accesso."
            );
            return;
        }

        List<Map<String, Object>> elenco =
                controllerServiziSistema.consultaSegnalazioniRicevute(
                        idUtenteAutenticato,
                        criteriFiltro
                );

        if (elenco == null) {
            segnalaErroreConsultazione(
                    "Impossibile applicare i filtri selezionati alle segnalazioni ricevute."
            );
            return;
        }

        comunicaElencoSegnalazioni(elenco);
    }

    /**
     * Mostra l'elenco delle segnalazioni ricevute nella tabella.
     *
     * @param elenco lista di mappe contenenti dati semplici delle segnalazioni
     */
    public void comunicaElencoSegnalazioni(List<Map<String, Object>> elenco) {

        DefaultTableModel modello =
                (DefaultTableModel) tabellaSegnalazioni.getModel();

        modello.setRowCount(0);

        if (elenco == null || elenco.isEmpty()) {
            esitoLabel.setText("Nessuna segnalazione ricevuta trovata.");
            return;
        }

        for (Map<String, Object> segnalazione : elenco) {
            modello.addRow(new Object[]{
                    segnalazione.get("idSegnalazione"),
                    segnalazione.get("titolo"),
                    segnalazione.get("categoria"),
                    recuperaStatoDaMappa(segnalazione),
                    formattaData(segnalazione.get("dataSegnalazione")),
                    costruisciPosizioneSintetica(segnalazione),
                    segnalazione.get("areaComunale")
            });
        }

        esitoLabel.setText("Segnalazioni ricevute caricate correttamente.");
    }

    /**
     * Registra graficamente la selezione della segnalazione.
     *
     * @param idSegnalazione identificativo della segnalazione selezionata
     */
    public void selezionaSegnalazione(Long idSegnalazione) {

        if (idSegnalazione == null) {
            esitoLabel.setText("Nessuna segnalazione selezionata.");
            return;
        }

        esitoLabel.setText("Segnalazione selezionata: " + idSegnalazione);
    }

    /**
     * Richiede il dettaglio della segnalazione selezionata.
     *
     * La GUI invia solo idUtenteAutenticato e idSegnalazione al Facade Controller,
     * senza passare istanze Entity tra Boundary e Control.
     *
     * @param idSegnalazione identificativo della segnalazione selezionata
     */
    public void richiediDettaglioSegnalazione(Long idSegnalazione) {

        if (idUtenteAutenticato == null) {
            segnalaErroreConsultazione(
                    "Sessione utente non valida. Effettua nuovamente l'accesso."
            );
            return;
        }

        if (idSegnalazione == null) {
            segnalaErroreConsultazione(
                    "Seleziona una segnalazione dall'elenco prima di visualizzare il dettaglio."
            );
            return;
        }

        Map<String, Object> dettaglio =
                controllerServiziSistema.visualizzaDettagliSegnalazione(
                        idUtenteAutenticato,
                        idSegnalazione
                );

        if (dettaglio == null || dettaglio.isEmpty()) {
            segnalaErroreConsultazione(
                    "Impossibile visualizzare il dettaglio della segnalazione selezionata."
            );
            return;
        }

        comunicaDettaglioSegnalazione(dettaglio);
    }

    /**
     * Comunica il dettaglio della segnalazione selezionata.
     *
     * La GUI visualizza solo i dati semplici già presenti nella Map.
     *
     * @param dettaglio mappa contenente i dati del dettaglio
     */
    public void comunicaDettaglioSegnalazione(Map<String, Object> dettaglio) {

        if (dettaglio == null || dettaglio.isEmpty()) {
            segnalaErroreConsultazione(
                    "Dettaglio della segnalazione non disponibile."
            );
            return;
        }

        StringBuilder testo = new StringBuilder();

        testo.append("ID Segnalazione: ")
                .append(valoreTestuale(dettaglio.get("idSegnalazione")))
                .append("\n");

        testo.append("Titolo: ")
                .append(valoreTestuale(dettaglio.get("titolo")))
                .append("\n");

        testo.append("Descrizione: ")
                .append(valoreTestuale(dettaglio.get("descrizione")))
                .append("\n");

        testo.append("Categoria: ")
                .append(valoreTestuale(dettaglio.get("categoria")))
                .append("\n");

        testo.append("Stato corrente: ")
                .append(valoreTestuale(recuperaStatoDaMappa(dettaglio)))
                .append("\n");

        testo.append("Data segnalazione: ")
                .append(formattaData(dettaglio.get("dataSegnalazione")))
                .append("\n\n");

        testo.append("Posizione\n");

        testo.append("Città: ")
                .append(valoreTestuale(dettaglio.get("citta")))
                .append("\n");

        testo.append("CAP: ")
                .append(valoreTestuale(dettaglio.get("cap")))
                .append("\n");

        testo.append("Strada: ")
                .append(valoreTestuale(dettaglio.get("strada")))
                .append("\n");

        testo.append("Numero civico: ")
                .append(valoreTestuale(dettaglio.get("numeroCivico")))
                .append("\n");

        testo.append("Area comunale: ")
                .append(valoreTestuale(dettaglio.get("areaComunale")))
                .append("\n");

        Object[] opzioni = {
                "Visualizza Evoluzione",
                "Visualizza Immagine",
                "Visualizza Note Interne",
                "Aggiungi Nota Interna",
                "Chiudi"
        };

        int scelta = JOptionPane.showOptionDialog(
                contentPanel,
                testo.toString(),
                "Dettaglio Segnalazione",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                caricaIconaLogoPopup(),
                opzioni,
                opzioni[4]
        );

        if (scelta == 0) {
            visualizzaEvoluzioneDaDettaglio(dettaglio);
        }

        if (scelta == 1) {
            visualizzaImmagineAllegata(dettaglio.get("immagineAllegata"));
        }

        if (scelta == 2) {
            visualizzaNoteInterneDaDettaglio(dettaglio);
        }

        if (scelta == 3) {
            apriGUINotaInternaDaDettaglio(dettaglio);
        }
    }

    /**
     * Comunica all'utente un errore relativo alla consultazione
     * delle segnalazioni ricevute.
     *
     * La Boundary gestisce la comunicazione grafica dell'esito negativo,
     * mentre il livello Control restituisce solo esiti semplici.
     *
     * @param messaggio descrizione dell'errore da mostrare all'utente
     */
    public void segnalaErroreConsultazione(String messaggio) {

        String messaggioDaMostrare = valoreTestuale(messaggio);

        if (isVuoto(messaggioDaMostrare)) {
            messaggioDaMostrare = "Impossibile completare la consultazione delle segnalazioni ricevute.";
        }

        esitoLabel.setText("Errore durante la consultazione delle segnalazioni ricevute.");

        JOptionPane.showMessageDialog(
                contentPanel,
                messaggioDaMostrare,
                "Errore consultazione segnalazioni ricevute",
                JOptionPane.ERROR_MESSAGE,
                caricaIconaLogoPopup()
        );
    }

    /**
     * Inizializza tutti i componenti grafici della GUI.
     */
    private void inizializzaComponenti() {
        contentPanel = new JPanel();

        logoLabel = new JLabel();

        titoloSistemaLabel = new JLabel("Sistema Comunale Gestione Segnalazioni");

        titoloPaginaLabel = new JLabel(
                "<html>Segnalazioni <span style='color:#1E88E5;'>ricevute</span></html>"
        );

        descrizioneLabel = new JLabel(
                "Consulta e filtra l'elenco delle segnalazioni ricevute."
        );

        esitoLabel = new JLabel("In attesa di caricamento...");

        boxCategoriaFiltro = new JComboBox<>();
        boxStatoFiltro = new JComboBox<>();
        txtAreaComunaleFiltro = new JTextField();

        applicaFiltriButton = new JButton("Applica Filtro");
        rimuoviFiltriButton = new JButton("Rimuovi Filtro");

        tabellaSegnalazioni = new JTable();
        scrollPaneSegnalazioni = new JScrollPane(tabellaSegnalazioni);

        visualizzaDettaglioButton = new JButton("Visualizza Dettaglio");
        tornaDashboardButton = new JButton("Torna alla Dashboard");

        configuraComboBoxFiltri();
        caricaLogo();
    }

    /**
     * Configura i valori disponibili nei filtri.
     */
    private void configuraComboBoxFiltri() {
        boxCategoriaFiltro.addItem("Tutte le categorie");

        for (CategoriaSegnalazione categoria : CategoriaSegnalazione.values()) {
            boxCategoriaFiltro.addItem(categoria);
        }

        boxStatoFiltro.addItem("Tutti gli stati");
        boxStatoFiltro.addItem("inviata");
        boxStatoFiltro.addItem("presa_in_carico");
        boxStatoFiltro.addItem("in_lavorazione");
        boxStatoFiltro.addItem("risolta");
    }

    /**
     * Carica il logo dalle resources del progetto.
     *
     * Percorso atteso: src/main/resources/images/logo.png
     */
    private void caricaLogo() {
        java.net.URL urlLogo = getClass().getResource("/images/logo.png");

        if (urlLogo == null) {
            System.err.println("Logo non trovato: /images/logo.png");
            logoLabel.setText("");
            return;
        }

        ImageIcon logo = new ImageIcon(urlLogo);
        Image immagine = logo.getImage();

        Image immagineRidimensionata = immagine.getScaledInstance(
                75,
                75,
                Image.SCALE_SMOOTH
        );

        logoLabel.setIcon(new ImageIcon(immagineRidimensionata));
        logoLabel.setText("");
    }

    /**
     * Carica il logo del progetto da usare nei popup.
     *
     * @return icona ridimensionata oppure null
     */
    private ImageIcon caricaIconaLogoPopup() {
        java.net.URL urlLogo = getClass().getResource("/images/logo.png");

        if (urlLogo == null) {
            System.err.println("Logo non trovato: /images/logo.png");
            return null;
        }

        ImageIcon logo = new ImageIcon(urlLogo);
        Image immagine = logo.getImage();

        Image immagineRidimensionata = immagine.getScaledInstance(
                75,
                75,
                Image.SCALE_SMOOTH
        );

        return new ImageIcon(immagineRidimensionata);
    }

    /**
     * Configura la struttura generale della schermata.
     */
    private void configuraLayout() {
        contentPanel.setLayout(new BorderLayout(20, 20));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
        contentPanel.setPreferredSize(new Dimension(950, 620));

        contentPanel.add(creaHeaderPanel(), BorderLayout.NORTH);
        contentPanel.add(creaCentroPanel(), BorderLayout.CENTER);
        contentPanel.add(creaFooterPanel(), BorderLayout.SOUTH);
    }

    /**
     * Crea la parte superiore con titolo e logo.
     *
     * @return pannello header
     */
    private JPanel creaHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JPanel titoloConLogoPanel = new JPanel(new BorderLayout());

        JLabel spazioSinistro = new JLabel();
        spazioSinistro.setPreferredSize(new Dimension(75, 75));

        titoloSistemaLabel.setFont(new Font("Arial", Font.BOLD, 24));
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
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(descrizionePanel);

        return headerPanel;
    }

    /**
     * Crea la parte centrale con filtri, messaggio di esito e tabella
     * delle segnalazioni ricevute.
     *
     * Il metodo costruisce solo componenti grafici interni alla Boundary.
     * La GUI non viene più riutilizzata tramite composizione dalla schermata
     * di aggiornamento stato: in modalità aggiornamento, la selezione della
     * segnalazione avviene tramite il pulsante principale della schermata.
     *
     * @return pannello centrale della GUI
     */
    private JPanel creaCentroPanel() {
        JPanel centroPanel = new JPanel(new BorderLayout(10, 10));

        centroPanel.add(creaFiltroPanel(), BorderLayout.NORTH);
        centroPanel.add(scrollPaneSegnalazioni, BorderLayout.CENTER);

        return centroPanel;
    }

    /**
     * Crea il pannello dei filtri per l'operatore comunale.
     *
     * @return pannello filtri
     */
    private JPanel creaFiltroPanel() {
        JPanel contenitoreFiltri = new JPanel();
        contenitoreFiltri.setLayout(new BoxLayout(contenitoreFiltri, BoxLayout.Y_AXIS));

        esitoLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        JPanel esitoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        esitoPanel.add(esitoLabel);

        JPanel filtroPanel = new JPanel(new GridBagLayout());
        GridBagConstraints vincoli = new GridBagConstraints();

        vincoli.insets = new Insets(4, 6, 4, 6);
        vincoli.fill = GridBagConstraints.HORIZONTAL;
        vincoli.gridy = 0;

        vincoli.gridx = 0;
        vincoli.weightx = 0.22;
        filtroPanel.add(new JLabel("Categoria"), vincoli);

        vincoli.gridx = 1;
        vincoli.weightx = 0.22;
        filtroPanel.add(new JLabel("Stato"), vincoli);

        vincoli.gridx = 2;
        vincoli.weightx = 0.28;
        filtroPanel.add(new JLabel("Area comunale"), vincoli);

        vincoli.gridx = 3;
        vincoli.weightx = 0.14;
        filtroPanel.add(new JLabel(""), vincoli);

        vincoli.gridx = 4;
        vincoli.weightx = 0.14;
        filtroPanel.add(new JLabel(""), vincoli);

        vincoli.gridy = 1;

        vincoli.gridx = 0;
        vincoli.weightx = 0.22;
        boxCategoriaFiltro.setPreferredSize(new Dimension(220, 32));
        filtroPanel.add(boxCategoriaFiltro, vincoli);

        vincoli.gridx = 1;
        vincoli.weightx = 0.22;
        boxStatoFiltro.setPreferredSize(new Dimension(220, 32));
        filtroPanel.add(boxStatoFiltro, vincoli);

        vincoli.gridx = 2;
        vincoli.weightx = 0.28;
        txtAreaComunaleFiltro.setPreferredSize(new Dimension(240, 32));
        filtroPanel.add(txtAreaComunaleFiltro, vincoli);

        vincoli.gridx = 3;
        vincoli.weightx = 0.14;
        applicaFiltriButton.setPreferredSize(new Dimension(150, 32));
        filtroPanel.add(applicaFiltriButton, vincoli);

        vincoli.gridx = 4;
        vincoli.weightx = 0.14;
        rimuoviFiltriButton.setPreferredSize(new Dimension(150, 32));
        filtroPanel.add(rimuoviFiltriButton, vincoli);

        contenitoreFiltri.add(esitoPanel);
        contenitoreFiltri.add(Box.createVerticalStrut(6));
        contenitoreFiltri.add(filtroPanel);

        return contenitoreFiltri;
    }

    /**
     * Crea il pannello inferiore con le azioni principali.
     *
     * @return pannello footer
     */
    private JPanel creaFooterPanel() {
        JPanel footerPanel = new JPanel(new GridLayout(1, 2, 20, 0));

        footerPanel.add(visualizzaDettaglioButton);
        footerPanel.add(tornaDashboardButton);

        return footerPanel;
    }

    /**
     * Configura la JTable.
     */
    private void configuraTabella() {
        DefaultTableModel modelloTabella = new DefaultTableModel(
                new Object[]{
                        "ID",
                        "Titolo",
                        "Categoria",
                        "Stato",
                        "Data",
                        "Posizione",
                        "Area comunale"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabellaSegnalazioni.setModel(modelloTabella);
        tabellaSegnalazioni.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabellaSegnalazioni.getTableHeader().setReorderingAllowed(false);
        tabellaSegnalazioni.setRowHeight(24);
    }

    /**
     * Collega i pulsanti alle rispettive azioni.
     */
    private void configuraAzioni() {
        applicaFiltriButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Map<String, Object> criteriFiltro = acquisisciCriteriFiltro();
                inviaCriteriFiltro(criteriFiltro);
            }
        });

        rimuoviFiltriButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boxCategoriaFiltro.setSelectedIndex(0);
                boxStatoFiltro.setSelectedIndex(0);
                txtAreaComunaleFiltro.setText("");

                richiediConsultazioneSegnalazioniRicevute();
            }
        });

        visualizzaDettaglioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Long idSegnalazione = ottieniIdSegnalazioneSelezionata();

                if (idSegnalazione == null) {
                    segnalaErroreConsultazione(
                            "Seleziona una segnalazione dall'elenco."
                    );
                    return;
                }

                selezionaSegnalazione(idSegnalazione);

                if (modalitaAggiornamentoStato) {
                    apriGUIAggiornamentoStatoDaSelezione(idSegnalazione);
                    return;
                }

                richiediDettaglioSegnalazione(idSegnalazione);
            }
        });

        tornaDashboardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tornaAllaDashboard();
            }
        });
    }

    /**
     * Recupera l'id della segnalazione selezionata nella tabella.
     *
     * Il metodo converte l'indice della riga selezionata nell'indice del modello,
     * così resta corretto anche in caso di ordinamento o filtraggio grafico
     * della JTable.
     *
     * @return id selezionato, oppure null
     */
    private Long ottieniIdSegnalazioneSelezionata() {
        int rigaSelezionata = tabellaSegnalazioni.getSelectedRow();

        if (rigaSelezionata == -1) {
            return null;
        }

        int rigaModello = tabellaSegnalazioni.convertRowIndexToModel(rigaSelezionata);
        Object valoreId = tabellaSegnalazioni.getModel().getValueAt(rigaModello, 0);

        return estraiLong(valoreId);
    }

    /**
     * Torna alla dashboard dell'utente autenticato usando dati semplici di sessione.
     */
    private void tornaAllaDashboard() {

        if (datiSessioneUtente == null || idUtenteAutenticato == null) {
            segnalaErroreConsultazione(
                    "Sessione utente non valida. Impossibile tornare alla dashboard."
            );
            return;
        }

        GUIDashboardUtente guiDashboard = new GUIDashboardUtente(datiSessioneUtente);

        JFrame frameDashboard = new JFrame("Dashboard Utente");
        frameDashboard.setContentPane(guiDashboard.getContentPanel());
        frameDashboard.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameDashboard.setResizable(false);
        frameDashboard.pack();
        frameDashboard.setLocationRelativeTo(null);
        frameDashboard.setVisible(true);

        Window finestraCorrente = SwingUtilities.getWindowAncestor(contentPanel);

        if (finestraCorrente != null) {
            finestraCorrente.dispose();
        }
    }

    /**
     * Apre la GUI per l'inserimento di una nota interna.
     *
     * @param dettaglio mappa contenente il dettaglio della segnalazione
     */
    private void apriGUINotaInternaDaDettaglio(Map<String, Object> dettaglio) {

        if (dettaglio == null || datiSessioneUtente == null) {
            segnalaErroreConsultazione(
                    "Impossibile aprire l'inserimento della nota interna."
            );
            return;
        }

        Long idSegnalazione = estraiLong(dettaglio.get("idSegnalazione"));

        if (idSegnalazione == null) {
            segnalaErroreConsultazione(
                    "Impossibile individuare la segnalazione selezionata."
            );
            return;
        }

        GUINotaInterna guiNotaInterna =
                new GUINotaInterna(
                        datiSessioneUtente,
                        idSegnalazione
                );

        JFrame frameNota = new JFrame("Nota Interna");
        frameNota.setContentPane(guiNotaInterna.getContentPanel());
        frameNota.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameNota.setResizable(false);
        frameNota.pack();
        frameNota.setLocationRelativeTo(null);
        frameNota.setVisible(true);

        Window finestraCorrente = SwingUtilities.getWindowAncestor(contentPanel);

        if (finestraCorrente != null) {
            finestraCorrente.dispose();
        }
    }

    /**
     * Visualizza l'eventuale immagine allegata alla segnalazione.
     *
     * @param immagineAllegata percorso dell'immagine allegata
     */
    private void visualizzaImmagineAllegata(Object immagineAllegata) {

        String percorsoImmagine = valoreTestuale(immagineAllegata);

        if (isVuoto(percorsoImmagine)) {
            JOptionPane.showMessageDialog(
                    contentPanel,
                    "Nessuna immagine allegata disponibile.",
                    "Immagine non disponibile",
                    JOptionPane.INFORMATION_MESSAGE,
                    caricaIconaLogoPopup()
            );
            return;
        }

        ImageIcon immagine = new ImageIcon(percorsoImmagine);

        if (immagine.getIconWidth() <= 0 || immagine.getIconHeight() <= 0) {
            JOptionPane.showMessageDialog(
                    contentPanel,
                    "Impossibile caricare l'immagine allegata.",
                    "Errore immagine",
                    JOptionPane.ERROR_MESSAGE,
                    caricaIconaLogoPopup()
            );
            return;
        }

        Image immagineRidimensionata = immagine.getImage().getScaledInstance(
                420,
                300,
                Image.SCALE_SMOOTH
        );

        JLabel immagineLabel = new JLabel(new ImageIcon(immagineRidimensionata));
        immagineLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JOptionPane.showMessageDialog(
                contentPanel,
                immagineLabel,
                "Immagine allegata",
                JOptionPane.PLAIN_MESSAGE,
                caricaIconaLogoPopup()
        );
    }

    /**
     * Visualizza l'evoluzione degli stati presente nella Map del dettaglio.
     *
     * @param dettaglio mappa del dettaglio
     */
    private void visualizzaEvoluzioneDaDettaglio(Map<String, Object> dettaglio) {

        Object valoreEvoluzione = dettaglio.get("evoluzione");

        if (!(valoreEvoluzione instanceof List<?> listaEvoluzione) || listaEvoluzione.isEmpty()) {
            JOptionPane.showMessageDialog(
                    contentPanel,
                    "Nessuna evoluzione disponibile per questa segnalazione.",
                    "Evoluzione Segnalazione",
                    JOptionPane.INFORMATION_MESSAGE,
                    caricaIconaLogoPopup()
            );
            return;
        }

        StringBuilder messaggio = new StringBuilder();

        for (Object elemento : listaEvoluzione) {
            if (!(elemento instanceof Map<?, ?> aggiornamento)) {
                continue;
            }

            messaggio.append("Aggiornamento ID: ")
                    .append(valoreTestuale(aggiornamento.get("idAggiornamento")))
                    .append("\n");

            messaggio.append("Data: ")
                    .append(valoreTestuale(aggiornamento.get("dataAggiornamento")))
                    .append("\n");

            messaggio.append("Nuovo stato: ")
                    .append(valoreTestuale(aggiornamento.get("nuovoStato")))
                    .append("\n");

            messaggio.append("Operatore: ")
                    .append(valoreTestuale(aggiornamento.get("idOperatore")))
                    .append("\n");

            messaggio.append("-----------------------------\n");
        }

        JOptionPane.showMessageDialog(
                contentPanel,
                messaggio.toString(),
                "Evoluzione Segnalazione",
                JOptionPane.INFORMATION_MESSAGE,
                caricaIconaLogoPopup()
        );
    }

    /**
     * Visualizza le eventuali note interne associate alla segnalazione.
     *
     * @param dettaglio mappa del dettaglio
     */
    private void visualizzaNoteInterneDaDettaglio(Map<String, Object> dettaglio) {

        if (dettaglio == null) {
            return;
        }

        Object noteObject = dettaglio.get("noteInterne");

        if (!(noteObject instanceof List<?> noteInterne) || noteInterne.isEmpty()) {
            JOptionPane.showMessageDialog(
                    contentPanel,
                    "Nessuna nota interna disponibile per la segnalazione selezionata.",
                    "Note Interne",
                    JOptionPane.INFORMATION_MESSAGE,
                    caricaIconaLogoPopup()
            );
            return;
        }

        StringBuilder testo = new StringBuilder();

        testo.append("Note interne associate alla segnalazione ")
                .append(valoreTestuale(dettaglio.get("idSegnalazione")))
                .append("\n\n");

        for (Object elemento : noteInterne) {
            if (!(elemento instanceof Map<?, ?> nota)) {
                continue;
            }

            testo.append("Nota ID: ")
                    .append(valoreTestuale(nota.get("idNota")))
                    .append("\n");

            testo.append("Data inserimento: ")
                    .append(valoreTestuale(nota.get("dataInserimento")))
                    .append("\n");

            testo.append("Operatore: ")
                    .append(valoreTestuale(nota.get("idOperatore")))
                    .append("\n");

            testo.append("Testo nota:\n")
                    .append(valoreTestuale(nota.get("testoNota")))
                    .append("\n");

            testo.append("----------------------------------------\n\n");
        }

        JTextArea areaNote = new JTextArea(testo.toString());
        areaNote.setEditable(false);
        areaNote.setLineWrap(true);
        areaNote.setWrapStyleWord(true);
        areaNote.setFont(new Font("Arial", Font.PLAIN, 14));
        areaNote.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(areaNote);
        scrollPane.setPreferredSize(new Dimension(520, 340));

        JOptionPane.showMessageDialog(
                contentPanel,
                scrollPane,
                "Note Interne",
                JOptionPane.INFORMATION_MESSAGE,
                caricaIconaLogoPopup()
        );
    }

    /**
     * Costruisce una posizione sintetica dai dati semplici della Map.
     *
     * @param segnalazione mappa della segnalazione
     * @return posizione sintetica
     */
    private String costruisciPosizioneSintetica(Map<String, Object> segnalazione) {

        String citta = valoreTestuale(segnalazione.get("citta"));
        String strada = valoreTestuale(segnalazione.get("strada"));
        String numeroCivico = valoreTestuale(segnalazione.get("numeroCivico"));
        String cap = valoreTestuale(segnalazione.get("cap"));

        StringBuilder posizione = new StringBuilder();

        if (!isVuoto(strada)) {
            posizione.append(strada);
        }

        if (!isVuoto(numeroCivico)) {
            if (posizione.length() > 0) {
                posizione.append(", ");
            }
            posizione.append(numeroCivico);
        }

        if (!isVuoto(citta)) {
            if (posizione.length() > 0) {
                posizione.append(" - ");
            }
            posizione.append(citta);
        }

        if (!isVuoto(cap)) {
            if (posizione.length() > 0) {
                posizione.append(" ");
            }
            posizione.append("(").append(cap).append(")");
        }

        if (posizione.length() == 0) {
            return "Posizione non disponibile";
        }

        return posizione.toString();
    }

    /**
     * Recupera lo stato corrente da una Map.
     *
     * @param datiSegnalazione mappa contenente i dati della segnalazione
     * @return stato corrente
     */
    private Object recuperaStatoDaMappa(Map<String, Object> datiSegnalazione) {

        if (datiSegnalazione == null) {
            return "";
        }

        Object statoCorrente = datiSegnalazione.get("statoCorrente");

        if (statoCorrente != null) {
            return statoCorrente;
        }

        return datiSegnalazione.get("nomeStatoCorrente");
    }

    /**
     * Estrae un Long da un valore generico.
     *
     * @param valore valore da convertire
     * @return Long oppure null
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
            return Long.parseLong(valore.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Estrae l'identificativo dell'utente autenticato dai dati semplici di sessione.
     *
     * @param datiSessioneUtente mappa contenente i dati semplici dell'utente autenticato
     * @return id dell'utente autenticato, oppure null se assente o non valido
     */
    private Long estraiIdUtenteDaSessione(Map<String, Object> datiSessioneUtente) {

        if (datiSessioneUtente == null) {
            return null;
        }

        return estraiLong(datiSessioneUtente.get("idUtente"));
    }

    /**
     * Apre la GUI di aggiornamento stato a partire dalla segnalazione ricevuta
     * selezionata dall'Operatore Comunale.
     *
     * La GUI di consultazione non passa l'oggetto Segnalazione, ma solo
     * idSegnalazione come dato semplice. In questo modo la Boundary resta
     * coerente con il Pattern BCED e non espone Entity tra interfacce grafiche.
     *
     * @param idSegnalazione identificativo della segnalazione selezionata
     */
    private void apriGUIAggiornamentoStatoDaSelezione(Long idSegnalazione) {

        if (datiSessioneUtente == null || idUtenteAutenticato == null) {
            segnalaErroreConsultazione(
                    "Sessione utente non valida. Impossibile aprire l'aggiornamento dello stato."
            );
            return;
        }

        if (idSegnalazione == null) {
            segnalaErroreConsultazione(
                    "Impossibile individuare la segnalazione selezionata."
            );
            return;
        }

        GUIAggiornamentoStatoSegnalazione guiAggiornamentoStato =
                new GUIAggiornamentoStatoSegnalazione(
                        datiSessioneUtente,
                        idSegnalazione
                );

        JFrame frameAggiornamento = new JFrame("Aggiornamento Stato Segnalazione");
        frameAggiornamento.setContentPane(guiAggiornamentoStato.getContentPanel());
        frameAggiornamento.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameAggiornamento.setResizable(false);
        frameAggiornamento.pack();
        frameAggiornamento.setLocationRelativeTo(null);
        frameAggiornamento.setVisible(true);

        Window finestraCorrente = SwingUtilities.getWindowAncestor(contentPanel);

        if (finestraCorrente != null) {
            finestraCorrente.dispose();
        }
    }

    /**
     * Converte un valore generico in stringa normalizzata.
     *
     * @param valore valore da convertire
     * @return stringa normalizzata
     */
    private String valoreTestuale(Object valore) {
        if (valore == null) {
            return "";
        }

        return valore.toString().trim();
    }

    /**
     * Converte una data in una rappresentazione testuale leggibile per la GUI.
     *
     * Il metodo gestisce sia valori Date sia eventuali valori già testuali,
     * evitando di mostrare direttamente il formato predefinito di Date.
     *
     * @param valore valore della data ricevuto nella Map
     * @return data formattata oppure stringa vuota se assente
     */

    private String formattaData(Object valore) {

        if (valore == null) {
            return "";
        }

        if (valore instanceof Date) {
            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
            return formato.format((Date) valore);
        }

        return valore.toString().trim();
    }

    /**
     * Configura alcuni elementi grafici in base alla modalità di apertura
     * della GUI.
     *
     * In modalità ordinaria la GUI consente la visualizzazione del dettaglio
     * della segnalazione selezionata. In modalità aggiornamento stato, invece,
     * la GUI viene usata come schermata preliminare per scegliere la segnalazione
     * ricevuta da aggiornare.
     */
    private void configuraModalitaApertura() {

        if (modalitaAggiornamentoStato) {
            titoloPaginaLabel.setText(
                    "<html>Seleziona segnalazione <span style='color:#1E88E5;'>da aggiornare</span></html>"
            );

            descrizioneLabel.setText(
                    "Seleziona una segnalazione ricevuta per procedere con l'aggiornamento dello stato."
            );

            visualizzaDettaglioButton.setText("Aggiorna stato");
            return;
        }

        titoloPaginaLabel.setText(
                "<html>Segnalazioni <span style='color:#1E88E5;'>ricevute</span></html>"
        );

        descrizioneLabel.setText(
                "Consulta e filtra l'elenco delle segnalazioni ricevute."
        );

        visualizzaDettaglioButton.setText("Visualizza Dettaglio");
    }

    /**
     * Verifica se una stringa è nulla o vuota.
     *
     * @param valore stringa da verificare
     * @return true se vuota, false altrimenti
     */
    private boolean isVuoto(String valore) {
        return valore == null || valore.trim().isEmpty();
    }


}