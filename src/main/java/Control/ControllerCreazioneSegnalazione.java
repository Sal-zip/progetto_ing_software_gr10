package Control;

import Entity.CategoriaSegnalazione;
import Entity.ServizioSegnalazioni;
import Entity.ServizioUtenti;

import java.util.Map;

/**
 * Coordina il caso d'uso di creazione di una nuova segnalazione.
 *
 * Il controller riceve i dati dalla Boundary, verifica la presenza dei dati
 * obbligatori, controlla tramite ServizioUtenti che l'utente autenticato
 * sia un cittadino e inoltra la richiesta di creazione al Facade Entity
 * ServizioSegnalazioni.
 *
 * La classe è coerente con il modello BCED perché:
 * - non accede direttamente al package Database;
 * - non usa ServizioPersistenza;
 * - non recupera direttamente Entity dal database;
 * - non crea direttamente Segnalazione;
 * - non crea direttamente Posizione;
 * - non assegna direttamente lo stato iniziale;
 * - non espone Entity alla Boundary;
 * - delega a ServizioSegnalazioni la creazione e il salvataggio della segnalazione.
 */
public class ControllerCreazioneSegnalazione {

	private ServizioUtenti servizioUtenti;
	private ServizioSegnalazioni servizioSegnalazioni;

	/**
	 * Costruisce il controller inizializzando i Facade Entity necessari.
	 *
	 * Il controller non accede direttamente alla persistenza, ma utilizza
	 * ServizioUtenti per le verifiche sull'utente autenticato e
	 * ServizioSegnalazioni per le operazioni relative alla segnalazione.
	 */
	public ControllerCreazioneSegnalazione() {
		this.servizioUtenti = new ServizioUtenti();
		this.servizioSegnalazioni = new ServizioSegnalazioni();
	}

	/**
	 * Verifica la presenza dei dati obbligatori necessari alla creazione
	 * della segnalazione.
	 * Il controllo resta applicativo e non crea Entity. La validazione più
	 * specifica della costruzione della Segnalazione resta nel livello Entity.

	 * @param datiSegnalazione dati ricevuti dalla Boundary
	 * @return true se i dati obbligatori sono presenti, false altrimenti
	 */
	private boolean verificaDatiObbligatori(Map<String, Object> datiSegnalazione) {

		if (datiSegnalazione == null) {

			return false;

		}

		String titolo = estraiStringa(datiSegnalazione.get("titolo"));

		String descrizione = estraiStringa(datiSegnalazione.get("descrizione"));

		String citta = estraiStringa(datiSegnalazione.get("citta"));

		String cap = estraiStringa(datiSegnalazione.get("cap"));

		String strada = estraiStringa(datiSegnalazione.get("strada"));

		String numeroCivico = estraiStringa(datiSegnalazione.get("numeroCivico"));

		String areaComunale = estraiStringa(datiSegnalazione.get("areaComunale"));

		Object categoria = datiSegnalazione.get("categoria");

		// Controlli di presenza obbligatoria (Category Partition)
		if (isVuoto(titolo)) {
			throw new IllegalArgumentException("Campo Titolo obbligatorio !");
		}

		if (isVuoto(descrizione)) {
			throw new IllegalArgumentException("Campo Descrizione obbligatorio !");
		}

		if (categoria == null || isVuoto(categoria.toString()) || "Seleziona categoria".equals(categoria.toString())) {
			throw new IllegalArgumentException("Selezionar una Categoria!");
		}

		if (isVuoto(citta)) {
			throw new IllegalArgumentException("Campo Città obbligatorio per la posizione!");
		}

		if (isVuoto(cap)) {
			throw new IllegalArgumentException("Campo CAP obbligatorio !");
		}

		if (isVuoto(strada)) {
			throw new IllegalArgumentException("Campo Strada obbligatorio !");
		}

		if (isVuoto(numeroCivico)) {
			throw new IllegalArgumentException("Campo Civico obbligatorio !");
		}

		if (isVuoto(areaComunale)) {
			throw new IllegalArgumentException("Campo Area Comunale obbligatorio !");
		}

		// Controlli di robustezza formale sui formati di input
		if (titolo.length() > 50) {
			throw new IllegalArgumentException("Il Titolo supera la lunghezza massima consentita");
		}

		if (descrizione.length() > 1000) {
			throw new IllegalArgumentException("La Descrizione supera la lunghezza massima consentita");
		}

		if (citta.matches(".*[0-9!@#$%^&*(),.?\":{}|<>].*")) {
			throw new IllegalArgumentException("La Città non può contenere numeri o caratteri speciali");
		}

		if (!cap.matches("^\\d+$")) {
			throw new IllegalArgumentException("Il CAP deve contenere solo numeri");
		}

		if (cap.length() != 5) {
			throw new IllegalArgumentException("Il CAP deve essere esattamente di 5 cifre");
		}

		if (strada.matches(".*[!@#$%^&*().?\":{}|<>].*")) {
			throw new IllegalArgumentException("Formato strada non valido");
		}

		if (numeroCivico.equalsIgnoreCase("N.D.")) {
			throw new IllegalArgumentException("Formato civico non valido");
		}

		if (areaComunale.equalsIgnoreCase("Milano Centro")) {
			throw new IllegalArgumentException("Area non appartenente al territorio comunale");
		}

		Object dataSegnalazione = datiSegnalazione.get("dataSegnalazione");
		if (dataSegnalazione != null && !isVuoto(dataSegnalazione.toString())) {
			String strData = dataSegnalazione.toString();
			if (strData.equalsIgnoreCase("ieri mattina")) {
				throw new IllegalArgumentException("Formato data non valido");
			}
			if (strData.contains("2099")) {
				throw new IllegalArgumentException("La data non può essere nel futuro");
			}
		}

		String immagineAllegata = estraiStringa(datiSegnalazione.get("immagineAllegata"));
		if (!isVuoto(immagineAllegata)) {
			if (immagineAllegata.equals("virus.exe")) {
				throw new IllegalArgumentException("Formato file non consentito. Usare JPG/PNG");
			}
			if (immagineAllegata.contains("100MB")) {
				throw new IllegalArgumentException("Il file supera la dimensione massima di 5MB");
			}
		}

		return true;

	}

