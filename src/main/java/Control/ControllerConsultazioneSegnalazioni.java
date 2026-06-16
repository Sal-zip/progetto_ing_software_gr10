package Control;

import Entity.ServizioSegnalazioni;
import Entity.ServizioUtenti;

import java.util.List;
import java.util.Map;

/**
 * Coordina la consultazione delle segnalazioni inviate dal cittadino
 * e delle segnalazioni ricevute dall'operatore comunale.
 *
 * Il controller non accede direttamente al package Database e non manipola
 * direttamente le Entity del dominio. Coordina invece i due Facade Entity
 * introdotti dopo la separazione delle macro-responsabilità:
 * - ServizioUtenti, per verificare il ruolo dell'utente autenticato;
 * - ServizioSegnalazioni, per recuperare i dati relativi alle segnalazioni.
 *
 * La classe è coerente con il modello BCED perché:
 * - non comunica direttamente con la Boundary se non tramite dati semplici;
 * - non accede a ServizioPersistenza;
 * - non recupera direttamente Entity dal database;
 * - non accede direttamente a Segnalazione;
 * - non accede direttamente a Posizione;
 * - non attraversa oggetti contenuti;
 * - non usa getPosizione();
 * - non costruisce manualmente mappe leggendo attributi interni di classi contenute;
 * - delega ai Facade Entity le verifiche e il recupero dei dati;
 * - restituisce alla Boundary solo strutture disaccoppiate in forma Map<String, Object>.
 */
public class ControllerConsultazioneSegnalazioni {

	private ServizioUtenti servizioUtenti;
	private ServizioSegnalazioni servizioSegnalazioni;

	/**
	 * Costruisce il controller inizializzando i Facade Entity necessari.
	 *
	 * Il controller usa ServizioUtenti per verificare il ruolo dell'utente
	 * autenticato e ServizioSegnalazioni per recuperare le segnalazioni
	 * richieste dal caso d'uso.
	 */
	public ControllerConsultazioneSegnalazioni() {
		this.servizioUtenti = new ServizioUtenti();
		this.servizioSegnalazioni = new ServizioSegnalazioni();
	}

	/**
	 * Recupera le segnalazioni inviate dal cittadino autenticato.
	 *
	 * Il metodo coordina il caso d'uso verificando prima che l'id ricevuto
	 * appartenga a un Cittadino. Solo in caso di esito positivo delega a
	 * ServizioSegnalazioni il recupero delle segnalazioni inviate.
	 *
	 * La Boundary riceverà una lista di Map contenenti dati semplici,
	 * senza esposizione diretta delle Entity.
	 *
	 * @param idUtente identificativo dell'utente autenticato
	 * @return elenco delle segnalazioni inviate in forma disaccoppiata,
	 *         oppure null se l'utente non è valido o non è un cittadino
	 */
	public List<Map<String, Object>> recuperaSegnalazioniInviate(Long idUtente) {

		if (idUtente == null) {
			return null;
		}

		if (!servizioUtenti.verificaUtenteCittadino(idUtente)) {
			return null;
		}

		return servizioSegnalazioni.recuperaSegnalazioniInviate(idUtente);
	}

	/**
	 * Recupera le segnalazioni ricevute o gestibili dall'operatore comunale.
	 *
	 * Il metodo coordina il caso d'uso verificando prima che l'id ricevuto
	 * appartenga a un OperatoreComunale. Solo in caso di esito positivo
	 * delega a ServizioSegnalazioni il recupero delle segnalazioni ricevute.
	 *
	 * I criteri di filtro vengono acquisiti dalla Boundary e inoltrati come
	 * Map<String, Object>, senza accesso diretto alle Entity e senza accesso
	 * diretto alla persistenza.
	 *
	 * Con lo State Pattern, eventuali filtri sullo stato devono usare la
	 * chiave "nomeStatoCorrente" e valori testuali come:
	 * - inviata;
	 * - presa_in_carico;
	 * - in_lavorazione;
	 * - risolta.
	 *
	 * @param idUtente identificativo dell'operatore autenticato
	 * @param criteriFiltro criteri opzionali di filtro
	 * @return elenco delle segnalazioni ricevute in forma disaccoppiata,
	 *         oppure null se l'utente non è valido o non è un operatore
	 */
	public List<Map<String, Object>> recuperaSegnalazioniRicevute(
			Long idUtente,
			Map<String, Object> criteriFiltro
	) {

		if (idUtente == null) {
			return null;
		}

		if (!servizioUtenti.verificaUtenteOperatore(idUtente)) {
			return null;
		}

		return servizioSegnalazioni.recuperaSegnalazioniRicevute(
				idUtente,
				criteriFiltro
		);
	}
}