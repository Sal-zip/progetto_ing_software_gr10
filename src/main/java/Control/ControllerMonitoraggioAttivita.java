package Control;

import Entity.ServizioSegnalazioni;
import Entity.ServizioUtenti;

import java.util.Date;
import java.util.Map;

/**
 * Controller applicativo dedicato al monitoraggio dell'andamento delle attività.
 *
 * Il controller coordina il caso d'uso di monitoraggio senza accedere
 * direttamente alla persistenza e senza manipolare direttamente le Entity.
 *
 * La classe è coerente con il modello BCED perché:
 * - non comunica direttamente con la Boundary;
 * - riceve dati semplici dal Facade Controller;
 * - non accede direttamente al package Database;
 * - delega ai Facade Entity la logica di business ed il recupero dati.
 */
public class ControllerMonitoraggioAttivita {

	private ServizioUtenti servizioUtenti;
	private ServizioSegnalazioni servizioSegnalazioni;

	/**
	 * Costruisce il controller inizializzando i Facade Entity necessari.
	 */
	public ControllerMonitoraggioAttivita() {
		this.servizioUtenti = new ServizioUtenti();
		this.servizioSegnalazioni = new ServizioSegnalazioni();
	}

	/**
	 * Metodo principale che coordina la generazione del monitoraggio attività.
	 *
	 * @param idUtente identificativo dell'operatore autenticato
	 * @param criteriMonitoraggio criteri scelti nella GUI
	 * @return risultati aggregati del monitoraggio, oppure null se la richiesta non è valida
	 * @throws IllegalArgumentException se i criteri inseriti violano le regole di business
	 */
	public Map<String, Object> generaMonitoraggioAttivita(
			Long idUtente,
			Map<String, Object> criteriMonitoraggio
	) {
		// Controllo preliminare di validità dei parametri di input
		if (idUtente == null || criteriMonitoraggio == null) {
			return null;
		}

		// 1. Verifica dell'autorizzazione dell'utente tramite il sotto-metodo dedicato
		if (!verificaAutorizzazioneOperatore(idUtente)) {
			System.err.println("[ControllerMonitoraggioAttivita] Accesso negato: l'utente " + idUtente + " non è un operatore comunale.");
			return null;
		}

		// 2. Validazione approfondita dei criteri temporali (solleva IllegalArgumentException attesa dal Facade)
		validaCriteri(criteriMonitoraggio);

		// 3. Smistamento ed elaborazione in base al tipo di monitoraggio richiesto
		String tipoMonitoraggio = (String) criteriMonitoraggio.get("tipoMonitoraggio");

		if ("inLavorazione".equals(tipoMonitoraggio)) {
			return elaboraMonitoraggioInLavorazione(idUtente, criteriMonitoraggio);
		} else if ("riepilogoZona".equals(tipoMonitoraggio)) {
			return elaboraMonitoraggioRiepilogoZona(idUtente, criteriMonitoraggio);
		} else {
			return elaboraMonitoraggioGenerale(idUtente, criteriMonitoraggio);
		}
	}

	/**
	 * Verifica se l'utente identificato ha i privilegi di Operatore Comunale.
	 *
	 * @param idUtente identificativo dell'utente da verificare
	 * @return true se l'utente è autorizzato, false altrimenti
	 */
	public boolean verificaAutorizzazioneOperatore(Long idUtente) {
		if (idUtente == null) {
			return false;
		}
		// Richiama il metodo corretto presente su ServizioUtenti.java
		return servizioUtenti.verificaUtenteOperatore(idUtente);
	}

	/**
	 * Esegue la validazione dei criteri inseriti, applicando le regole di business temporali.
	 * Il metodo lancia un'eccezione IllegalArgumentException che viene catturata e rilanciata
	 * dal Facade Controller verso la Boundary.
	 *
	 * @param criteriMonitoraggio la mappa contenente i parametri inseriti
	 * @throws IllegalArgumentException in caso di violazione delle regole di business
	 */
	public void validaCriteri(Map<String, Object> criteriMonitoraggio) {
		if (criteriMonitoraggio == null) {
			throw new IllegalArgumentException("I criteri di monitoraggio non possono essere nulli.");
		}

		Date dInizio = (Date) criteriMonitoraggio.get("dataInizio");
		Date dFine = (Date) criteriMonitoraggio.get("dataFine");
		Date oggi = new Date();

		if (dInizio == null || dFine == null) {
			throw new IllegalArgumentException("Inserisci entrambe le date nel formato gg-mm-aaaa.");
		}

		// Validazione: Data Inizio futura rispetto ad oggi
		if (dInizio.after(oggi)) {
			throw new IllegalArgumentException("La data di inizio non può essere successiva alla data odierna.");
		}

		// Validazione: Data Fine futura rispetto ad oggi
		if (dFine.after(oggi)) {
			throw new IllegalArgumentException("La data di fine non può essere nel futuro.");
		}

		// Validazione: Intervallo Inizio rovesciato (Data Inizio > Data Fine)
		if (dInizio.after(dFine)) {
			throw new IllegalArgumentException("La data di inizio deve essere antecedente o uguale alla data di fine.");
		}
	}

	/**
	 * Gestisce lo scenario di monitoraggio generale delle attività nell'intervallo scelto.
	 *
	 * @param idUtente identificativo dell'operatore comunale
	 * @param criteri mappa dei criteri di monitoraggio
	 * @return mappa dei risultati aggregati generati dal metodo corretto 'generaRisultatiMonitoraggio'
	 */
	public Map<String, Object> elaboraMonitoraggioGenerale(Long idUtente, Map<String, Object> criteri) {
		return servizioSegnalazioni.generaRisultatiMonitoraggio(idUtente, criteri);
	}

	/**
	 * Gestisce lo scenario opzionale relativo alle segnalazioni attualmente in lavorazione.
	 *
	 * @param idUtente identificativo dell'operatore comunale
	 * @param criteri mappa dei criteri di monitoraggio
	 * @return mappa dei risultati aggregati generati dal metodo corretto 'generaRisultatiMonitoraggio'
	 */
	public Map<String, Object> elaboraMonitoraggioInLavorazione(Long idUtente, Map<String, Object> criteri) {
		return servizioSegnalazioni.generaRisultatiMonitoraggio(idUtente, criteri);
	}

	/**
	 * Gestisce lo scenario alternativo di vista riepilogativa e monitoraggio per area comunale/zona.
	 *
	 * @param idUtente identificativo dell'operatore comunale
	 * @param criteri mappa dei criteri contenente l'area comunale specificata
	 * @return mappa dei risultati aggregati generati dal metodo corretto 'generaRisultatiMonitoraggio'
	 */
	public Map<String, Object> elaboraMonitoraggioRiepilogoZona(Long idUtente, Map<String, Object> criteri) {
		return servizioSegnalazioni.generaRisultatiMonitoraggio(idUtente, criteri);
	}
}