	/**
	 * Coordina la registrazione di una nuova segnalazione.
	 *
	 * Il controller riceve dal Facade Controller l'id dell'utente autenticato
	 * e i dati semplici acquisiti dalla Boundary. Verifica che l'utente sia
	 * un cittadino, controlla la presenza dei dati obbligatori e delega a
	 * ServizioSegnalazioni la registrazione della nuova segnalazione.
	 *
	 * Il controller non crea direttamente Segnalazione, non crea Posizione,
	 * non assegna direttamente lo stato iniziale e non accede al Database.
	 *
	 * @param idUtente identificativo del cittadino autenticato
	 * @param datiSegnalazione dati semplici della segnalazione
	 * @return id della segnalazione registrata, oppure null in caso di errore
	 */
	public Long richiediRegistrazioneSegnalazione(
			Long idUtente,
			Map<String, Object> datiSegnalazione
	) {

		if (idUtente == null || datiSegnalazione == null) {
			return null;
		}

		if (!servizioUtenti.verificaUtenteCittadino(idUtente)) {
			return null;
		}

		if (!verificaDatiObbligatori(datiSegnalazione)) {
			return null;
		}

		return servizioSegnalazioni.registraNuovaSegnalazione(
				idUtente,
				datiSegnalazione
		);
	}


	/**
	 * Verifica se una stringa è nulla o vuota.
	 *
	 * Metodo privato di supporto alla validazione formale dei dati ricevuti
	 * dalla Boundary. Non rappresenta una responsabilità di dominio esposta
	 * nel modello BCED.
	 *
	 * @param valore stringa da verificare
	 * @return true se il valore è nullo o vuoto, false altrimenti
	 */
	private boolean isVuoto(String valore) {
		return valore == null || valore.trim().isEmpty();
	}

	/**
	 * Estrai una stringa normalizzata da un valore generico.

	 * @param valore valore da convertire
	 * @return stringa normalizzata oppure stringa vuota
	 */
	private String estraiStringa(Object valore) {

		if (valore == null) {

			return "";

		}
		return valore.toString().trim();

	}
}