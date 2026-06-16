package Entity;

import Database.ServizioPersistenza;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ServizioUtenti svolge il ruolo di Facade Entity dedicato alla gestione
 * degli utenti del sistema.
 *
 * La classe espone verso il livello Control solo le operazioni relative a:
 * - registrazione utente;
 * - verifica dell'esistenza di un utente registrato;
 * - verifica del ruolo applicativo dell'utente;
 * - recupero dei dati semplici di sessione a partire dalle credenziali.
 *
 * La classe è coerente con l'architettura BCED perché:
 * - non comunica direttamente con le Boundary;
 * - non gestisce componenti grafici;
 * - non accede direttamente ai Controller;
 * - incapsula l'accesso alla persistenza tramite ServizioPersistenza;
 * - non espone oggetti conte	nuti come Credenziali;
 * - non restituisce istanze Entity alla Boundary;
 * - restituisce al livello Control solo dati semplici quando richiesto.
 *
 * Dopo la separazione dei Facade Entity, le responsabilità relative a
 * Segnalazione, Stato, NotaInterna, Notifica, AggiornamentoStato e
 * monitoraggio devono essere collocate in ServizioSegnalazioni.
 */
public class ServizioUtenti {

	private ServizioPersistenza servizioPersistenza;

	/**
	 * Costruisce il Facade Entity dedicato agli utenti.
	 *
	 * Il Facade utilizza ServizioPersistenza come punto di accesso controllato
	 * al package Database, senza esporre la persistenza ai Controller.
	 */
	public ServizioUtenti() {
		this.servizioPersistenza = new ServizioPersistenza();
	}

	/**
	 * Verifica se un utente risulta già registrato nel sistema.
	 *
	 * La verifica viene effettuata sulla base dell'email contenuta nei dati
	 * di registrazione. Poiché Credenziali è un oggetto embedded e contenuto
	 * in Utente, la ricerca avviene tramite il path JPA "credenziali.email",
	 * senza esporre l'oggetto Credenziali tramite getter pubblici.
	 *
	 * @param datiRegistrazione mappa contenente i dati acquisiti dalla GUI di registrazione
	 * @return true se l'utente risulta già registrato, false altrimenti
	 */
	public boolean verificaUtenteRegistrato(Map<String, Object> datiRegistrazione) {

		if (datiRegistrazione == null) {
			return false;
		}

		return !verificaDisponibilitaCredenziali(datiRegistrazione);
	}

	/**
	 * Verifica se l'utente identificato dall'id indicato è un Cittadino.
	 *
	 * Il metodo viene usato dai Controller specifici come controllo
	 * applicativo prima di eseguire casi d'uso riservati al cittadino,
	 * ad esempio la creazione di una nuova segnalazione.
	 *
	 * La verifica non viene demandata alla Boundary: la dashboard può usare
	 * il ruolo di sessione solo per mostrare o nascondere pulsanti, mentre
	 * l'autorizzazione effettiva resta nel livello Control/Entity.
	 *
	 * @param idUtente identificativo dell'utente autenticato
	 * @return true se l'utente è un Cittadino, false altrimenti
	 */
	public boolean verificaUtenteCittadino(Long idUtente) {

		if (idUtente == null) {
			return false;
		}

		Cittadino cittadino = servizioPersistenza.trovaPerId(
				Cittadino.class,
				idUtente
		);

		return cittadino != null;
	}

	/**
	 * Verifica se l'utente identificato dall'id indicato è un OperatoreComunale.
	 *
	 * Il metodo viene usato dai Controller specifici come controllo
	 * applicativo prima di eseguire casi d'uso riservati all'operatore,
	 * ad esempio aggiornamento stato, nota interna, consultazione ricevute
	 * e monitoraggio.
	 *
	 * @param idUtente identificativo dell'utente autenticato
	 * @return true se l'utente è un OperatoreComunale, false altrimenti
	 */
	public boolean verificaUtenteOperatore(Long idUtente) {

		if (idUtente == null) {
			return false;
		}

		OperatoreComunale operatore = servizioPersistenza.trovaPerId(
				OperatoreComunale.class,
				idUtente
		);

		return operatore != null;
	}

	/**
	 * Registra un nuovo utente nel sistema.
	 *
	 * Il metodo riceve dati semplici dalla catena Boundary → Control e crea
	 * l'istanza concreta corretta del dominio, cioè Cittadino oppure
	 * OperatoreComunale.
	 *
	 * Le Credenziali non vengono create direttamente dal Facade: il Facade
	 * passa email e password al costruttore dell'utente concreto, mentre la
	 * creazione dell'oggetto embedded Credenziali resta responsabilità interna
	 * della classe Utente.
	 *
	 * @param datiRegistrazione mappa contenente nome, cognome, recapito, email, password e ruolo
	 * @return id dell'utente registrato, oppure null se la registrazione fallisce
	 */
	public Long registraUtente(Map<String, Object> datiRegistrazione) {

		if (datiRegistrazione == null) {
			return null;
		}

		if (verificaUtenteRegistrato(datiRegistrazione)) {
			return null;
		}

		try {
			Utente utente = creaUtenteDaDatiRegistrazione(datiRegistrazione);

			if (utente == null) {
				return null;
			}

			servizioPersistenza.salva(utente);

			return utente.getIdUtente();

		} catch (RuntimeException e) {
			System.err.println("[ServizioUtenti] Errore durante la registrazione utente: "
					+ e.getMessage());
			return null;
		}
	}

