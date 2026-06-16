package Control;

import java.util.List;
import java.util.Map;

/**
 * Svolge il ruolo di Facade del package Control e rappresenta il punto unico
 * di accesso alle funzionalità principali del sistema.
 *
 * I suoi metodi coordinano l'avvio dei casi d'uso individuati in analisi,
 * disaccoppiando le classi Boundary dai controller specifici.
 *
 * La classe è coerente con il modello BCED perché:
 * - le Boundary comunicano solo con questo Facade Controller;
 * - il Facade Controller non accede direttamente al package Database;
 * - il Facade Controller non manipola direttamente la persistenza;
 * - il Facade Controller non crea direttamente Entity;
 * - il Facade Controller non attraversa oggetti contenuti;
 * - la logica specifica dei casi d'uso viene delegata ai controller dedicati;
 * - gli scenari alternativi restano gestiti nei controller specifici del caso d'uso.
 */
public class ControllerServiziSistema {

	private ControllerRegistrazioneUtente controllerRegistrazioneUtente;
	private ControllerCreazioneSegnalazione controllerCreazioneSegnalazione;
	private ControllerConsultazioneSegnalazioni controllerConsultazioneSegnalazioni;
	private ControllerDettaglioSegnalazione controllerDettaglioSegnalazione;
	private ControllerAggiornamentoStatoSegnalazione controllerAggiornamentoStatoSegnalazione;
	private ControllerNotaInterna controllerNotaInterna;
	private ControllerMonitoraggioAttivita controllerMonitoraggioAttivita;

	/**
	 * Costruisce il Facade Controller inizializzando i controller specifici
	 * dei casi d'uso principali.
	 *
	 * La classe non istanzia Facade Entity e non accede alla persistenza:
	 * tali responsabilità restano nei controller specifici e nei Facade Entity.
	 */
	public ControllerServiziSistema() {
		this.controllerRegistrazioneUtente = new ControllerRegistrazioneUtente();
		this.controllerCreazioneSegnalazione = new ControllerCreazioneSegnalazione();
		this.controllerConsultazioneSegnalazioni = new ControllerConsultazioneSegnalazioni();
		this.controllerDettaglioSegnalazione = new ControllerDettaglioSegnalazione();
		this.controllerAggiornamentoStatoSegnalazione = new ControllerAggiornamentoStatoSegnalazione();
		this.controllerNotaInterna = new ControllerNotaInterna();
		this.controllerMonitoraggioAttivita = new ControllerMonitoraggioAttivita();
	}

