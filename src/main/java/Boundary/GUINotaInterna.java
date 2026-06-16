package Boundary;

import Control.ControllerServiziSistema;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Map;

/**
 * Boundary per l'inserimento di una nota interna associata a una segnalazione.
 *
 * La GUI consente all'Operatore Comunale di inserire una nota interna
 * relativa alla segnalazione selezionata, per descrivere l'intervento svolto
 * o lo stato delle verifiche effettuate.
 *
 * La classe è coerente con il modello BCED perché:
 * - riceve solo dati semplici di sessione;
 * - non mantiene istanze Entity dell'utente autenticato;
 * - acquisisce il testo della nota inserito dall'operatore;
 * - invia la richiesta al Facade Controller;
 * - non crea direttamente NotaInterna;
 * - non modifica direttamente Segnalazione;
 * - non accede al package Database;
 * - non usa ServizioPersistenza;
 * - non contiene logica di persistenza;
 * - mantiene l'information hiding delle classi contenute.
 */
public class GUINotaInterna {

	private JPanel contentPanel;

	private JLabel logoLabel;
	private JLabel titoloSistemaLabel;
	private JLabel titoloPaginaLabel;
	private JLabel descrizioneLabel;
	private JLabel idSegnalazioneLabel;
	private JLabel testoNotaLabel;
	private JLabel esitoLabel;

	private JTextArea areaTestoNota;
	private JScrollPane scrollPaneNota;

	private JButton confermaInserimentoButton;
	private JButton annullaButton;

	private ControllerServiziSistema controllerServiziSistema;

	/**
	 * Dati semplici della sessione dell'utente autenticato.
	 *
	 * La Boundary conserva questa Map solo per inoltrare l'id utente al
	 * Facade Controller e per tornare alle altre schermate senza passare
	 * istanze Entity tra Boundary.
	 */
	private Map<String, Object> datiSessioneUtente;

	/**
	 * Identificativo dell'utente autenticato estratto dalla Map di sessione.
	 */
	private Long idUtenteAutenticato;

	/**
	 * Identificativo della segnalazione a cui associare la nota interna.
	 */
	private Long idSegnalazione;

