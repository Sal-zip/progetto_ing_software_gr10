package Control;

import Entity.ServizioSegnalazioni;

/**
 * Coordina la generazione della notifica al cittadino in seguito
 * all'aggiornamento dello stato di una segnalazione.
 *
 * Il controller non crea direttamente la Notifica, non accede al Database
 * e non manipola direttamente la Segnalazione o il Cittadino segnalante.
 *
 * Dopo la separazione dei Facade Entity, la generazione della notifica
 * viene delegata a ServizioSegnalazioni, perché l'operazione riguarda
 * una segnalazione aggiornata, il cittadino destinatario e lo stato
 * raggiunto dalla segnalazione.
 *
 * La classe è coerente con il modello BCED perché:
 * - non comunica direttamente con la Boundary;
 * - non accede direttamente al package Database;
 * - non usa ServizioPersistenza;
 * - non crea direttamente Notifica;
 * - non recupera direttamente Segnalazione;
 * - non recupera direttamente Cittadino;
 * - non attraversa associazioni o oggetti contenuti;
 * - delega al Facade Entity la generazione e la registrazione della notifica.
 *
 * Dopo l'introduzione del pattern State:
 * - StatoSegnalazione non viene usato come enum;
 * - il controller riceve solo il nome testuale dello stato raggiunto;
 * - il controllo condizionale sulla notificabilità viene richiesto esplicitamente
 * al Facade Entity prima di procedere con l'invio materiale.
 */
public class ControllerNotifiche {

	private ServizioSegnalazioni servizioSegnalazioni;

	/**
	 * Costruisce il controller inizializzando il Facade Entity dedicato
	 * alle operazioni sulle segnalazioni.
	 *
	 * ServizioSegnalazioni viene usato per generare l'eventuale notifica
	 * di cambio stato, mantenendo il controller disaccoppiato dalle Entity
	 * e dalla persistenza.
	 */
	public ControllerNotifiche() {
		this.servizioSegnalazioni = new ServizioSegnalazioni();
	}

	/**
	 * Richiede l'eventuale invio della notifica al cittadino previa verifica
	 * della notificabilità dello stato attuale tramite il Facade Entity.
	 *
	 * Rappresenta il punto di ingresso del flusso di notifica invocato dal
	 * controller del caso d'uso principale.
	 *
	 * @param idSegnalazione identificativo della segnalazione aggiornata
	 * @param nuovoStato nome dello stato raggiunto dalla segnalazione
	 */
	public void verificaStatoNotificabile(
			Long idSegnalazione,
			String nuovoStato
	) {

		if (idSegnalazione == null || isVuoto(nuovoStato)) {
			return;
		}

		// Sfruttiamo il tipo primitivo boolean (evita potenziali NullPointerException rispetto a Boolean wrapper)
		boolean esito = servizioSegnalazioni.verificaStatoNotificabile(
				idSegnalazione,
				nuovoStato
		);

		// Condizione pulita e idiomatica in Java (evitiamo il confronto esplicito == true)
		if (esito) {
			inviaNotificaAlCittadino(idSegnalazione, nuovoStato);
		}
	}

	/**
	 * Richiede l'invio materiale della notifica al cittadino.
	 *
	 * Il controller riceve l'identificativo della segnalazione aggiornata
	 * e il nome testuale del nuovo stato raggiunto, delegando la persistenza
	 * dell'oggetto Notifica al Facade Entity.
	 *
	 * @param idSegnalazione identificativo della segnalazione aggiornata
	 * @param nuovoStato nome testuale dello stato raggiunto dalla segnalazione
	 * @return true se la notifica viene generata correttamente, false altrimenti
	 */
	public boolean inviaNotificaAlCittadino(
			Long idSegnalazione,
			String nuovoStato
	) {

		if (idSegnalazione == null || isVuoto(nuovoStato)) {
			return false;
		}

		Long idNotifica = servizioSegnalazioni.generaNotificaCambioStato(
				idSegnalazione,
				nuovoStato
		);

		return idNotifica != null;
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