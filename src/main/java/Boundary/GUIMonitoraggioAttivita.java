
package Boundary;

import Control.ControllerServiziSistema;

import javax.swing.*;
        import javax.swing.border.TitledBorder;
import java.awt.*;
        import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Boundary per il monitoraggio dell'andamento delle attività.
 *
 * La GUI implementa lo scenario base del caso d'uso:
 * - l'Operatore Comunale accede alla sezione di monitoraggio;
 * - seleziona un intervallo temporale;
 * - invia la richiesta di monitoraggio;
 * - visualizza totale segnalazioni ricevute, segnalazioni risolte,
 *   segnalazioni ancora aperte e distribuzione per categoria.
 *
 * La classe è coerente con il modello BCED perché:
 * - riceve solo dati semplici di sessione;
 * - non mantiene istanze Entity dell'utente autenticato;
 * - acquisisce solo i parametri inseriti dall'operatore;
 * - invia la richiesta al Facade Controller;
 * - comunica i risultati restituiti dal sistema;
 * - non accede direttamente al package Database;
 * - non usa ServizioPersistenza;
 * - non manipola direttamente le Entity;
 * - non attraversa oggetti contenuti.
 */
public class GUIMonitoraggioAttivita {

    private JPanel contentPanel;

    private JLabel logoLabel;
    private JLabel titoloSistemaLabel;
    private JLabel titoloPaginaLabel;
    private JLabel descrizioneLabel;

    private JLabel dataInizioLabel;
    private JLabel dataFineLabel;
    private JLabel esitoLabel;

    private JTextField txtDataInizio;
    private JTextField txtDataFine;

    private JLabel totaleSegnalazioniLabel;
    private JLabel segnalazioniRisolteLabel;
    private JLabel segnalazioniAperteLabel;

    private JTextArea distribuzioneCategoriaArea;
    private JScrollPane scrollDistribuzioneCategoria;
    private JPanel pannelloDettaglioRisultati;
    private TitledBorder bordoDettaglioRisultati;

    private JButton elaboraMonitoraggioButton;
    private JButton rimuoviParametriButton;
    private JButton tornaDashboardButton;

    private JButton segnalazioniInLavorazioneButton;
    private JButton vistaRiepilogativaZonaButton;

    private ControllerServiziSistema controllerServiziSistema;

    /**
     * Dati semplici della sessione dell'utente autenticato.
     *
     * La Boundary conserva questa Map solo per inoltrare l'id utente
     * al Facade Controller e per tornare alla dashboard senza passare
     * istanze Entity.
     */
    private Map<String, Object> datiSessioneUtente;

    /**
     * Identificativo dell'utente autenticato estratto dalla Map di sessione.
     */
    private Long idUtenteAutenticato;

    /**
     * Costruisce la GUI di monitoraggio attività.
     *
     * La GUI riceve solo dati semplici di sessione e non un oggetto Entity
     * Utente. L'id dell'operatore autenticato viene estratto dalla Map e
     * usato per inoltrare le richieste al Facade Controller.
     *
     * @param datiSessioneUtente mappa contenente almeno idUtente, nome, cognome e ruolo
     */
    public GUIMonitoraggioAttivita(Map<String, Object> datiSessioneUtente) {
        this.datiSessioneUtente = datiSessioneUtente;
        this.idUtenteAutenticato = estraiIdUtenteDaSessione(datiSessioneUtente);
        this.controllerServiziSistema = new ControllerServiziSistema();

        inizializzaComponenti();
        configuraLayout();
        configuraAzioni();

        richiediMonitoraggioAttivita();
    }

    /**
     * Restituisce il pannello principale della GUI.
     *
     * @return pannello principale della Boundary
     */
    public JPanel getContentPanel() {
        return contentPanel;
    }

    /**
     * Metodo pubblico previsto dalla Boundary.
     *
     * Avvia la schermata di monitoraggio attività.
     */
    public void richiediMonitoraggioAttivita() {
        pulisciRisultatiMonitoraggio();
        esitoLabel.setText("Inserisci data inizio e data fine nel formato gg-mm-aaaa, poi avvia il monitoraggio.");
    }