	/**
	 * Recupera i dati semplici dell'utente associato alle credenziali inserite.
	 *
	 * Il metodo riceve dal ControllerAccessoUtente una Map contenente email e
	 * password. La verifica dell'esistenza dell'utente viene effettuata tramite
	 * ricerca sui campi embedded di Credenziali:
	 * - credenziali.email;
	 * - credenziali.password.
	 *
	 * Credenziali è un oggetto contenuto in Utente e non viene esposto tramite
	 * getter pubblici. Il metodo non restituisce l'istanza Entity Utente alla
	 * Boundary, ma solo una Map con dati semplici di sessione.
	 *
	 * La Map restituita contiene:
	 * - idUtente;
	 * - nome;
	 * - cognome;
	 * - ruolo.
	 *
	 * @param datiAccesso mappa contenente email e password inserite nella GUI di accesso
	 * @return mappa dei dati semplici di sessione, oppure null se le credenziali non sono valide
	 */
	public Map<String, Object> recuperaDatiUtenteDaCredenziali(Map<String, Object> datiAccesso) {

		if (datiAccesso == null) {
			return null;
		}

		String email = estraiStringa(datiAccesso.get("email"));
		String password = estraiStringa(datiAccesso.get("password"));

		if (isVuoto(email) || isVuoto(password)) {
			return null;
		}

		/*
		 * Credenziali è @Embeddable e contenuta in Utente.
		 * La ricerca avviene tramite path JPA sui campi embedded, senza esporre
		 * l'oggetto Credenziali e senza chiedere a Utente una verifica pubblica.
		 */
		Map<String, Object> campiRicerca = new HashMap<>();
		campiRicerca.put("credenziali.email", email);
		campiRicerca.put("credenziali.password", password);

		List<Utente> utentiTrovati = servizioPersistenza.cercaPerCampi(
				Utente.class,
				campiRicerca
		);

		if (utentiTrovati == null || utentiTrovati.isEmpty()) {
			return null;
		}

		Utente utenteAutenticato = utentiTrovati.get(0);

		return creaDatiSessioneUtente(utenteAutenticato);
	}

	/**
	 * Verifica la disponibilità delle credenziali in fase di registrazione.
	 *
	 * La disponibilità viene valutata controllando che l'email non sia già
	 * presente tra gli utenti registrati. Credenziali non viene esposto:
	 * la ricerca usa il path JPA dell'attributo embedded.
	 *
	 * @param datiRegistrazione mappa contenente i dati di registrazione
	 * @return true se le credenziali sono disponibili, false altrimenti
	 */
	private boolean verificaDisponibilitaCredenziali(Map<String, Object> datiRegistrazione) {

		if (datiRegistrazione == null) {
			return false;
		}

		String email = estraiStringa(datiRegistrazione.get("email"));

		if (isVuoto(email)) {
			return false;
		}

		return !verificaEmailGiaRegistrata(email);
	}

	/**
	 * Verifica se l'email indicata è già associata a un utente registrato.
	 *
	 * Il metodo usa il path JPA "credenziali.email", poiché Credenziali è
	 * un oggetto embedded contenuto in Utente.
	 *
	 * @param email email da verificare
	 * @return true se l'email è già registrata, false altrimenti
	 */
	private boolean verificaEmailGiaRegistrata(String email) {

		if (isVuoto(email)) {
			return false;
		}

		Map<String, Object> campiRicerca = new HashMap<>();
		campiRicerca.put("credenziali.email", email);

		List<Utente> utentiTrovati = servizioPersistenza.cercaPerCampi(
				Utente.class,
				campiRicerca
		);

		return utentiTrovati != null && !utentiTrovati.isEmpty();
	}

