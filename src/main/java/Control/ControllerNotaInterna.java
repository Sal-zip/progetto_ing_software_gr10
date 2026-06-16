package Control;

import Entity.ServizioSegnalazioni;
import Entity.ServizioUtenti;

/**
 * Controller applicativo dedicato all'inserimento di una nota interna.
 *
 * La nota interna non è modellata come caso d'uso autonomo principale,
 * ma come comportamento opzionale attivabile nel ramo Operatore Comunale
 * della visualizzazione del dettaglio di una segnalazione ricevuta.
 *
 * Il controller coordina il flusso applicativo senza accedere direttamente
 * alla persistenza e senza manipolare direttamente le Entity.
 *
 * Responsabilità principali:
 * - coordinare la richiesta di aggiunta nota;
 * - verificare che l'utente sia un Operatore Comunale tramite ServizioUtenti;
 * - delegare a ServizioSegnalazioni la registrazione della nota interna;
 * - restituire l'esito dell'operazione al Facade Controller.
 *
 * La classe è coerente con il modello BCED perché:
 * - non comunica direttamente con la Boundary;
 * - non accede direttamente al package Database;
 * - non usa ServizioPersistenza;
 * - non crea direttamente NotaInterna;
 * - non modifica direttamente Segnalazione;
 * - non attraversa oggetti contenuti;
 * - usa ServizioUtenti solo per la verifica del ruolo;
 * - usa ServizioSegnalazioni per le operazioni relative alla segnalazione.
 */
public class ControllerNotaInterna {

	private ServizioUtenti servizioUtenti;
	private ServizioSegnalazioni servizioSegnalazioni;

	/**
	 * Costruisce il controller inizializzando i Facade Entity necessari.
	 *
	 * ServizioUtenti viene usato per verificare che l'utente autenticato
	 * sia un Operatore Comunale. ServizioSegnalazioni viene usato per
	 * registrare la nota interna sulla segnalazione selezionata.
	 */
	public ControllerNotaInterna() {
		this.servizioUtenti = new ServizioUtenti();
		this.servizioSegnalazioni = new ServizioSegnalazioni();
	}

	/**
	 * Coordina l'aggiunta di una nota interna associata a una segnalazione.
	 *
	 * Il metodo verifica la validità dei parametri, controlla che l'utente
	 * autenticato sia un Operatore Comunale e delega la registrazione della
	 * nota interna al Facade Entity dedicato alle segnalazioni.
	 *
	 * La nota non viene creata nel controller: la creazione resta incapsulata
	 * nella Segnalazione, coerentemente con il contenimento di NotaInterna.
	 *
	 * @param idUtente identificativo dell'operatore autenticato
	 * @param idSegnalazione identificativo della segnalazione selezionata
	 * @param testoNota testo della nota interna
	 * @return true se la nota viene inserita correttamente, false altrimenti
	 */
	public boolean richiediAggiuntaNotaSegnalazione(
			Long idUtente,
			Long idSegnalazione,
			String testoNota
	) {
		if (idUtente == null || idSegnalazione == null || isVuoto(testoNota)) {
			return false;
		}

		if (!richiediVerificaPermessiNota(idUtente, idSegnalazione)) {
			return false;
		}

		return richiediRegistrazioneNotaInterna(
				idUtente,
				idSegnalazione,
				testoNota
		);
	}

	/**
	 * Verifica che l'utente autenticato possa inserire una nota interna.
	 *
	 * Nel modello attuale la nota interna può essere inserita solo
	 * dall'Operatore Comunale. Il controller verifica il ruolo tramite
	 * ServizioUtenti, senza recuperare direttamente Entity e senza accedere
	 * al Database.
	 *
	 * @param idUtente identificativo dell'utente autenticato
	 * @param idSegnalazione identificativo della segnalazione
	 * @return true se l'utente può inserire la nota, false altrimenti
	 */
	private boolean richiediVerificaPermessiNota(
			Long idUtente,
			Long idSegnalazione
	) {
		if (idUtente == null || idSegnalazione == null) {
			return false;
		}

		return servizioUtenti.verificaUtenteOperatore(idUtente);
	}

	/**
	 * Richiede al Facade Entity dedicato alle segnalazioni la registrazione
	 * della nota interna.
	 *
	 * Il controller non crea direttamente NotaInterna e non modifica
	 * direttamente Segnalazione. La responsabilità di creare la nota resta
	 * nella Segnalazione, mentre ServizioSegnalazioni coordina il recupero
	 * delle Entity necessarie e la persistenza dell'aggiornamento.
	 *
	 * @param idUtente identificativo dell'operatore che inserisce la nota
	 * @param idSegnalazione identificativo della segnalazione
	 * @param testoNota testo della nota interna
	 * @return true se la nota viene registrata correttamente, false altrimenti
	 */
	public boolean richiediRegistrazioneNotaInterna(
			Long idUtente,
			Long idSegnalazione,
			String testoNota
	) {
		if (idUtente == null || idSegnalazione == null || isVuoto(testoNota)) {
			return false;
		}

		return servizioSegnalazioni.registraNotaInterna(
				idUtente,
				idSegnalazione,
				testoNota
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