    /**
     * Metodo pubblico previsto dalla Boundary.
     *
     * Acquisisce l'intervallo temporale selezionato dall'operatore.
     *
     * @return parametri del monitoraggio
     */
    public Map<String, Object> acquisisciParametriMonitoraggio() {
        Map<String, Object> parametriMonitoraggio = new HashMap<>();

        Date dataInizio = estraiData(txtDataInizio.getText());
        Date dataFine = estraiData(txtDataFine.getText());

        if (dataInizio != null) {
            parametriMonitoraggio.put("dataInizio", dataInizio);
        }

        if (dataFine != null) {
            parametriMonitoraggio.put("dataFine", dataFine);
        }

        parametriMonitoraggio.put("tipoMonitoraggio", "generale");

        return parametriMonitoraggio;
    }

    /**
     * Metodo pubblico previsto dalla Boundary.
     *
     * Invia la richiesta di monitoraggio al Facade Controller usando solo
     * idUtenteAutenticato e parametri semplici.
     *
     * @param parametriMonitoraggio parametri acquisiti dalla GUI
     */
    /**
     * Metodo pubblico previsto dalla Boundary aggiornato per riflettere
     * esattamente le specifiche della test suite del caso d'uso 3.4.
     */
    public void inviaRichiestaMonitoraggio(Map<String, Object> parametriMonitoraggio) {
        // TC_3: Utente non autenticato
        if (idUtenteAutenticato == null) {
            segnalaErroreMonitoraggio("Effettuare l'accesso per visualizzare i report.");
            return;
        }

        // TC_2: Violazione permessi (Ruolo Cittadino)
        if (datiSessioneUtente != null && "Cittadino".equals(datiSessioneUtente.get("ruolo"))) {
            segnalaErroreMonitoraggio("Accesso negato. Solo gli operatori possono visualizzare l'andamento.");
            return;
        }

        String strDataInizio = txtDataInizio.getText().trim();
        String strDataFine = txtDataFine.getText().trim();

        SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
        formato.setLenient(false);

        // TC_5: Formato Data Inizio non valido
        if (!strDataInizio.isEmpty()) {
            try {
                formato.parse(strDataInizio);
            } catch (ParseException e) {
                segnalaErroreMonitoraggio("Formato data di inizio non valido.");
                return;
            }
        }

        // TC_8: Formato Data Fine non valido o inesistente
        if (!strDataFine.isEmpty()) {
            try {
                formato.parse(strDataFine);
            } catch (ParseException e) {
                segnalaErroreMonitoraggio("Formato data di fine non valido o inesistente.");
                return;
            }
        }

        // Controllo generico di presenza parametri obbligatori
        if (parametriMonitoraggio == null || parametriMonitoraggio.get("dataInizio") == null || parametriMonitoraggio.get("dataFine") == null) {
            segnalaErroreMonitoraggio("Inserisci entrambe le date nel formato gg-mm-aaaa.");
            return;
        }

        Date dInizio = (Date) parametriMonitoraggio.get("dataInizio");
        Date dFine = (Date) parametriMonitoraggio.get("dataFine");
        Date oggi = new Date();

        // TC_4: Data Inizio futura
        if (dInizio.after(oggi)) {
            segnalaErroreMonitoraggio("La data di inizio non può essere successiva alla data odierna.");
            return;
        }

        // TC_7: Data Fine futura
        if (dFine.after(oggi)) {
            segnalaErroreMonitoraggio("La data di fine non può essere nel futuro.");
            return;
        }

        // TC_6: Intervallo Inizio rovesciato
        if (dInizio.after(dFine)) {
            segnalaErroreMonitoraggio("La data di inizio deve essere antecedente o uguale alla data di fine.");
            return;
        }

        // TC_1: Scenario di successo coordinato dal Facade Controller
        Map<String, Object> risultatiMonitoraggio =
                controllerServiziSistema.monitoraAndamentoAttivita(
                        idUtenteAutenticato,
                        parametriMonitoraggio
                );

        if (risultatiMonitoraggio == null || risultatiMonitoraggio.isEmpty()) {
            segnalaErroreMonitoraggio("Impossibile generare i risultati del monitoraggio per i criteri selezionati.");
            return;
        }

        comunicaRisultatiMonitoraggio(risultatiMonitoraggio);
    }