	/**
	 * Costruisce la GUI per l'inserimento di una nota interna.
	 *
	 * La GUI riceve solo dati semplici di sessione e l'identificativo della
	 * segnalazione selezionata. Non riceve né conserva oggetti Entity.
	 *
	 * @param datiSessioneUtente mappa contenente almeno idUtente, nome, cognome e ruolo
	 * @param idSegnalazione identificativo della segnalazione selezionata
	 */
	public GUINotaInterna(
			Map<String, Object> datiSessioneUtente,
			Long idSegnalazione
	) {
		this.datiSessioneUtente = datiSessioneUtente;
		this.idUtenteAutenticato = estraiIdUtenteDaSessione(datiSessioneUtente);

		this.idSegnalazione = idSegnalazione;
		this.controllerServiziSistema = new ControllerServiziSistema();

		inizializzaComponenti();
		configuraLayout();
		configuraAzioni();

		richiediInserimentoNota(idSegnalazione);
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
	 * Avvia la procedura di inserimento della nota interna per la
	 * segnalazione selezionata.
	 *
	 * @param idSegnalazione identificativo della segnalazione selezionata
	 */
	public void richiediInserimentoNota(Long idSegnalazione) {
		this.idSegnalazione = idSegnalazione;

		if (idSegnalazione == null) {
			idSegnalazioneLabel.setText("ID Segnalazione: -");
			esitoLabel.setText("Nessuna segnalazione selezionata.");
		} else {
			idSegnalazioneLabel.setText("ID Segnalazione: " + idSegnalazione);
			esitoLabel.setText("Inserisci il testo della nota interna.");
		}
	}

	/**
	 * Metodo pubblico previsto dalla Boundary.
	 *
	 * Acquisisce il testo della nota interna inserito dall'operatore.
	 *
	 * @return testo della nota interna
	 */
	public String acquisisciTestoNota() {
		return areaTestoNota.getText();
	}

	/**
	 * Metodo pubblico previsto dalla Boundary.
	 *
	 * Invia la nota interna al Facade Controller usando solo dati semplici:
	 * idUtenteAutenticato, idSegnalazione e testoNota.
	 *
	 * La GUI non crea NotaInterna e non modifica direttamente Segnalazione.
	 *
	 * @param idSegnalazione identificativo della segnalazione
	 * @param testoNota testo della nota interna
	 */
	public void inviaNotaInterna(
			Long idSegnalazione,
			String testoNota
	) {
		if (idUtenteAutenticato == null) {
			segnalaErroreNotaInterna(
					"Sessione utente non valida. Effettua nuovamente l'accesso."
			);
			return;
		}

		if (idSegnalazione == null) {
			segnalaErroreNotaInterna(
					"Nessuna segnalazione selezionata. Seleziona una segnalazione prima di inserire la nota."
			);
			return;
		}

		if (!validaTestoNota(testoNota)) {
			segnalaErroreNotaInterna(
					"Inserisci il testo della nota interna."
			);
			return;
		}

		boolean inserimentoRiuscito =
				controllerServiziSistema.aggiungiNotaInterna(
						idUtenteAutenticato,
						idSegnalazione,
						testoNota
				);

		if (inserimentoRiuscito) {
				comunicaConfermaInserimento();
			tornaAllaConsultazioneRicevute();
		} else {
			segnalaErroreNotaInterna(
					"Inserimento non completato. Verifica di avere i permessi e che la segnalazione sia valida."
			);
		}
	}

	/**
	 * Metodo pubblico previsto dalla Boundary.
	 *
	 * Effettua una validazione preliminare lato GUI.
	 * La logica applicativa e la verifica dei permessi restano nel Control.
	 *
	 * @param testoNota testo inserito dall'operatore
	 * @return true se il testo è valorizzato, false altrimenti
	 */
	public boolean validaTestoNota(String testoNota) {
		return !isVuoto(testoNota);
	}

	/**
	 * Metodo pubblico previsto dalla Boundary.
	 *
	 * Comunica all'utente un errore relativo all'inserimento della nota interna.
	 * La Boundary gestisce la comunicazione grafica dell'esito negativo, mentre
	 * il livello Control restituisce solo un esito booleano.
	 *
	 * @param messaggio descrizione dell'errore da mostrare all'utente
	 */
	public void segnalaErroreNotaInterna(String messaggio) {

		String messaggioDaMostrare = valoreTestuale(messaggio);

		if (isVuoto(messaggioDaMostrare)) {
			messaggioDaMostrare = "Impossibile inserire la nota interna.";
		}

		esitoLabel.setText("Errore durante l'inserimento della nota interna.");

		JOptionPane.showMessageDialog(
				contentPanel,
				messaggioDaMostrare,
				"Errore nota interna",
				JOptionPane.ERROR_MESSAGE,
				caricaIconaLogoPopup()
		);
	}

	/**
	 * Metodo pubblico previsto dalla Boundary.
	 *
	 * Comunica la corretta conclusione dell'inserimento della nota interna.
	 */
	public void comunicaConfermaInserimento() {
		esitoLabel.setText("Nota interna inserita correttamente.");

		JOptionPane.showMessageDialog(
				contentPanel,
				"Nota interna inserita correttamente.",
				"Inserimento completato",
				JOptionPane.INFORMATION_MESSAGE,
				caricaIconaLogoPopup()
		);
	}

	/**
	 * Inizializza i componenti grafici della schermata.
	 */
	private void inizializzaComponenti() {
		contentPanel = new JPanel();

		logoLabel = new JLabel();

		titoloSistemaLabel = new JLabel("Sistema Comunale Gestione Segnalazioni");

		titoloPaginaLabel = new JLabel(
				"<html>Nota <span style='color:#1E88E5;'>interna</span></html>"
		);

		descrizioneLabel = new JLabel(
				"Inserisci una nota interna associata alla segnalazione selezionata."
		);

		idSegnalazioneLabel = new JLabel("ID Segnalazione: -");
		testoNotaLabel = new JLabel("Testo nota interna");
		esitoLabel = new JLabel("In attesa di inserimento nota...");

		areaTestoNota = new JTextArea();
		areaTestoNota.setLineWrap(true);
		areaTestoNota.setWrapStyleWord(true);
		areaTestoNota.setRows(8);

		scrollPaneNota = new JScrollPane(areaTestoNota);

		confermaInserimentoButton = new JButton("Conferma Inserimento");
		annullaButton = new JButton("Annulla");

		caricaLogo();
	}

	/**
	 * Configura il layout principale.
	 */
	private void configuraLayout() {
		contentPanel.setLayout(new BorderLayout(20, 20));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
		contentPanel.setPreferredSize(new Dimension(760, 520));

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
	 * Crea la parte centrale della GUI.
	 *
	 * @return pannello centrale
	 */
	private JPanel creaCentroPanel() {
		JPanel centroPanel = new JPanel(new GridBagLayout());
		GridBagConstraints vincoli = new GridBagConstraints();

		vincoli.insets = new Insets(8, 8, 8, 8);
		vincoli.fill = GridBagConstraints.HORIZONTAL;

		idSegnalazioneLabel.setFont(new Font("Arial", Font.BOLD, 14));
		testoNotaLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		esitoLabel.setFont(new Font("Arial", Font.PLAIN, 13));

		vincoli.gridx = 0;
		vincoli.gridy = 0;
		vincoli.gridwidth = 2;
		vincoli.weightx = 1.0;
		centroPanel.add(idSegnalazioneLabel, vincoli);

		vincoli.gridx = 0;
		vincoli.gridy = 1;
		vincoli.gridwidth = 2;
		centroPanel.add(testoNotaLabel, vincoli);

		vincoli.gridx = 0;
		vincoli.gridy = 2;
		vincoli.gridwidth = 2;
		vincoli.weightx = 1.0;
		vincoli.weighty = 1.0;
		vincoli.fill = GridBagConstraints.BOTH;
		scrollPaneNota.setPreferredSize(new Dimension(620, 180));
		centroPanel.add(scrollPaneNota, vincoli);

		vincoli.gridx = 0;
		vincoli.gridy = 3;
		vincoli.gridwidth = 2;
		vincoli.weighty = 0.0;
		vincoli.fill = GridBagConstraints.HORIZONTAL;
		centroPanel.add(esitoLabel, vincoli);

		return centroPanel;
	}

	/**
	 * Crea la parte inferiore con i pulsanti principali.
	 *
	 * @return pannello footer
	 */
	private JPanel creaFooterPanel() {
		JPanel footerPanel = new JPanel(new GridLayout(1, 2, 20, 0));

		footerPanel.add(confermaInserimentoButton);
		footerPanel.add(annullaButton);

		return footerPanel;
	}

	/**
	 * Collega i pulsanti alle rispettive azioni.
	 */
	private void configuraAzioni() {
		confermaInserimentoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String testoNota = acquisisciTestoNota();
				inviaNotaInterna(idSegnalazione, testoNota);
			}
		});

		annullaButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tornaAllaConsultazioneRicevute();
			}
		});
	}

	/**
	 * Ritorna alla GUI di consultazione delle segnalazioni ricevute.
	 *
	 * La nota interna è attivata a partire dal flusso dell'Operatore Comunale
	 * sulla segnalazione selezionata. Per questo motivo, al termine
	 * dell'inserimento o in caso di annullamento, il ritorno alla consultazione
	 * delle segnalazioni ricevute mantiene la navigazione coerente con il caso
	 * d'uso di gestione della segnalazione.
	 *
	 * La GUI passa solo la Map di sessione alla Boundary di consultazione,
	 * mantenendo il disaccoppiamento dalle Entity.
	 */
	private void tornaAllaConsultazioneRicevute() {

		if (datiSessioneUtente == null || idUtenteAutenticato == null) {
			segnalaErroreNotaInterna(
					"Sessione utente non valida. Impossibile tornare alla consultazione delle segnalazioni ricevute."
			);
			return;
		}

		GUIConsultazioneSegnalazioniRicevute guiConsultazione =
				new GUIConsultazioneSegnalazioniRicevute(datiSessioneUtente);

		JFrame frameConsultazione = new JFrame("Segnalazioni Ricevute");
		frameConsultazione.setContentPane(guiConsultazione.getContentPanel());
		frameConsultazione.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frameConsultazione.setResizable(false);
		frameConsultazione.pack();
		frameConsultazione.setLocationRelativeTo(null);
		frameConsultazione.setVisible(true);

		chiudiFinestraCorrente();
	}

	/**
	 * Chiude la finestra corrente della GUI.
	 */
	private void chiudiFinestraCorrente() {
		Window finestraCorrente =
				SwingUtilities.getWindowAncestor(contentPanel);

		if (finestraCorrente != null) {
			finestraCorrente.dispose();
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
	 * Verifica se una stringa è nulla o vuota.
	 *
	 * @param valore stringa da verificare
	 * @return true se vuota, false altrimenti
	 */
	private boolean isVuoto(String valore) {
		return valore == null || valore.trim().isEmpty();
	}

	/**
	 * Estrae l'identificativo dell'utente autenticato dai dati semplici di sessione.
	 *
	 * Il metodo appartiene alla Boundary e legge solo valori semplici dalla Map
	 * ricevuta dalla schermata precedente. Non accede a Entity, Control o Database.
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
	 * Converte un valore generico in stringa normalizzata.
	 *
	 * @param valore valore da convertire
	 * @return stringa senza spazi iniziali/finali, oppure stringa vuota
	 */
	private String valoreTestuale(Object valore) {

		if (valore == null) {
			return "";
		}

		return valore.toString().trim();
	}
}