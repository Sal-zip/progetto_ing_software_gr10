package Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Value Object che rappresenta la posizione geografica associata
 * a una Segnalazione.
 *
 * Posizione è contenuta strettamente in Segnalazione e non possiede
 * un ciclo di vita autonomo.
 *
 * In coerenza con il modello BCED e con l'information hiding rigoroso:
 * - non espone getter pubblici;
 * - non espone getter package-private;
 * - non espone setter pubblici;
 * - non espone setter package-private;
 * - non espone metodi di mapping non modellati;
 * - non espone metodi di interrogazione non modellati;
 * - non viene restituita direttamente dalla classe Segnalazione;
 * - viene costruita in modo controllato all'interno del package Entity.
 *
 * La lettura o la modifica dei dati della posizione non deve avvenire
 * attraversando direttamente Posizione, ma deve restare sotto il controllo
 * della classe contenitrice e del Facade Entity, nel rispetto delle
 * operazioni previste dal modello BCED.
 */
@Embeddable
public class Posizione {

	@Column(name = "citta", nullable = false, length = 80)
	private String citta;

	@Column(name = "cap", nullable = false, length = 5)
	private String cap;

	@Column(name = "strada", nullable = false, length = 100)
	private String strada;

	@Column(name = "numero_civico", nullable = false, length = 10)
	private String numeroCivico;

	@Column(name = "area_comunale", nullable = false, length = 80)
	private String areaComunale;

	/**
	 * Costruttore richiesto da Hibernate/JPA.
	 *
	 * La visibilità protected consente al provider JPA di ricostruire
	 * l'oggetto dalla persistenza, evitando però la creazione libera
	 * dall'esterno.
	 */
	protected Posizione() {
	}

	/**
	 * Costruttore applicativo con visibilità package-private.
	 *
	 * Posizione non deve essere creata direttamente da Boundary o Control.
	 * La sua creazione resta confinata al package Entity, durante la
	 * costruzione controllata di una Segnalazione.
	 *
	 * @param citta città della segnalazione
	 * @param cap codice di avviamento postale
	 * @param strada strada della segnalazione
	 * @param numeroCivico numero civico della segnalazione
	 * @param areaComunale area comunale o zona di riferimento
	 */
	Posizione(String citta,
	          String cap,
	          String strada,
	          String numeroCivico,
	          String areaComunale) {

		if (isVuoto(citta)) {
			throw new IllegalArgumentException("La città non può essere vuota.");
		}

		if (isVuoto(cap)) {
			throw new IllegalArgumentException("Il CAP non può essere vuoto.");
		}

		if (isVuoto(strada)) {
			throw new IllegalArgumentException("La strada non può essere vuota.");
		}

		if (isVuoto(numeroCivico)) {
			throw new IllegalArgumentException("Il numero civico non può essere vuoto.");
		}

		if (isVuoto(areaComunale)) {
			throw new IllegalArgumentException("L'area comunale non può essere vuota.");
		}

		this.citta = citta.trim();
		this.cap = cap.trim();
		this.strada = strada.trim();
		this.numeroCivico = numeroCivico.trim();
		this.areaComunale = areaComunale.trim();
	}

	/**
	 * Metodo privato di supporto alla validazione interna.
	 *
	 * Non rappresenta un'operazione di dominio esposta nel BCED,
	 * quindi resta confinato all'interno della classe.
	 */
	private boolean isVuoto(String valore) {
		return valore == null || valore.trim().isEmpty();
	}

	// Metodi di get per recuperare i valori degli attributi della posizione, definiti con visibilità di package
	// saranno utilizzati effettivamente come public dalla classe segnalazione

	 String getAreaComunale() {
		return this.areaComunale;
	}

	 String getCap() {
		return this.cap;
	}

	 String getCitta() {
		return this.citta;
	}

	 String getNumeroCivico() {
		return this.numeroCivico;
	}

	 String getStrada() {
		return this.strada;
	}
}