    /**
     * Metodo pubblico previsto dalla Boundary.
     *
     * Comunica all'operatore i risultati del monitoraggio.
     *
     * @param risultatiMonitoraggio risultati aggregati del monitoraggio
     */
    public void comunicaRisultatiMonitoraggio(
            Map<String, Object> risultatiMonitoraggio
    ) {
        if (risultatiMonitoraggio == null || risultatiMonitoraggio.isEmpty()) {
            segnalaErroreMonitoraggio(
                    "Nessun risultato di monitoraggio disponibile per i criteri selezionati."
            );
            return;
        }

        totaleSegnalazioniLabel.setText(
                "Totale segnalazioni ricevute: "
                        + valoreNumericoTestuale(risultatiMonitoraggio.get("totaleSegnalazioni"))
        );

        segnalazioniRisolteLabel.setText(
                "Segnalazioni risolte: "
                        + valoreNumericoTestuale(risultatiMonitoraggio.get("segnalazioniRisolte"))
        );

        segnalazioniAperteLabel.setText(
                "Segnalazioni ancora aperte: "
                        + valoreNumericoTestuale(risultatiMonitoraggio.get("segnalazioniAperte"))
        );

        String tipoMonitoraggio =
                valoreTestuale(risultatiMonitoraggio.get("tipoMonitoraggio"));

        if ("inLavorazione".equals(tipoMonitoraggio)) {
            aggiornaTitoloDettaglioRisultati("Segnalazioni attualmente in lavorazione");

            comunicaSegnalazioniRisultato(
                    risultatiMonitoraggio.get("segnalazioniRisultato")
            );

            esitoLabel.setText("Scenario opzionale caricato: segnalazioni in lavorazione.");
            return;
        }

        if ("riepilogoZona".equals(tipoMonitoraggio)) {
            aggiornaTitoloDettaglioRisultati("Vista riepilogativa zona");

            comunicaSegnalazioniRisultato(
                    risultatiMonitoraggio.get("segnalazioniRisultato")
            );

            esitoLabel.setText("Scenario alternativo caricato: vista riepilogativa zona.");
            return;
        }

        aggiornaTitoloDettaglioRisultati("Distribuzione per categoria");

        comunicaDistribuzioneCategoria(
                risultatiMonitoraggio.get("conteggioPerCategoria")
        );

        esitoLabel.setText("Risultati monitoraggio caricati correttamente.");
    }

    /**
     * Comunica un errore durante il monitoraggio.
     */
    public void segnalaErroreMonitoraggio() {
        esitoLabel.setText("Errore durante il monitoraggio delle attività.");

        JOptionPane.showMessageDialog(
                contentPanel,
                "Impossibile generare i risultati di monitoraggio.",
                "Errore monitoraggio",
                JOptionPane.ERROR_MESSAGE,
                caricaIconaLogoPopup()
        );
    }

