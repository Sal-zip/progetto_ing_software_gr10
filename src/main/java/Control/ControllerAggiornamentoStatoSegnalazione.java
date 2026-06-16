package Control;

import Entity.ServizioSegnalazioni;
import Entity.ServizioUtenti;

/**
 * Coordina il caso d'uso di aggiornamento dello stato di una segnalazione.
 *
 * Il controller riceve la richiesta dal Facade Controller, verifica che
 * l'utente autenticato sia un Operatore Comunale, delega l'aggiornamento
 * dello stato al Facade Entity ServizioSegnalazioni e coordina l'eventuale
 * invio della notifica al cittadino.
 *
 * La classe è coerente con il modello BCED perché:
 * - non comunica direttamente con la Boundary;
 * - non accede direttamente al package Database;
 * - non usa ServizioPersistenza;
 * - non manipola direttamente la persistenza;
 * - non modifica direttamente la Segnalazione;
 * - non crea direttamente AggiornamentoStato;
 * - non crea direttamente Notifica;
 * - usa ServizioUtenti solo per la verifica del ruolo;
 * - usa ServizioSegnalazioni per le operazioni relative alla segnalazione;
 * - mantiene la notifica como comportamento coordinato dal caso d'uso.
 *
 * Dopo l'introduzione del pattern State:
 * - StatoSegnalazione non è più un enum, ma un'interfaccia comportamentale;
 * - il controller non riceve il nuovo stato dalla Boundary;
 * - il controller non confronta direttamente gli stati concreti;
 * - la validità della transizione è demandata a Segnalazione e agli stati concreti;
 * - ServizioSegnalazioni restituisce il nome del nuovo stato assunto dalla segnalazione.
 */
public class ControllerAggiornamentoStatoSegnalazione {

	private ServizioUtenti servizioUtenti;
	private ServizioSegnalazioni servizioSegnalazioni;
	private ControllerNotifiche controllerNotifiche;

	/**
	 * Costruisce il controller inizializzando i collaboratori necessari.
	 *
	 * ServizioUtenti viene usato per verificare il ruolo dell'utente
	 * autenticato. ServizioSegnalazioni viene usato per applicare
	 * l'aggiornamento dello stato. ControllerNotifiche viene usato per
	 * coordinare l'eventuale generazione della notifica al cittadino.
	 */
	public ControllerAggiornamentoStatoSegnalazione() {
		this.servizioUtenti = new ServizioUtenti();
		this.servizioSegnalazioni = new ServizioSegnalazioni();
		this.controllerNotifiche = new ControllerNotifiche();
	}

	/**
	 * Coordina la registrazione dell'aggiornamento di stato di una segnalazione.
	 *
	 * Il controller riceve dal Facade Controller l'id dell'operatore autenticato
	 * e l'id della segnalazione selezionata. Verifica che l'utente sia un
	 * operatore comunale, richiede l'applicazione del nuovo stato e, se lo stato
	 * è stato aggiornato correttamente, richiede l'eventuale invio della notifica.
	 *
	 * Il controller non mostra messaggi grafici, non modifica direttamente
	 * Segnalazione, non crea AggiornamentoStato e non accede al Database.
	 *
	 * @param idUtente identificativo dell'operatore comunale autenticato
	 * @param idSegnalazione identificativo della segnalazione da aggiornare
	 * @return true se l'aggiornamento di stato viene completato, false altrimenti
	 */
	public boolean registraAggiornamentoStato(
			Long idUtente,
			Long idSegnalazione
	) {

		if (idUtente == null) {
			throw new IllegalArgumentException("Si prega di effettuare il login.");
		}

		if (idSegnalazione == null) {
			throw new IllegalArgumentException("Seleziona una segnalazione prima di confermare l'aggiornamento.");
		}

		if (!servizioUtenti.verificaUtenteOperatore(idUtente)) {
			throw new IllegalArgumentException("Privilegi insufficienti. Solo un Operatore può aggiornare lo stato.");
		}

		java.util.Map<String, Object> dettaglio = servizioSegnalazioni.recuperaDettaglioSegnalazione(idUtente, idSegnalazione);
		if (dettaglio == null || dettaglio.isEmpty()) {
			throw new IllegalArgumentException("Segnalazione non trovata nel sistema.");
		}

		String statoAttuale = (String) dettaglio.get("statoCorrente");
		if ("risolta".equalsIgnoreCase(statoAttuale)) {
			throw new IllegalArgumentException("Impossibile modificare una segnalazione già risolta e chiusa.");
		}

		String nuovoStato = applicaNuovoStato(
				idUtente,
				idSegnalazione
		);

		if (isVuoto(nuovoStato)) {
			return false;
		}

		/*
		 * La notifica è eventuale: il controller richiede la generazione,
		 * ma la decisione finale sul fatto che lo stato sia notificabile
		 * resta incapsulata nel flusso di notifica / Facade Entity dedicato.
		 *
		 * Un eventuale mancato invio della notifica non annulla
		 * l'aggiornamento di stato già completato.
		 */
		richiediInvioNotifica(
				idSegnalazione,
				nuovoStato
		);

		return true;
	}

	/**
	 * Applica l'aggiornamento di stato delegando l'operazione a ServizioSegnalazioni.
	 *
	 * Il controller non modifica direttamente la Segnalazione e non crea
	 * AggiornamentoStato. La transizione viene applicata internamente
	 * dalla Segnalazione tramite il pattern State.
	 *
	 * ServizioSegnalazioni:
	 * - recupera l'operatore;
	 * - recupera la segnalazione;
	 * - associa l'operatore alla segnalazione;
	 * - invoca segnalazione.aggiornaStato();
	 * - persiste la segnalazione aggiornata;
	 * - restituisce il nuovo stato como String.
	 *
	 * @param idUtente identificativo dell'operatore autenticato
	 * @param idSegnalazione identificativo della segnalazione da aggiornare
	 * @return nome del nuovo stato assunto dalla segnalazione, oppure null se l'aggiornamento fallisce
	 */
	public String applicaNuovoStato(
			Long idUtente,
			Long idSegnalazione
	) {

		if (idUtente == null || idSegnalazione == null) {
			return null;
		}

		return servizioSegnalazioni.aggiornaStatoSegnalazione(
				idUtente,
				idSegnalazione
		);
	}

	/**
	 * Richiede al ControllerNotifiche l'eventuale invio della notifica.
	 *
	 * Il controller di aggiornamento non crea direttamente la Notifica e
	 * non verifica direttamente gli stati notificabili. Inoltra il nome
	 * dello stato raggiunto al controller dedicato, che coordina la
	 * generazione della notifica tramite il Facade Entity.
	 *
	 * @param idSegnalazione identificativo della segnalazione aggiornata
	 * @param nuovoStato nome dello stato raggiunto dalla segnalazione
	 */
	public void richiediInvioNotifica(
			Long idSegnalazione,
			String nuovoStato
	) {

		if (idSegnalazione == null || isVuoto(nuovoStato)) {
			return;
		}

		controllerNotifiche.verificaStatoNotificabile(
				idSegnalazione,
				nuovoStato
		);
	}

	/**
	 * Verifica se una stringa è nulla o vuota.
	 *
	 * Metodo privato di supporto alla validazione interna del controller.
	 * Non rappresenta una responsabilità pubblica di dominio.
	 *
	 * @param valore stringa da verificare
	 * @return true se il valore è nullo o vuoto, false altrimenti
	 */
	private boolean isVuoto(String valore) {
		return valore == null || valore.trim().isEmpty();
	}
}