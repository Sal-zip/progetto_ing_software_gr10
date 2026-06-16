package Boundary;

import Control.ControllerServiziSistema;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

/**
 * Boundary per la consultazione delle segnalazioni inviate dal cittadino.
 *
 * La classe non accede direttamente alla persistenza, non gestisce logica
 * di dominio e non mantiene istanze Entity dell'utente autenticato.
 *
 * Per mantenere chiusa l'architettura BCED, riceve solo dati semplici
 * di sessione, estrae l'idUtenteAutenticato e delega le richieste al
 * Facade Controller.
 */
public class GUIConsultazioneSegnalazioniInviate {

    private JPanel contentPanel;

    private JLabel titoloSistemaLabel;
    private JLabel titoloPaginaLabel;
    private JLabel descrizioneLabel;
    private JLabel esitoLabel;
    private JLabel logoLabel;

    private JTable tabellaSegnalazioni;
    private JScrollPane scrollPaneSegnalazioni;

    private JButton visualizzaDettaglioButton;
    private JButton tornaDashboardButton;

    private ControllerServiziSistema controllerServiziSistema;

    /**
     * Dati semplici della sessione dell'utente autenticato.
     *
     * La Boundary conserva questa Map solo per poter tornare alla dashboard
     * senza passare istanze Entity tra Boundary.
     */
    private Map<String, Object> datiSessioneUtente;

    /**
     * Identificativo dell'utente autenticato, estratto dai dati semplici
     * di sessione.
     */
    private Long idUtenteAutenticato;

    /**
     * Costruisce la GUI di consultazione delle segnalazioni inviate.
     *
     * La GUI riceve solo dati semplici di sessione e non un oggetto Entity
     * Utente. L'idUtente viene usato per inoltrare la richiesta al Facade
     * Controller, mentre la Map viene conservata per il ritorno alla dashboard.
     *
     * @param datiSessioneUtente mappa contenente almeno idUtente, nome, cognome e ruolo
     */
    public GUIConsultazioneSegnalazioniInviate(Map<String, Object> datiSessioneUtente) {
        this.datiSessioneUtente = datiSessioneUtente;
        this.idUtenteAutenticato = estraiIdUtenteDaSessione(datiSessioneUtente);

        this.controllerServiziSistema = new ControllerServiziSistema();

        inizializzaComponenti();
        configuraLayout();
        configuraTabella();
        configuraAzioni();

        richiediConsultazioneSegnalazioniInviate();
    }


    /**
     * Restituisce il pannello principale della Boundary.
     *
     * @return pannello principale della GUI
     */
    public JPanel getContentPanel() {
        return contentPanel;
    }

    /**
     * Metodo pubblico previsto dalla Boundary specializzata.
     *
     * Avvia la richiesta di consultazione delle segnalazioni inviate
     * dal cittadino autenticato.
     */
    public void richiediConsultazioneSegnalazioniInviate() {
        inviaRichiestaConsultazione();
    }

    /**
     * Invia la richiesta di consultazione al Facade Controller.
     *
     * La Boundary non recupera direttamente Entity, non accede al Database
     * e non applica logica di dominio.
     */
    public void inviaRichiestaConsultazione() {

        if (idUtenteAutenticato == null) {
            segnalaErroreConsultazioneInviate(
                    "Sessione utente non valida. Effettua nuovamente l'accesso."
            );
            return;
        }

        List<Map<String, Object>> elenco =
                controllerServiziSistema.consultaSegnalazioniInviate(idUtenteAutenticato);

        if (elenco == null) {
            segnalaErroreConsultazioneInviate(
                    "Impossibile caricare le segnalazioni inviate."
            );
            return;
        }

        comunicaElencoSegnalazioni(elenco);
    }

    /**
     * Mostra nella tabella l'elenco delle segnalazioni ricevuto dal livello Control.
     *
     * @param elenco lista di mappe contenenti le segnalazioni inviate
     */
    public void comunicaElencoSegnalazioni(List<Map<String, Object>> elenco) {
        DefaultTableModel modello = (DefaultTableModel) tabellaSegnalazioni.getModel();

        modello.setRowCount(0);

        for (Map<String, Object> segnalazione : elenco) {
            modello.addRow(new Object[]{
                    segnalazione.get("idSegnalazione"),
                    segnalazione.get("titolo"),
                    segnalazione.get("categoria"),
                    recuperaStatoDaMappa(segnalazione),
                    segnalazione.get("dataSegnalazione"),
                    costruisciPosizioneSintetica(segnalazione),
                    segnalazione.get("areaComunale")
            });
        }

        if (elenco.isEmpty()) {
            esitoLabel.setText("Nessuna segnalazione inviata trovata.");
        } else {
            esitoLabel.setText("Segnalazioni inviate caricate correttamente.");
        }
    }