    /**
     * Inizializza i componenti grafici.
     */
    private void inizializzaComponenti() {
        contentPanel = new JPanel();

        logoLabel = new JLabel();
        titoloSistemaLabel = new JLabel("Sistema Comunale Gestione Segnalazioni");

        titoloPaginaLabel = new JLabel(
                "<html>Monitoraggio <span style='color:#1E88E5;'>attività</span></html>"
        );

        descrizioneLabel = new JLabel(
                "Seleziona un intervallo temporale per analizzare l'andamento delle segnalazioni."
        );

        dataInizioLabel = new JLabel("Data inizio");
        dataFineLabel = new JLabel("Data fine");

        txtDataInizio = new JTextField();
        txtDataFine = new JTextField();

        txtDataInizio.setToolTipText("Formato richiesto: dd-MM-yyyy");
        txtDataFine.setToolTipText("Formato richiesto: dd-MM-yyyy");

        esitoLabel = new JLabel("Inserisci un intervallo temporale e avvia il monitoraggio.");

        totaleSegnalazioniLabel = new JLabel("Totale segnalazioni ricevute: -");
        segnalazioniRisolteLabel = new JLabel("Segnalazioni risolte: -");
        segnalazioniAperteLabel = new JLabel("Segnalazioni ancora aperte: -");

        distribuzioneCategoriaArea = new JTextArea();
        distribuzioneCategoriaArea.setEditable(false);
        distribuzioneCategoriaArea.setLineWrap(true);
        distribuzioneCategoriaArea.setWrapStyleWord(true);
        distribuzioneCategoriaArea.setText("Distribuzione per categoria non ancora disponibile.");

        scrollDistribuzioneCategoria = new JScrollPane(distribuzioneCategoriaArea);

        elaboraMonitoraggioButton = new JButton("Elabora Monitoraggio");
        rimuoviParametriButton = new JButton("Rimuovi Parametri");

        segnalazioniInLavorazioneButton = new JButton("Segnalazioni in lavorazione");
        vistaRiepilogativaZonaButton = new JButton("Vista riepilogativa zona");

        tornaDashboardButton = new JButton("Torna alla Dashboard");

        caricaLogo();
    }

    /**
     * Configura il layout principale.
     */
    private void configuraLayout() {
        contentPanel.setLayout(new BorderLayout(20, 20));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
        contentPanel.setPreferredSize(new Dimension(920, 700));

        contentPanel.add(creaHeaderPanel(), BorderLayout.NORTH);
        contentPanel.add(creaCentroPanel(), BorderLayout.CENTER);
        contentPanel.add(creaFooterPanel(), BorderLayout.SOUTH);
    }

    /**
     * Crea la parte superiore della GUI.
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

        titoloPaginaLabel.setFont(new Font("Arial", Font.BOLD, 21));
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
     * Crea la parte centrale della GUI.
     *
     * @return pannello centrale
     */
    private JPanel creaCentroPanel() {
        JPanel centroPanel = new JPanel(new BorderLayout(16, 18));

        centroPanel.add(creaParametriPanel(), BorderLayout.NORTH);
        centroPanel.add(creaRisultatiPanel(), BorderLayout.CENTER);

        return centroPanel;
    }

    /**
     * Crea il pannello dei parametri temporali.
     *
     * @return pannello parametri
     */
    private JPanel creaParametriPanel() {
        JPanel contenitore = new JPanel();
        contenitore.setLayout(new BoxLayout(contenitore, BoxLayout.Y_AXIS));
        contenitore.setBorder(BorderFactory.createTitledBorder("Intervallo temporale"));

        JPanel campiPanel = new JPanel(new GridBagLayout());
        GridBagConstraints vincoli = new GridBagConstraints();

        vincoli.insets = new Insets(8, 8, 8, 8);
        vincoli.fill = GridBagConstraints.HORIZONTAL;

        vincoli.gridx = 0;
        vincoli.gridy = 0;
        vincoli.weightx = 0.5;
        campiPanel.add(dataInizioLabel, vincoli);

        vincoli.gridx = 1;
        vincoli.gridy = 0;
        campiPanel.add(dataFineLabel, vincoli);

        vincoli.gridx = 0;
        vincoli.gridy = 1;
        txtDataInizio.setPreferredSize(new Dimension(220, 32));
        campiPanel.add(txtDataInizio, vincoli);

        vincoli.gridx = 1;
        vincoli.gridy = 1;
        txtDataFine.setPreferredSize(new Dimension(220, 32));
        campiPanel.add(txtDataFine, vincoli);

        JLabel formatoLabel = new JLabel("Formato data richiesto: gg-mm-aaaa");
        formatoLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        JPanel formatoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formatoPanel.add(formatoLabel);

        JPanel pulsantiPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        pulsantiPanel.add(elaboraMonitoraggioButton);
        pulsantiPanel.add(rimuoviParametriButton);

        JPanel esitoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        esitoLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        esitoPanel.add(esitoLabel);

        contenitore.add(campiPanel);
        contenitore.add(formatoPanel);
        contenitore.add(Box.createVerticalStrut(8));
        contenitore.add(pulsantiPanel);
        contenitore.add(Box.createVerticalStrut(8));
        contenitore.add(esitoPanel);

        return contenitore;
    }

