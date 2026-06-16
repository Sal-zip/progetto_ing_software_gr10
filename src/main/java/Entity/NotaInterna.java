package Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;

/**
 * Entity che rappresenta una nota interna associata a una Segnalazione.
 *
 * Nel modello BCED, NotaInterna è contenuta strettamente in Segnalazione.
 * Per questo motivo il suo ciclo di vita deve rimanere sotto il controllo
 * della classe contenitrice.
 *
 * La classe non espone getter o setter, perché non deve essere trattata
 * come una struttura dati interrogabile o modificabile liberamente
 * dall'esterno.
 *
 * La creazione della nota interna deve avvenire tramite il metodo modellato
 * nella classe Segnalazione, ad esempio Segnalazione.aggiungiNotaInterna(...),
 * che mantiene il controllo sul contenimento.
 */
@Entity
@Table(name = "note_interne")
public class NotaInterna {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_nota")
	private Long idNota;

	/**
	 * Testo della nota interna inserita dall'operatore comunale.
	 */
	@Column(name = "testo", nullable = false, length = 1000)
	private String testo;

	/**
	 * Data e ora di inserimento della nota interna.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "data_inserimento", nullable = false)
	private Date dataInserimento;

	/**
	 * Operatore comunale che ha inserito la nota.
	 *
	 * Non viene usato cascade perché NotaInterna non controlla
	 * il ciclo di vita dell'OperatoreComunale.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_operatore", nullable = false)
	private OperatoreComunale operatore;

	/**
	 * Segnalazione contenitrice della nota interna.
	 *
	 * Il contenimento stretto viene gestito dal lato Segnalazione tramite
	 * una relazione @OneToMany con cascade e orphanRemoval.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_segnalazione", nullable = false)
	private Segnalazione segnalazione;

	/**
	 * Costruttore richiesto da Hibernate/JPA.
	 *
	 * La visibilità protected permette al provider JPA di ricostruire
	 * l'oggetto dalla persistenza, evitando però la creazione libera
	 * dall'esterno.
	 */
	protected NotaInterna() {
	}

	/**
	 * Costruttore applicativo con visibilità package-private.
	 *
	 * La nota interna non deve essere creata da Boundary o Control.
	 * La sua creazione deve rimanere confinata al package Entity e,
	 * in particolare, deve essere orchestrata dalla classe contenitrice
	 * Segnalazione.
	 *
	 * @param testo testo della nota interna
	 * @param dataInserimento data di inserimento della nota
	 * @param segnalazione segnalazione a cui la nota appartiene
	 * @param operatore operatore comunale che inserisce la nota
	 */
	NotaInterna(String testo,
	            Date dataInserimento,
	            Segnalazione segnalazione,
	            OperatoreComunale operatore) {

		if (isVuoto(testo)) {
			throw new IllegalArgumentException("Il testo della nota interna non può essere vuoto.");
		}

		if (dataInserimento == null) {
			throw new IllegalArgumentException("La data di inserimento non può essere nulla.");
		}

		if (segnalazione == null) {
			throw new IllegalArgumentException("La segnalazione non può essere nulla.");
		}

		if (operatore == null) {
			throw new IllegalArgumentException("L'operatore non può essere nullo.");
		}

		this.testo = testo.trim();
		this.dataInserimento = dataInserimento;
		this.segnalazione = segnalazione;
		this.operatore = operatore;
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

	public Long getIdNota() {
		return idNota;
	}

	public String getTesto() {
		return testo;
	}

	public Date getDataInserimento() {
		if (dataInserimento == null) {
			return null;
		}
		return new Date(dataInserimento.getTime());
	}

	public OperatoreComunale getOperatore() {
		return operatore;
	}
}