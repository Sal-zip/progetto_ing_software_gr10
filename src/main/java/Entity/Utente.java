package Entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

/**
 * Classe astratta che rappresenta un utente registrato nel sistema.
 *
 * Nel modello BCED, Utente rappresenta la generalizzazione comune
 * dei ruoli specifici Cittadino e OperatoreComunale.
 *
 * La classe contiene solo gli attributi comuni agli utenti registrati:
 * - nome;
 * - cognome;
 * - recapito telefonico;
 * - credenziali.
 *
 * Credenziali è contenuta strettamente in Utente tramite @Embedded.
 * Per rispettare rigorosamente l'information hiding, l'oggetto Credenziali
 * non viene esposto tramite getter pubblico e non viene gestito
 * direttamente dai livelli Boundary o Control.
 */
@Entity
@Table(name = "utenti")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
		name = "ruolo",
		discriminatorType = DiscriminatorType.STRING,
		length = 30
)
public abstract class Utente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_utente")
	private Long idUtente;

	@Column(name = "nome", nullable = false, length = 50)
	private String nome;

	@Column(name = "cognome", nullable = false, length = 50)
	private String cognome;

	@Column(name = "recapito_telefonico", nullable = false, length = 20)
	private String recapitoTelefonico;

	/**
	 * Credenziali personali dell'utente.
	 * <p>
	 * Sono modellate come oggetto incorporato perché non possiedono
	 * un ciclo di vita autonomo rispetto all'utente.
	 * <p>
	 * Non viene definito getCredenziali(), perché esporre l'oggetto
	 * contenuto violerebbe il contenimento stretto e l'information hiding.
	 */
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(
					name = "email",
					column = @Column(
							name = "email",
							nullable = false,
							unique = true,
							length = 100
					)
			),
			@AttributeOverride(
					name = "password",
					column = @Column(
							name = "password",
							nullable = false,
							length = 100
					)
			)
	})
	private Credenziali credenziali;

	/**
	 * Costruttore richiesto da Hibernate/JPA.
	 * <p>
	 * La visibilità protected consente a Hibernate di ricostruire
	 * l'oggetto dalla persistenza, evitando però l'istanziazione diretta
	 * della classe astratta dall'esterno.
	 */
	protected Utente() {
	}

	/**
	 * Costruttore applicativo usato dalle sottoclassi concrete
	 * Cittadino e OperatoreComunale.
	 * <p>
	 * La classe Utente non costruisce autonomamente le Credenziali:
	 * le riceve già create dal package Entity, in fase di registrazione,
	 * e ne mantiene il contenimento.
	 *
	 * @param nome               nome dell'utente
	 * @param cognome            cognome dell'utente
	 * @param recapitoTelefonico recapito telefonico dell'utente
	 * @param email        attributo relativo alla classe credenziali
	 * @param password 		attributo relativo alla classe credenziali
	 */
	protected Utente(String nome,
	                 String cognome,
	                 String recapitoTelefonico,
	                 String email,
	                 String password) {

		if (isVuoto(nome)) {
			throw new IllegalArgumentException("Il nome non può essere vuoto.");
		}

		if (isVuoto(cognome)) {
			throw new IllegalArgumentException("Il cognome non può essere vuoto.");
		}

		if (isVuoto(recapitoTelefonico)) {
			throw new IllegalArgumentException("Il recapito telefonico non può essere vuoto.");
		}

		this.nome = nome.trim();
		this.cognome = cognome.trim();
		this.recapitoTelefonico = recapitoTelefonico.trim();

		/*
		 * Credenziali è contenuta strettamente in Utente.
		 * Per rispettare l'information hiding, viene creata dalla classe
		 * contenitrice e non dal Facade Entity.
		 */
		this.credenziali = new Credenziali(email, password);
	}

	public Long getIdUtente() {
		return this.idUtente;
	}

	public String getNome(){
		return this.nome;
	}

	public String getCognome(){
		return this.cognome;
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