    /**
     * Crea il pannello dei risultati.
     *
     * @return pannello risultati
     */
    private JPanel creaRisultatiPanel() {
        JPanel risultatiPanel = new JPanel(new BorderLayout(18, 12));
        risultatiPanel.setBorder(BorderFactory.createTitledBorder("Risultati monitoraggio"));
        risultatiPanel.setPreferredSize(new Dimension(820, 230));

        JPanel riepilogoPanel = new JPanel(new GridLayout(3, 1, 8, 8));
        riepilogoPanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        riepilogoPanel.setPreferredSize(new Dimension(330, 170));

        totaleSegnalazioniLabel.setFont(new Font("Arial", Font.BOLD, 15));
        segnalazioniRisolteLabel.setFont(new Font("Arial", Font.BOLD, 15));
        segnalazioniAperteLabel.setFont(new Font("Arial", Font.BOLD, 15));

        riepilogoPanel.add(totaleSegnalazioniLabel);
        riepilogoPanel.add(segnalazioniRisolteLabel);
        riepilogoPanel.add(segnalazioniAperteLabel);

        pannelloDettaglioRisultati = new JPanel(new BorderLayout());

        bordoDettaglioRisultati =
                BorderFactory.createTitledBorder("Distribuzione per categoria");

        pannelloDettaglioRisultati.setBorder(bordoDettaglioRisultati);
        pannelloDettaglioRisultati.setPreferredSize(new Dimension(450, 170));

        distribuzioneCategoriaArea.setFont(new Font("Arial", Font.PLAIN, 14));
        distribuzioneCategoriaArea.setEditable(false);
        distribuzioneCategoriaArea.setLineWrap(true);
        distribuzioneCategoriaArea.setWrapStyleWord(true);

        scrollDistribuzioneCategoria.setBorder(null);
        scrollDistribuzioneCategoria.setPreferredSize(new Dimension(430, 145));

        pannelloDettaglioRisultati.add(scrollDistribuzioneCategoria, BorderLayout.CENTER);

        risultatiPanel.add(riepilogoPanel, BorderLayout.WEST);
        risultatiPanel.add(pannelloDettaglioRisultati, BorderLayout.CENTER);

        return risultatiPanel;
    }

    /**
     * Crea il pannello inferiore con scenari opzionali/alternativi e ritorno.
     *
     * @return pannello footer
     */
    private JPanel creaFooterPanel() {
        JPanel footerContainer = new JPanel();
        footerContainer.setLayout(new BoxLayout(footerContainer, BoxLayout.Y_AXIS));

        JPanel scenariAlternativiPanel = new JPanel(new GridLayout(1, 2, 20, 0));

        scenariAlternativiPanel.add(segnalazioniInLavorazioneButton);
        scenariAlternativiPanel.add(vistaRiepilogativaZonaButton);

        JPanel dashboardPanel = new JPanel(new GridLayout(1, 1, 20, 0));
        dashboardPanel.add(tornaDashboardButton);

        footerContainer.add(scenariAlternativiPanel);
        footerContainer.add(Box.createVerticalStrut(12));
        footerContainer.add(dashboardPanel);

        return footerContainer;
    }