	/**
	 * Metodo facade invocato dalla GUIRegistrazioneUtente.
	 *
	 * Coordina il caso d'uso di registrazione utente delegando l'intera
	 * procedura al ControllerRegistrazioneUtente.
	 *
	 * Il valore Long restituito dal controller specifico rappresenta l'idUtente
	 * generato in caso di registrazione corretta. Il Facade lo interpreta come
	 * esito booleano per la Boundary.
	 *
	 * @param datiRegistrazione dati semplici acquisiti dalla GUI di registrazione
	 * @return true se la registrazione viene completata correttamente, false altrimenti
	 */
	public boolean registraUtente(Map<String, Object> datiRegistrazione) {
		if (datiRegistrazione == null) {
			return false;
		}

		try {
			Long idUtenteRegistrato = controllerRegistrazioneUtente.richiediRegistrazioneUtente(datiRegistrazione);
			return idUtenteRegistrato != null;

		} catch (IllegalArgumentException e) {
			// Rilanciamo l'eccezione alla Boundary in modo che possa leggere il messaggio specifico
			throw e;
		} catch (RuntimeException e) {
			System.err.println("[ControllerServiziSistema] Errore imprevisto durante la registrazione utente.");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Coordina la richiesta di creazione di una nuova segnalazione.
	 *
	 * Il Facade Controller riceve dalla Boundary solo dati semplici e delega
	 * al controller specifico la gestione del caso d'uso.
	 *
	 * Il metodo non crea direttamente Segnalazione, non crea Posizione,
	 * non assegna lo stato iniziale, non accede al Database e non manipola Entity.
	 *
	 * @param idUtente identificativo del cittadino autenticato
	 * @param datiSegnalazione dati acquisiti dalla GUI di creazione
	 * @return true se la segnalazione viene creata correttamente, false altrimenti
	 */
	public boolean creaSegnalazione(
			Long idUtente,
			Map<String, Object> datiSegnalazione
	) {

		if (idUtente == null || datiSegnalazione == null) {
			return false;
		}

		try {
			Long idSegnalazioneRegistrata =
					controllerCreazioneSegnalazione.richiediRegistrazioneSegnalazione(
							idUtente,
							datiSegnalazione
					);

			return idSegnalazioneRegistrata != null;

		} catch (RuntimeException e) {
			System.err.println("[ControllerServiziSistema] Errore durante la creazione della segnalazione.");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Metodo facade invocato dalla GUIConsultazioneSegnalazioniInviate.
	 *
	 * Coordina la consultazione delle segnalazioni inviate dal cittadino,
	 * delegando al controller specifico.
	 *
	 * @param idUtente identificativo del cittadino autenticato
	 * @return lista di segnalazioni mappate come dati semplici, oppure null in caso di errore
	 */
	public List<Map<String, Object>> consultaSegnalazioniInviate(Long idUtente) {

		if (idUtente == null) {
			return null;
		}

		try {
			return controllerConsultazioneSegnalazioni.recuperaSegnalazioniInviate(
					idUtente
			);

		} catch (RuntimeException e) {
			System.err.println("[ControllerServiziSistema] Errore durante la consultazione delle segnalazioni inviate.");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Metodo facade invocato dalla GUIConsultazioneSegnalazioniRicevute.
	 *
	 * Coordina la consultazione delle segnalazioni ricevute dall'operatore
	 * comunale, delegando al controller specifico.
	 *
	 * I criteri di filtro restano interni al caso d'uso di consultazione.
	 *
	 * @param idUtente identificativo dell'operatore autenticato
	 * @param criteriFiltro criteri scelti nella GUI
	 * @return lista di segnalazioni mappate come dati semplici, oppure null in caso di errore
	 */
	public List<Map<String, Object>> consultaSegnalazioniRicevute(
			Long idUtente,
			Map<String, Object> criteriFiltro
	) {

		if (idUtente == null) {
			return null;
		}

		try {
			return controllerConsultazioneSegnalazioni.recuperaSegnalazioniRicevute(
					idUtente,
					criteriFiltro
			);

		} catch (RuntimeException e) {
			System.err.println("[ControllerServiziSistema] Errore durante la consultazione delle segnalazioni ricevute.");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Metodo facade invocato dalle GUI di consultazione.
	 *
	 * Coordina la visualizzazione del dettaglio di una segnalazione selezionata.
	 * Gli eventuali scenari alternativi del dettaglio restano responsabilità
	 * del ControllerDettaglioSegnalazione e non vengono esposti come metodi
	 * autonomi del Facade Controller.
	 *
	 * @param idUtente identificativo dell'utente autenticato
	 * @param idSegnalazione identificativo della segnalazione selezionata
	 * @return dati semplici del dettaglio della segnalazione, oppure null in caso di errore
	 */
	public Map<String, Object> visualizzaDettagliSegnalazione(
			Long idUtente,
			Long idSegnalazione
	) {

		if (idUtente == null || idSegnalazione == null) {
			return null;
		}

		try {
			if (!controllerDettaglioSegnalazione.verificaAccessoDettaglio(
					idUtente,
					idSegnalazione
			)) {
				return null;
			}

			return controllerDettaglioSegnalazione.recuperaDettaglioSegnalazione(
					idUtente,
					idSegnalazione
			);

		} catch (RuntimeException e) {
			System.err.println("[ControllerServiziSistema] Errore durante la visualizzazione del dettaglio segnalazione.");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Metodo facade invocato dalla GUIAggiornamentoStatoSegnalazione.
	 *
	 * Coordina l'avvio del caso d'uso di aggiornamento dello stato,
	 * delegando al controller specifico.
	 *
	 * Con lo State Pattern, la Boundary non passa il nuovo stato: l'avanzamento
	 * viene gestito nel livello Entity tramite ServizioSegnalazioni.
	 *
	 * @param idUtente identificativo dell'operatore autenticato
	 * @param idSegnalazione identificativo della segnalazione selezionata
	 * @return true se lo stato viene aggiornato correttamente, false altrimenti
	 */
	public boolean aggiornaStatoSegnalazione(
			Long idUtente,
			Long idSegnalazione
	) {

		if (idUtente == null || idSegnalazione == null) {
			return false;
		}

		try {
			return controllerAggiornamentoStatoSegnalazione.registraAggiornamentoStato(
					idUtente,
					idSegnalazione
			);

		} catch (RuntimeException e) {
			System.err.println("[ControllerServiziSistema] Errore durante l'aggiornamento stato.");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Coordina il caso d'uso di monitoraggio dell'andamento delle attività.
	 *
	 * Il metodo rappresenta il punto di accesso offerto alle Boundary.
	 * La logica specifica viene delegata al ControllerMonitoraggioAttivita.
	 *
	 * Il Facade Controller non crea statistiche e non accede direttamente
	 * al Database.
	 *
	 * @param idUtente identificativo dell'operatore autenticato
	 * @param criteriMonitoraggio criteri scelti nella GUI
	 * @return risultati aggregati del monitoraggio, oppure null se la richiesta non è valida
	 */
	public Map<String, Object> monitoraAndamentoAttivita(
			Long idUtente,
			Map<String, Object> criteriMonitoraggio
	) {
		try {
			return controllerMonitoraggioAttivita.generaMonitoraggioAttivita(
					idUtente,
					criteriMonitoraggio
			);
		} catch (IllegalArgumentException e) {
			// RILANCIA L'ECCEZIONE: questo è il punto critico
			throw e;
		} catch (RuntimeException e) {
			System.err.println("[ControllerServiziSistema] Errore durante il monitoraggio attività.");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Richiede l'aggiunta di una nota interna a una segnalazione.
	 *
	 * Il metodo rappresenta il punto di accesso offerto alle Boundary.
	 * La logica specifica viene delegata al ControllerNotaInterna.
	 *
	 * Il Facade Controller non crea NotaInterna e non modifica direttamente
	 * Segnalazione.
	 *
	 * @param idUtente identificativo dell'operatore autenticato
	 * @param idSegnalazione identificativo della segnalazione selezionata
	 * @param testoNota testo della nota interna
	 * @return true se la nota viene inserita correttamente, false altrimenti
	 */
	public boolean aggiungiNotaInterna(
			Long idUtente,
			Long idSegnalazione,
			String testoNota
	) {

		if (idUtente == null || idSegnalazione == null || isVuoto(testoNota)) {
			return false;
		}

		try {
			return controllerNotaInterna.richiediAggiuntaNotaSegnalazione(
					idUtente,
					idSegnalazione,
					testoNota
			);

		} catch (RuntimeException e) {
			System.err.println("[ControllerServiziSistema] Errore durante l'aggiunta della nota interna.");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Verifica se una stringa è nulla o vuota.
	 *
	 * Metodo privato di supporto del Facade Controller per evitare
	 * chiamate ai controller specifici con parametri testuali non validi.
	 *
	 * @param valore stringa da verificare
	 * @return true se nulla o vuota, false altrimenti
	 */
	private boolean isVuoto(String valore) {
		return valore == null || valore.trim().isEmpty();
	}
}