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
 * Entity che rappresenta la registrazione storica di un aggiornamento
 * di stato effettuato su una Segnalazione.
 *
 * AggiornamentoStato non applica direttamente la transizione di stato:
 * la transizione viene gestita dalla classe Segnalazione tramite
 * il pattern State.
 *
 * La responsabilità di questa classe è conservare le informazioni
 * necessarie alla consultazione dell'evoluzione della segnalazione:
 * - data dell'aggiornamento;
 * - nuovo stato assunto;
 * - segnalazione aggiornata;
 * - operatore comunale che ha effettuato l'aggiornamento.
 *
 * In coerenza con il contenimento dello storico nella Segnalazione:
 * - AggiornamentoStato viene creato dalla Segnalazione;
 * - non espone setter;
 * - non modifica la Segnalazione;
 * - non controlla il ciclo di vita di Segnalazione o OperatoreComunale;
 * - espone solo i getter necessari al Facade Entity per consultare lo storico.
 */
@Entity
@Table(name = "aggiornamenti_stato")
public class AggiornamentoStato {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_aggiornamento")
	private Long idAggiornamento;

	/**
	 * Data e ora in cui viene registrato l'aggiornamento di stato.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "data_aggiornamento", nullable = false)
	private Date dataAggiornamento;

	/**
	 * Nome persistibile del nuovo stato assunto dalla segnalazione.
	 *
	 * StatoSegnalazione è un'interfaccia comportamentale del pattern State,
	 * quindi nello storico viene salvato solo il nome dello stato raggiunto.
	 */
	@Column(name = "nuovo_stato", nullable = false, length = 30)
	private String nuovoStato;

	/**
	 * Segnalazione a cui appartiene questo aggiornamento.
	 *
	 * La relazione inversa è gestita da Segnalazione tramite @OneToMany.
	 * Non viene usato cascade da AggiornamentoStato verso Segnalazione,
	 * perché l'aggiornamento non controlla il ciclo di vita della
	 * segnalazione padre.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_segnalazione", nullable = false)
	private Segnalazione segnalazione;

	/**
	 * Operatore comunale che ha effettuato l'aggiornamento.
	 *
	 * Non viene usato cascade perché AggiornamentoStato non controlla
	 * il ciclo di vita dell'operatore.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_operatore", nullable = false)
	private OperatoreComunale operatore;

	/**
	 * Costruttore richiesto da Hibernate/JPA.
	 *
	 * La visibilità protected evita l'istanziazione libera dall'esterno,
	 * ma consente al provider JPA di ricostruire l'oggetto dalla persistenza.
	 */
	protected AggiornamentoStato() {
	}

	/**
	 * Costruttore applicativo con visibilità package-private.
	 *
	 * L'aggiornamento di stato deve essere creato all'interno del package Entity,
	 * in particolare dalla classe Segnalazione, quando viene registrata
	 * l'evoluzione dello stato corrente.
	 *
	 * @param dataAggiornamento data dell'aggiornamento
	 * @param nuovoStato nome del nuovo stato assunto dalla segnalazione
	 * @param segnalazione segnalazione aggiornata
	 * @param operatore operatore che ha effettuato l'aggiornamento
	 */
	AggiornamentoStato(Date dataAggiornamento,
	                   String nuovoStato,
	                   Segnalazione segnalazione,
	                   OperatoreComunale operatore) {

		if (dataAggiornamento == null) {
			throw new IllegalArgumentException("La data dell'aggiornamento non può essere nulla.");
		}

		if (isVuoto(nuovoStato)) {
			throw new IllegalArgumentException("Il nuovo stato non può essere nullo o vuoto.");
		}

		if (segnalazione == null) {
			throw new IllegalArgumentException("La segnalazione non può essere nulla.");
		}

		if (operatore == null) {
			throw new IllegalArgumentException("L'operatore non può essere nullo.");
		}

		this.dataAggiornamento = copiaData(dataAggiornamento);
		this.nuovoStato = nuovoStato.trim();
		this.segnalazione = segnalazione;
		this.operatore = operatore;
	}

	/**
	 * Restituisce l'identificativo dell'aggiornamento.
	 *
	 * Necessario al Facade Entity per rappresentare lo storico
	 * dell'evoluzione della segnalazione.
	 */
	public Long getIdAggiornamento() {
		return idAggiornamento;
	}

	/**
	 * Restituisce la data dell'aggiornamento.
	 *
	 * Viene restituita una copia per evitare che il chiamante possa
	 * modificare indirettamente il valore interno dell'oggetto.
	 */
	public Date getDataAggiornamento() {
		return copiaData(dataAggiornamento);
	}

	/**
	 * Restituisce il nuovo stato registrato nello storico.
	 *
	 * Il valore è una String perché lo stato comportamentale
	 * StatoSegnalazione non viene persistito direttamente.
	 */
	public String getNuovoStato() {
		return nuovoStato;
	}

	/**
	 * Restituisce la segnalazione associata all'aggiornamento.
	 *
	 * Serve al Facade Entity per recuperare l'identificativo della
	 * segnalazione durante la costruzione della rappresentazione dello storico.
	 */
	public Segnalazione getSegnalazione() {
		return segnalazione;
	}

	/**
	 * Restituisce l'operatore che ha effettuato l'aggiornamento.
	 *
	 * Serve al Facade Entity per mostrare chi ha eseguito il cambio stato.
	 */
	public OperatoreComunale getOperatore() {
		return operatore;
	}

	/**
	 * Metodo privato di supporto alla copia difensiva delle date.
	 */
	private Date copiaData(Date data) {
		if (data == null) {
			return null;
		}

		return new Date(data.getTime());
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