    /**
     * Registra graficamente la selezione di una segnalazione.
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
     * @param idSegnalazione identificativo della segnalazione selezionata
     */
    public void richiediDettaglioSegnalazione(Long idSegnalazione) {

        if (idUtenteAutenticato == null) {
            segnalaErroreConsultazioneInviate(
                    "Sessione utente non valida. Effettua nuovamente l'accesso."
            );
            return;
        }

        if (idSegnalazione == null) {
            segnalaErroreConsultazioneInviate(
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
            segnalaErroreConsultazioneInviate(
                    "Impossibile visualizzare il dettaglio della segnalazione selezionata."
            );
            return;
        }

        comunicaDettaglioSegnalazione(dettaglio);
    }

    /**
     * Comunica graficamente il dettaglio della segnalazione selezionata.
     *
     * La GUI visualizza solo dati semplici già presenti nella Map del dettaglio.
     *
     * @param dettaglio mappa contenente i dati del dettaglio della segnalazione
     */
    public void comunicaDettaglioSegnalazione(Map<String, Object> dettaglio) {

        if (dettaglio == null || dettaglio.isEmpty()) {
            segnalaErroreConsultazioneInviate(
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
                .append(valoreTestuale(dettaglio.get("dataSegnalazione")))
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
                "Visualizza Notifiche",
                "Visualizza Immagine",
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
                opzioni[3]
        );

        if (scelta == 0) {
            visualizzaEvoluzioneDaDettaglio(dettaglio);
        }

        if (scelta == 1) {
            visualizzaNotificheDaDettaglio(dettaglio);
        }

        if (scelta == 2) {
            visualizzaImmagineAllegata(dettaglio.get("immagineAllegata"));
        }
    }

    /**
     * Comunica all'utente un errore relativo alla consultazione
     * delle segnalazioni inviate.
     *
     * La Boundary gestisce la comunicazione grafica dell'esito negativo,
     * mentre il livello Control restituisce solo esiti semplici.
     *
     * @param messaggio descrizione dell'errore da mostrare all'utente
     */
    public void segnalaErroreConsultazioneInviate(String messaggio) {

        String messaggioDaMostrare = valoreTestuale(messaggio);

        if (isVuoto(messaggioDaMostrare)) {
            messaggioDaMostrare = "Impossibile completare la consultazione delle segnalazioni inviate.";
        }

        esitoLabel.setText("Errore durante la consultazione delle segnalazioni inviate.");

        JOptionPane.showMessageDialog(
                contentPanel,
                messaggioDaMostrare,
                "Errore consultazione segnalazioni inviate",
                JOptionPane.ERROR_MESSAGE,
                caricaIconaLogoPopup()
        );
    }

    /**
     * Inizializza i componenti grafici della GUI.
     *
     * Il metodo richiama il blocco di setup generato in stile IntelliJ e poi
     * configura i testi specifici della Boundary.
     */
    private void inizializzaComponenti() {
        $$$setupUI$$$();

        titoloSistemaLabel.setText("Sistema Comunale Gestione Segnalazioni");
        titoloPaginaLabel.setText("<html>Segnalazioni <span style='color:green;'>inviate</span></html>");
        descrizioneLabel.setText("Consulta l'elenco delle segnalazioni che hai inviato.");
        esitoLabel.setText("In attesa di caricamento...");

        visualizzaDettaglioButton.setText("Visualizza Dettaglio");
        tornaDashboardButton.setText("Torna alla Dashboard");

        caricaLogo();
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
                70,
                70,
                Image.SCALE_SMOOTH
        );

        logoLabel.setIcon(new ImageIcon(immagineRidimensionata));
        logoLabel.setText("");
    }

    /**
     * Configura la struttura grafica principale della GUI.
     */
    private void configuraLayout() {
        contentPanel.setLayout(new BorderLayout(20, 20));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
        contentPanel.setPreferredSize(new Dimension(850, 520));

        contentPanel.add(creaHeaderPanel(), BorderLayout.NORTH);
        contentPanel.add(creaCentroPanel(), BorderLayout.CENTER);
        contentPanel.add(creaFooterPanel(), BorderLayout.SOUTH);
    }

    /**
     * Crea il pannello superiore della GUI.
     *
     * @return pannello header
     */
    private JPanel creaHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JPanel titoloConLogoPanel = new JPanel(new BorderLayout());

        JLabel spazioSinistro = new JLabel();
        spazioSinistro.setPreferredSize(new Dimension(70, 70));

        titoloSistemaLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titoloSistemaLabel.setHorizontalAlignment(SwingConstants.CENTER);

        titoloConLogoPanel.add(spazioSinistro, BorderLayout.WEST);
        titoloConLogoPanel.add(titoloSistemaLabel, BorderLayout.CENTER);
        titoloConLogoPanel.add(logoLabel, BorderLayout.EAST);

        titoloPaginaLabel.setFont(new Font("Arial", Font.BOLD, 18));
        descrizioneLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        titoloPaginaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descrizioneLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titoloConLogoPanel);
        headerPanel.add(Box.createVerticalStrut(12));
        headerPanel.add(new JSeparator());
        headerPanel.add(Box.createVerticalStrut(18));