    /**
     * Collega i pulsanti alle rispettive azioni.
     */
    private void configuraAzioni() {
        elaboraMonitoraggioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Map<String, Object> parametriMonitoraggio =
                        acquisisciParametriMonitoraggio();

                inviaRichiestaMonitoraggio(parametriMonitoraggio);
            }
        });

        segnalazioniInLavorazioneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Map<String, Object> parametriMonitoraggio =
                        acquisisciParametriMonitoraggio();

                parametriMonitoraggio.put("tipoMonitoraggio", "inLavorazione");
                parametriMonitoraggio.put("nomeStatoCorrente", "in_lavorazione");

                inviaRichiestaMonitoraggio(parametriMonitoraggio);
            }
        });

        vistaRiepilogativaZonaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String areaComunale = JOptionPane.showInputDialog(
                        contentPanel,
                        "Inserisci l'area comunale da analizzare:",
                        "Vista riepilogativa zona",
                        JOptionPane.QUESTION_MESSAGE
                );

                if (isVuoto(areaComunale)) {
                    segnalaErroreMonitoraggio(
                            "Inserisci un'area comunale valida per generare la vista riepilogativa di zona."
                    );
                    return;
                }

                Map<String, Object> parametriMonitoraggio =
                        acquisisciParametriMonitoraggio();

                parametriMonitoraggio.put("tipoMonitoraggio", "riepilogoZona");
                parametriMonitoraggio.put("posizione.areaComunale", areaComunale.trim());

                inviaRichiestaMonitoraggio(parametriMonitoraggio);
            }
        });

        rimuoviParametriButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtDataInizio.setText("");
                txtDataFine.setText("");
                pulisciRisultatiMonitoraggio();
                esitoLabel.setText("Parametri rimossi. Inserisci un nuovo intervallo temporale.");
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
     * Comunica la distribuzione per categoria.
     *
     * @param valoreDistribuzione valore restituito dal sistema
     */
    private void comunicaDistribuzioneCategoria(Object valoreDistribuzione) {
        if (!(valoreDistribuzione instanceof Map<?, ?> distribuzione)) {
            distribuzioneCategoriaArea.setText("Distribuzione per categoria non disponibile.");
            return;
        }

        StringBuilder testo = new StringBuilder();

        for (Map.Entry<?, ?> entry : distribuzione.entrySet()) {
            testo.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("\n");
        }

        if (testo.length() == 0) {
            distribuzioneCategoriaArea.setText("Nessuna segnalazione trovata nell'intervallo selezionato.");
        } else {
            distribuzioneCategoriaArea.setText(testo.toString());
        }
    }

    /**
     * Comunica le segnalazioni risultanti da scenari opzionali o alternativi.
     *
     * @param valoreRisultati valore restituito dal sistema
     */
    private void comunicaSegnalazioniRisultato(Object valoreRisultati) {
        if (!(valoreRisultati instanceof java.util.List<?> listaRisultati)) {
            distribuzioneCategoriaArea.setText("Nessuna informazione disponibile.");
            return;
        }

        StringBuilder testo = new StringBuilder();

        if (listaRisultati.isEmpty()) {
            testo.append("Nessuna segnalazione trovata.");
            distribuzioneCategoriaArea.setText(testo.toString());
            return;
        }

        for (Object elemento : listaRisultati) {
            if (!(elemento instanceof Map<?, ?> segnalazione)) {
                continue;
            }

            testo.append("ID: ")
                    .append(valoreTestuale(segnalazione.get("idSegnalazione")))
                    .append("\nTitolo: ")
                    .append(valoreTestuale(segnalazione.get("titolo")))
                    .append("\nCategoria: ")
                    .append(valoreTestuale(segnalazione.get("categoria")))
                    .append("\nStato: ")
                    .append(valoreTestuale(recuperaStatoDaMappa(segnalazione)))
                    .append("\nArea comunale: ")
                    .append(valoreTestuale(segnalazione.get("areaComunale")))
                    .append("\n")
                    .append("----------------------------------------")
                    .append("\n");
        }

        distribuzioneCategoriaArea.setText(testo.toString());
    }

    /**
     * Pulisce i risultati del monitoraggio.
     */
    private void pulisciRisultatiMonitoraggio() {
        totaleSegnalazioniLabel.setText("Totale segnalazioni ricevute: -");
        segnalazioniRisolteLabel.setText("Segnalazioni risolte: -");
        segnalazioniAperteLabel.setText("Segnalazioni ancora aperte: -");

        aggiornaTitoloDettaglioRisultati("Distribuzione per categoria");
        distribuzioneCategoriaArea.setText("Distribuzione per categoria non ancora disponibile.");
    }

    /**
     * Aggiorna il titolo del pannello di dettaglio dei risultati.
     *
     * @param titolo titolo da mostrare
     */
    private void aggiornaTitoloDettaglioRisultati(String titolo) {
        if (bordoDettaglioRisultati != null) {
            bordoDettaglioRisultati.setTitle(titolo);
        }

        if (pannelloDettaglioRisultati != null) {
            pannelloDettaglioRisultati.repaint();
        }
    }

    /**
     * Ritorna alla dashboard dell'utente autenticato usando la Map di sessione.
     */
    private void tornaAllaDashboard() {

        if (datiSessioneUtente == null || idUtenteAutenticato == null) {
            segnalaErroreMonitoraggio(
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

        Window finestraCorrente =
                SwingUtilities.getWindowAncestor(contentPanel);

        if (finestraCorrente != null) {
            finestraCorrente.dispose();
        }
    }

    /**
     * Estrae una data da una stringa nel formato dd-MM-yyyy.
     *
     * @param valore valore inserito nella GUI
     * @return data convertita oppure null
     */
    private Date estraiData(String valore) {
        if (isVuoto(valore)) {
            return null;
        }

        try {
            SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
            formato.setLenient(false);
            return formato.parse(valore.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Carica il logo dalle resources del progetto.
     *
     * Percorso atteso: src/main/resources/images/logo.png
     */
    private void caricaLogo() {
        URL urlLogo = getClass().getResource("/images/logo.png");

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
     * Carica il logo da usare nei popup.
     *
     * @return icona ridimensionata oppure null
     */
    private ImageIcon caricaIconaLogoPopup() {
        URL urlLogo = getClass().getResource("/images/logo.png");

        if (urlLogo == null) {
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
     * Recupera lo stato corrente da una Map.
     *
     * Supporta sia statoCorrente sia nomeStatoCorrente, coerentemente con
     * l'aggiornamento allo State Pattern.
     *
     * @param datiSegnalazione mappa della segnalazione
     * @return stato corrente
     */
    private Object recuperaStatoDaMappa(Map<?, ?> datiSegnalazione) {
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
     * @return valore Long oppure null
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
     * Converte un valore numerico in stringa, usando 0 se il valore è assente.
     *
     * @param valore valore da convertire
     * @return stringa numerica
     */
    private String valoreNumericoTestuale(Object valore) {
        if (valore == null) {
            return "0";
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
     * Comunica all'utente un errore relativo al monitoraggio delle attività.
     *
     * La Boundary gestisce esclusivamente la comunicazione grafica dell'esito
     * negativo, mentre il livello Control restituisce solo un esito semplice
     * relativo alla generazione dei risultati di monitoraggio.
     *
     * @param messaggio descrizione dell'errore da mostrare all'utente
     */
    /**
     * Modificato per impostare il testo specifico dell'errore direttamente su esitoLabel.
     */
    public void segnalaErroreMonitoraggio(String messaggio) {
        String messaggioDaMostrare = valoreTestuale(messaggio);

        if (isVuoto(messaggioDaMostrare)) {
            messaggioDaMostrare = "Impossibile generare i risultati del monitoraggio.";
        }

        // Imposta il messaggio specifico direttamente sulla label visibile all'utente
        esitoLabel.setText(messaggioDaMostrare);

        // Evita il blocco del thread nei test automatici headless controllando l'ambiente grafico
        if (!GraphicsEnvironment.isHeadless()) {
            JOptionPane.showMessageDialog(
                    contentPanel,
                    messaggioDaMostrare,
                    "Errore monitoraggio attività",
                    JOptionPane.ERROR_MESSAGE,
                    caricaIconaLogoPopup()
            );
        }
    }

    // Setter e Getter aggiunti/modificati ad uso esclusivo dei Test Black-Box per iniettare gli input
    public void setTxtDataInizio(String testo) {
        this.txtDataInizio.setText(testo);
    }

    public void setTxtDataFine(String testo) {
        this.txtDataFine.setText(testo);
    }

    public String getEsitoLabelText() {
        return this.esitoLabel.getText();
    }

    public void setControllerServiziSistema(ControllerServiziSistema controller) {
        this.controllerServiziSistema = controller;
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
}