package Control;

import java.util.Map;
import Entity.ServizioUtenti;

/**
 * Coordina il caso d'uso di registrazione utente.
 *
 * Il controller riceve i dati dalla Boundary, applica i controlli di robustezza
 * e validazione sintattico-semantica in linea con il Piano di Test Funzionale,
 * lanciando eccezioni mirate in caso di errore, e delega al Facade Entity
 * ServizioUtenti la verifica dell'eventuale duplicato e la registrazione effettiva.
 *
 * La classe è coerente con il modello BCED perché:
 * - non comunica direttamente con il Database;
 * - non usa ServizioPersistenza;
 * - non crea direttamente Utente, Cittadino, OperatoreComunale o Credenziali;
 * - non accede all'oggetto contenuto Credenziali;
 * - delega al package Entity la creazione dell'utente e delle credenziali;
 * - mantiene nel livello Control solo il coordinamento del caso d'uso.
 */
public class ControllerRegistrazioneUtente {

	private ServizioUtenti servizioUtenti;

	/**
	 * Costruisce il controller inizializzando il Facade Entity.
	 */
	public ControllerRegistrazioneUtente() {
		this.servizioUtenti = new ServizioUtenti();
	}

	/**
	 * Coordina la richiesta di registrazione di un nuovo utente.
	 *
	 * Il metodo valida i dati lanciando eccezioni specifiche per ogni violazione dei
	 * criteri di accettazione dei campi. Se i dati sono conformi, verifica che
	 * l'utente non sia già registrato prima di persistere l'Entity.
	 *
	 * @param datiRegistrazione dati semplici inseriti nella GUI di registrazione
	 * @return id dell'utente registrato in caso di successo
	 * @throws IllegalArgumentException se uno dei controlli sul formato o sulla presenza dei dati fallisce
	 */
	public Long richiediRegistrazioneUtente(Map<String, Object> datiRegistrazione) {

		if (datiRegistrazione == null) {
			throw new IllegalArgumentException("Dati di registrazione nulli.");
		}

		// Esegue la catena di validazione stringente legata al Piano di Test
		verificaDatiRegistrazione(datiRegistrazione);

		// Controllo di unicità sul Database (Mappato sul TC_11)
		if (servizioUtenti.verificaUtenteRegistrato(datiRegistrazione)) {
			throw new IllegalArgumentException("L'indirizzo Email risulta già registrato");
		}

		return servizioUtenti.registraUtente(datiRegistrazione);
	}

	/**
	 * Verifica la correttezza formale e la robustezza dei dati inseriti.
	 *
	 * I controlli applicati riflettono puntualmente le classi di equivalenza
	 * e gli output attesi definiti mediante la tecnica Category Partition.
	 *
	 * @param datiRegistrazione mappa contenente i dati acquisiti dalla Boundary
	 * @return true se tutti i vincoli sono superati con successo
	 * @throws IllegalArgumentException contenente il messaggio d'errore specifico atteso in output
	 */
	public boolean verificaDatiRegistrazione(Map<String, Object> datiRegistrazione) {

		if (datiRegistrazione == null) {
			return false;
		}

		String nome = valoreTestuale(datiRegistrazione.get("nome"));
		String cognome = valoreTestuale(datiRegistrazione.get("cognome"));
		String email = valoreTestuale(datiRegistrazione.get("email"));
		String password = valoreTestuale(datiRegistrazione.get("password"));
		String confermaPassword = valoreTestuale(datiRegistrazione.get("confermaPassword"));
		String recapitoTelefonico = valoreTestuale(datiRegistrazione.get("recapitoTelefonico"));
		String ruolo = valoreTestuale(datiRegistrazione.get("ruolo"));

		// 1. VERIFICA PRESENZA CAMPI OBBLIGATORI (Mappatura campi vuoti)
		if (isVuoto(nome)) throw new IllegalArgumentException("Il campo Nome è obbligatorio");
		if (isVuoto(cognome)) throw new IllegalArgumentException("Il campo Cognome è obbligatorio");
		if (isVuoto(email)) throw new IllegalArgumentException("Il campo Email è obbligatorio");
		if (isVuoto(recapitoTelefonico)) throw new IllegalArgumentException("Il campo Recapito Telefonico è obbligatorio");
		if (isVuoto(password)) throw new IllegalArgumentException("Il campo Password è obbligatorio");
		if (isVuoto(confermaPassword)) throw new IllegalArgumentException("Il campo Conferma Password è obbligatorio");
		if (isVuoto(ruolo)) throw new IllegalArgumentException("Selezionare il proprio ruolo dal menù");

		// 2. VERIFICA CORRISPONDENZA PASSWORD
		if (!password.equals(confermaPassword)) {
			throw new IllegalArgumentException("Le password inserite non coincidono.");
		}

		// 3. CONTROLLI DI FORMATO E ROBUSTEZZA (Category Partition)

		// Nome: Lunghezza massima 50 caratteri, caratteri alfabetici, iniziale maiuscola
		if (nome.length() > 50 || nome.matches(".*[0-9!@#$%^&*(),.?\":{}|<>].*")) {
			throw new IllegalArgumentException("Formato Nome non valido o troppo lungo");
		}
		if (!Character.isUpperCase(nome.charAt(0))) {
			throw new IllegalArgumentException("Il Nome deve iniziare con una lettera maiuscola");
		}

		// Cognome: Lunghezza massima 50 caratteri, caratteri alfabetici, iniziale maiuscola
		if (cognome.length() > 50 || cognome.matches(".*[0-9!@#$%^&*(),.?\":{}|<>].*")) {
			throw new IllegalArgumentException("Formato Cognome non valido o troppo lungo");
		}
		if (!Character.isUpperCase(cognome.charAt(0))) {
			throw new IllegalArgumentException("Il Cognome deve iniziare con una lettera maiuscola");
		}

		// Email: Presenza della @, assenza di spazi intermedi e pattern standard sintattico
		if (!email.contains("@")) {
			throw new IllegalArgumentException("Formato Email non valido");
		}
		if (email.contains(" ")) {
			throw new IllegalArgumentException("L'Email non può contenere spazi");
		}
		if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
			throw new IllegalArgumentException("Formato Email non valido");
		}

		// Recapito Telefonico: Solo cifre numeriche e lunghezza fissa pari a 10
		if (!recapitoTelefonico.matches("^\\d+$")) {
			throw new IllegalArgumentException("Il recapito deve contenere solo numeri");
		}
		if (recapitoTelefonico.length() != 10) {
			throw new IllegalArgumentException("La lunghezza del recapito non è valida");
		}

		// Password: Almeno 8 caratteri complessivi, contenente maiuscole, numeri e simboli
		if (password.length() < 8) {
			throw new IllegalArgumentException("La password deve contenere almeno 8 caratteri");
		}
		boolean haMaiuscola = password.matches(".*[A-Z].*");
		boolean haNumero = password.matches(".*\\d.*");
		boolean haSimbolo = password.matches(".*[^a-zA-Z0-9].*");
		if (!haMaiuscola || !haNumero || !haSimbolo) {
			throw new IllegalArgumentException("La password deve contenere numeri, maiuscole e simboli");
		}

		return true;
	}

	/**
	 * Converte un valore generico in una stringa normalizzata.
	 */
	private String valoreTestuale(Object valore) {
		if (valore == null) {
			return "";
		}
		return valore.toString().trim();
	}

	/**
	 * Verifica se una stringa è nulla o vuota.
	 */
	private boolean isVuoto(String valore) {
		return valore == null || valore.trim().isEmpty();
	}
}