	/**
	 * Crea l'utente concreto a partire dai dati di registrazione.
	 *
	 * Il metodo resta privato perché la scelta della sottoclasse concreta
	 * rappresenta un dettaglio interno del Facade Entity. La Boundary e il
	 * Control passano solo dati semplici, senza creare direttamente Cittadino,
	 * OperatoreComunale o Credenziali.
	 *
	 * @param datiRegistrazione mappa contenente i dati dell'utente da registrare
	 * @return istanza concreta di Utente, oppure null se i dati non sono validi
	 */
	private Utente creaUtenteDaDatiRegistrazione(Map<String, Object> datiRegistrazione) {

		if (datiRegistrazione == null) {
			return null;
		}

		String nome = estraiStringa(datiRegistrazione.get("nome"));
		String cognome = estraiStringa(datiRegistrazione.get("cognome"));
		String recapitoTelefonico = estraiStringa(datiRegistrazione.get("recapitoTelefonico"));
		String email = estraiStringa(datiRegistrazione.get("email"));
		String password = estraiStringa(datiRegistrazione.get("password"));
		String ruolo = estraiStringa(datiRegistrazione.get("ruolo"));

		if (isVuoto(nome)
				|| isVuoto(cognome)
				|| isVuoto(recapitoTelefonico)
				|| isVuoto(email)
				|| isVuoto(password)
				|| isVuoto(ruolo)) {
			return null;
		}

		/*
		 * Credenziali è contenuta in Utente.
		 * ServizioUtenti passa email/password, ma non crea direttamente Credenziali.
		 */
		if ("cittadino".equalsIgnoreCase(ruolo)
				|| "Cittadino".equalsIgnoreCase(ruolo)) {
			return new Cittadino(
					nome,
					cognome,
					recapitoTelefonico,
					email,
					password
			);
		}

		if ("operatore comunale".equalsIgnoreCase(ruolo)
				|| "Operatore Comunale".equalsIgnoreCase(ruolo)
				|| "operatore_comunale".equalsIgnoreCase(ruolo)
				|| "operatore".equalsIgnoreCase(ruolo)) {
			return new OperatoreComunale(
					nome,
					cognome,
					recapitoTelefonico,
					email,
					password
			);
		}

		return null;
	}

	/**
	 * Crea la Map dei dati semplici di sessione dell'utente autenticato.
	 *
	 * Il metodo è privato perché rappresenta un dettaglio interno del Facade
	 * Entity. Espone verso il livello Control solo valori semplici, evitando
	 * il passaggio diretto dell'istanza Entity Utente alla Boundary.
	 *
	 * Il metodo usa solo getter semplici dell'utente, senza accedere a oggetti
	 * contenuti come Credenziali.
	 *
	 * @param utenteAutenticato utente recuperato tramite le credenziali inserite
	 * @return mappa contenente idUtente, nome, cognome e ruolo, oppure null se l'utente non è valido
	 */
	private Map<String, Object> creaDatiSessioneUtente(Utente utenteAutenticato) {

		if (utenteAutenticato == null || utenteAutenticato.getIdUtente() == null) {
			return null;
		}

		String ruolo = recuperaRuoloUtente(utenteAutenticato);

		if (isVuoto(ruolo)) {
			return null;
		}

		Map<String, Object> datiSessioneUtente = new HashMap<>();

		datiSessioneUtente.put("idUtente", utenteAutenticato.getIdUtente());
		datiSessioneUtente.put("nome", utenteAutenticato.getNome());
		datiSessioneUtente.put("cognome", utenteAutenticato.getCognome());
		datiSessioneUtente.put("ruolo", ruolo);

		return datiSessioneUtente;
	}

	/**
	 * Determina il ruolo applicativo dell'utente autenticato.
	 *
	 * Il metodo resta interno al Facade Entity perché la distinzione tra
	 * Cittadino e OperatoreComunale appartiene al dominio e non deve essere
	 * demandata alla Boundary.
	 *
	 * @param utenteAutenticato utente recuperato tramite credenziali
	 * @return ruolo applicativo dell'utente, oppure null se il tipo non è riconosciuto
	 */
	private String recuperaRuoloUtente(Utente utenteAutenticato) {

		if (utenteAutenticato instanceof Cittadino) {
			return "Cittadino";
		}

		if (utenteAutenticato instanceof OperatoreComunale) {
			return "Operatore Comunale";
		}

		return null;
	}

	/**
	 * Converte un valore generico in stringa.
	 *
	 * Il metodo normalizza i dati ricevuti dalle Map provenienti dal livello
	 * Control, mantenendo la gestione della conversione confinata al Facade.
	 *
	 * @param valore valore da convertire
	 * @return stringa corrispondente, oppure null se il valore è nullo
	 */
	private String estraiStringa(Object valore) {

		if (valore == null) {
			return null;
		}

		if (valore instanceof char[]) {
			return new String((char[]) valore);
		}

		return valore.toString().trim();
	}

	/**
	 * Verifica se una stringa è nulla o vuota.
	 *
	 * Metodo privato di supporto alla validazione interna del Facade.
	 *
	 * @param valore stringa da verificare
	 * @return true se il valore è nullo o vuoto, false altrimenti
	 */
	private boolean isVuoto(String valore) {
		return valore == null || valore.trim().isEmpty();
	}
}