        JPanel titoloPaginaPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titoloPaginaPanel.add(titoloPaginaLabel);

        JPanel descrizionePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        descrizionePanel.add(descrizioneLabel);

        headerPanel.add(titoloPaginaPanel);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(descrizionePanel);

        return headerPanel;
    }

    /**
     * Crea il pannello centrale contenente tabella ed esito.
     *
     * @return pannello centrale
     */
    private JPanel creaCentroPanel() {
        JPanel centroPanel = new JPanel(new BorderLayout(10, 10));

        esitoLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        centroPanel.add(esitoLabel, BorderLayout.NORTH);
        centroPanel.add(scrollPaneSegnalazioni, BorderLayout.CENTER);

        return centroPanel;
    }

    /**
     * Crea il pannello inferiore con i pulsanti disponibili.
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
     * Configura la tabella delle segnalazioni.
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
     * Collega i pulsanti della GUI alle azioni grafiche.
     */
    private void configuraAzioni() {
        visualizzaDettaglioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Long idSegnalazione = ottieniIdSegnalazioneSelezionata();

                if (idSegnalazione == null) {
                    segnalaErroreConsultazioneInviate(
                            "Seleziona una segnalazione dall'elenco."
                    );
                    return;
                }

                selezionaSegnalazione(idSegnalazione);
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
     * @return id della segnalazione selezionata, oppure null
     */
    private Long ottieniIdSegnalazioneSelezionata() {
        int rigaSelezionata = tabellaSegnalazioni.getSelectedRow();

        if (rigaSelezionata == -1) {
            return null;
        }

        Object valoreId = tabellaSegnalazioni.getValueAt(rigaSelezionata, 0);

        return estraiLong(valoreId);
    }

    /**
     * Riapre la dashboard usando i dati semplici di sessione.
     *
     * Il metodo non passa Entity alla dashboard e non accede al dominio.
     */
    private void tornaAllaDashboard() {

        if (datiSessioneUtente == null || idUtenteAutenticato == null) {
            segnalaErroreConsultazioneInviate(
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
     * Costruisce una posizione sintetica a partire dai dati semplici presenti nella Map.
     *
     * @param segnalazione mappa contenente i dati della segnalazione
     * @return testo sintetico della posizione
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
            if (!posizione.isEmpty()) {
                posizione.append(", ");
            }
            posizione.append(numeroCivico);
        }

        if (!isVuoto(citta)) {
            if (!posizione.isEmpty()) {
                posizione.append(" - ");
            }
            posizione.append(citta);
        }

        if (!isVuoto(cap)) {
            if (!posizione.isEmpty()) {
                posizione.append(" ");
            }
            posizione.append("(").append(cap).append(")");
        }

        if (posizione.isEmpty()) {
            return "Posizione non disponibile";
        }

        return posizione.toString();
    }

    /**
     * Recupera lo stato della segnalazione da una Map.
     *
     * La GUI supporta sia la chiave statoCorrente usata in visualizzazione,
     * sia nomeStatoCorrente usata nei criteri coerenti con lo State Pattern.
     *
     * @param datiSegnalazione mappa contenente i dati della segnalazione
     * @return stato corrente come testo
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
     * Converte un valore generico in stringa normalizzata.
     *
     * @param valore valore da convertire
     * @return stringa normalizzata oppure stringa vuota
     */
    private String valoreTestuale(Object valore) {
        if (valore == null) {
            return "";
        }

        return valore.toString().trim();
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

    /**
     * Estrae un Long da un valore generico.
     *
     * @param valore valore da convertire
     * @return Long corrispondente oppure null
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
     * Il metodo appartiene alla Boundary e legge solo valori semplici dalla Map
     * ricevuta dalla dashboard. Non accede a Entity, Control o Database.
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
     * Visualizza l'evoluzione della segnalazione già presente nella Map del dettaglio.
     *
     * @param dettaglio mappa del dettaglio segnalazione
     */
    private void visualizzaEvoluzioneDaDettaglio(Map<String, Object> dettaglio) {
        Object evoluzioneObject = dettaglio.get("evoluzione");

        if (!(evoluzioneObject instanceof List<?> evoluzione)) {
            JOptionPane.showMessageDialog(
                    contentPanel,
                    "Nessuna evoluzione disponibile per la segnalazione selezionata.",
                    "Evoluzione Segnalazione",
                    JOptionPane.INFORMATION_MESSAGE,
                    caricaIconaLogoPopup()
            );
            return;
        }

        StringBuilder testo = new StringBuilder();

        testo.append("ID Segnalazione: ")
                .append(valoreTestuale(dettaglio.get("idSegnalazione")))
                .append("\n\n");

        if (evoluzione.isEmpty()) {
            testo.append("Nessun aggiornamento di stato registrato.");
        } else {
            int progressivo = 1;

            for (Object elemento : evoluzione) {
                if (!(elemento instanceof Map<?, ?> aggiornamento)) {
                    continue;
                }

                testo.append("Evento storico: ")
                        .append(progressivo)
                        .append("\n");

                testo.append("Data: ")
                        .append(valoreTestuale(aggiornamento.get("dataAggiornamento")))
                        .append("\n");

                testo.append("Nuovo stato: ")
                        .append(valoreTestuale(aggiornamento.get("nuovoStato")))
                        .append("\n");

                testo.append("Operatore: ")
                        .append(valoreTestuale(aggiornamento.get("idOperatore")))
                        .append("\n");

                testo.append("------------------------------")
                        .append("\n");

                progressivo++;
            }
        }

        JOptionPane.showMessageDialog(
                contentPanel,
                testo.toString(),
                "Evoluzione Segnalazione",
                JOptionPane.INFORMATION_MESSAGE,
                caricaIconaLogoPopup()
        );
    }

    /**
     * Visualizza le eventuali notifiche associate alla segnalazione.
     *
     * @param dettaglio mappa del dettaglio segnalazione
     */
    private void visualizzaNotificheDaDettaglio(Map<String, Object> dettaglio) {

        if (dettaglio == null) {
            return;
        }

        Object notificheObject = dettaglio.get("notifiche");

        if (!(notificheObject instanceof List<?> notifiche) || notifiche.isEmpty()) {
            JOptionPane.showMessageDialog(
                    contentPanel,
                    "Nessuna notifica disponibile per la segnalazione selezionata.",
                    "Notifiche Segnalazione",
                    JOptionPane.INFORMATION_MESSAGE,
                    caricaIconaLogoPopup()
            );
            return;
        }

        StringBuilder testo = new StringBuilder();

        testo.append("Notifiche associate alla segnalazione ")
                .append(valoreTestuale(dettaglio.get("idSegnalazione")))
                .append("\n\n");

        for (Object elemento : notifiche) {
            if (!(elemento instanceof Map<?, ?> notifica)) {
                continue;
            }

            testo.append("Data: ")
                    .append(valoreTestuale(notifica.get("dataInvio")))
                    .append("\n");

            testo.append("Stato notificato: ")
                    .append(valoreTestuale(notifica.get("statoNotificato")))
                    .append("\n");

            testo.append("Messaggio: ")
                    .append(valoreTestuale(notifica.get("messaggio")))
                    .append("\n");

            testo.append("------------------------------")
                    .append("\n");
        }

        JOptionPane.showMessageDialog(
                contentPanel,
                testo.toString(),
                "Notifiche Segnalazione",
                JOptionPane.INFORMATION_MESSAGE,
                caricaIconaLogoPopup()
        );
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
                    "Nessuna immagine allegata alla segnalazione selezionata.",
                    "Immagine allegata",
                    JOptionPane.INFORMATION_MESSAGE,
                    caricaIconaLogoPopup()
            );
            return;
        }

        ImageIcon immagine = new ImageIcon(percorsoImmagine);

        if (immagine.getIconWidth() <= 0 || immagine.getIconHeight() <= 0) {
            JOptionPane.showMessageDialog(
                    contentPanel,
                    "Impossibile visualizzare l'immagine allegata.",
                    "Immagine non disponibile",
                    JOptionPane.WARNING_MESSAGE,
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
     * Blocco di setup grafico in stile IntelliJ GUI Designer.
     *
     * In questa versione il metodo inizializza i componenti principali,
     * mentre la disposizione grafica effettiva viene completata nei metodi
     * configuraLayout(), creaHeaderPanel(), creaCentroPanel() e creaFooterPanel().
     */
    private void $$$setupUI$$$() {
        contentPanel = new JPanel();

        titoloSistemaLabel = new JLabel();
        titoloPaginaLabel = new JLabel();
        descrizioneLabel = new JLabel();
        esitoLabel = new JLabel();
        logoLabel = new JLabel();

        tabellaSegnalazioni = new JTable();
        scrollPaneSegnalazioni = new JScrollPane(tabellaSegnalazioni);

        visualizzaDettaglioButton = new JButton();
        tornaDashboardButton = new JButton();
    }


    /**
     * Metodo generato in stile IntelliJ GUI Designer.
     *
     * @return componente radice della GUI
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }
}
