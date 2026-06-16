package Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Value Object che rappresenta le credenziali personali dell'utente.
 *
 * Credenziali è contenuta strettamente in Utente tramite @Embedded
 * e non possiede un ciclo di vita autonomo.
 *
 * In coerenza con il modello BCED e con l'information hiding:
 * - non espone getter pubblici;
 * - non espone setter pubblici;
 * - non espone getter o setter package-private;
 * - non espone direttamente email o password;
 * - non contiene metodi di mapping non modellati;
 * - non contiene metodi di verifica non modellati nel class diagram;
 * - viene creata in modo controllato all'interno del package Entity.
 *
 * La verifica dell'esistenza o della corrispondenza delle credenziali
 * viene gestita dal Facade Entity ServizioUtenti tramite query sui campi
 * embedded, senza esporre l'oggetto Credenziali.
 */
@Embeddable
public class Credenziali {

	@Column(name = "email", nullable = false, unique = true, length = 100)
	private String email;

	@Column(name = "password", nullable = false, length = 100)
	private String password;

	/**
	 * Costruttore richiesto da Hibernate/JPA.
	 *
	 * La visibilità protected consente al provider JPA di ricostruire
	 * l'oggetto dalla persistenza, evitando però la creazione libera
	 * dall'esterno.
	 */
	protected Credenziali() {
	}

	/**
	 * Costruttore applicativo con visibilità package-private.
	 *
	 * Credenziali non deve essere creata da Boundary o Control.
	 * La creazione avviene all'interno del package Entity, ad esempio
	 * durante la registrazione di un nuovo Utente.
	 *
	 * @param email email scelta dall'utente
	 * @param password password scelta dall'utente
	 */
	Credenziali(String email, String password) {

		if (isVuoto(email)) {
			throw new IllegalArgumentException("L'email non può essere vuota.");
		}

		if (isVuoto(password)) {
			throw new IllegalArgumentException("La password non può essere vuota.");
		}

		this.email = email.trim();
		this.password = password;
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
}
