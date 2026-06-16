package Entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * Rappresenta l'avviso generato dal sistema per informare il cittadino
 * di una variazione rilevante dello stato della propria segnalazione.
 *
 * La classe è coerente con lo State Pattern adottato per Segnalazione:
 * non conserva un riferimento a un oggetto StatoSegnalazione e non dipende
 * da un enum. Lo stato che ha generato la notifica viene memorizzato come
 * valore testuale, corrispondente al nome dello stato corrente raggiunto
 * dalla Segnalazione.
 *
 * La Notifica non determina e non applica transizioni di stato. La transizione
 * resta responsabilità di Segnalazione e delle classi concrete dello State
 * Pattern. Questa classe registra soltanto l'informazione necessaria alla
 * comunicazione verso il cittadino.
 *
 * Nel dominio considerato, sono notificabili soltanto alcuni stati della
 * segnalazione, in particolare:
 * - presa_in_carico;
 * - risolta.
 */
@Entity
@Table(name = "Notifiche")
public class Notifica {

	/**
	 * Valore testuale dello stato notificabile "presa_in_carico".
	 *
	 * Il valore deve essere coerente con il nome dello stato persistito
	 * nella classe Segnalazione.
	 */
	private static final String STATO_PRESA_IN_CARICO = "presa_in_carico";

	/**
	 * Valore testuale dello stato notificabile "risolta".
	 *
	 * Il valore deve essere coerente con il nome dello stato persistito
	 * nella classe Segnalazione.
	 */
	private static final String STATO_RISOLTA = "risolta";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_notifica")
	private Long idNotifica;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "data_invio", nullable = false)
	private Date dataInvio;

	@Column(name = "messaggio", nullable = false, length = 1000)
	private String messaggio;

	@Column(name = "stato_notificato", nullable = false, length = 50)
	private String statoNotificato;

	@ManyToOne
	@JoinColumn(name = "id_cittadino", nullable = false)
	private Cittadino destinatario;

	@ManyToOne
	@JoinColumn(name = "id_segnalazione", nullable = false)
	private Segnalazione segnalazione;

	/**
	 * Costruttore richiesto da JPA.
	 *
	 * Deve rimanere protetto per consentire al provider ORM di ricostruire
	 * l'istanza senza esporre una creazione libera della Notifica ai livelli
	 * Boundary o Control.
	 */
	protected Notifica() {
		/*
		 * Costruttore richiesto da JPA.
		 */
	}

	/**
	 * Costruisce una notifica relativa a uno stato notificabile raggiunto
	 * da una segnalazione.
	 *
	 * Il costruttore ha visibilità package-private per mantenere la creazione
	 * della Notifica confinata al package Entity. In questo modo Boundary e
	 * Control non possono creare direttamente notifiche, ma devono passare
	 * attraverso il Facade Entity incaricato.
	 *
	 * Il parametro statoNotificato è un valore testuale perché lo stato della
	 * Segnalazione, nello State Pattern adottato, viene rappresentato e
	 * persistito tramite il nome dello stato corrente.
	 *
	 * @param dataInvio data e ora di generazione della notifica
	 * @param messaggio testo del messaggio notificato al cittadino
	 * @param statoNotificato nome dello stato notificabile raggiunto dalla segnalazione
	 * @param destinatario cittadino destinatario della notifica
	 * @param segnalazione segnalazione a cui la notifica è associata
	 */
	Notifica(
			Date dataInvio,
			String messaggio,
			String statoNotificato,
			Cittadino destinatario,
			Segnalazione segnalazione
	) {
		if (dataInvio == null) {
			throw new IllegalArgumentException("La data di invio della notifica è obbligatoria.");
		}

		if (isVuoto(messaggio)) {
			throw new IllegalArgumentException("Il messaggio della notifica è obbligatorio.");
		}

		if (!isStatoNotificabile(statoNotificato)) {
			throw new IllegalArgumentException("Lo stato indicato non è notificabile.");
		}

		if (destinatario == null) {
			throw new IllegalArgumentException("Il destinatario della notifica è obbligatorio.");
		}

		if (segnalazione == null) {
			throw new IllegalArgumentException("La segnalazione associata alla notifica è obbligatoria.");
		}

		this.dataInvio = new Date(dataInvio.getTime());
		this.messaggio = messaggio.trim();
		this.statoNotificato = statoNotificato.trim();
		this.destinatario = destinatario;
		this.segnalazione = segnalazione;
	}

	/**
	 * Restituisce l'identificativo persistente della notifica.
	 *
	 * @return identificativo della notifica
	 */
	public Long getIdNotifica() {
		return idNotifica;
	}

	/**
	 * Restituisce la data di invio della notifica.
	 *
	 * Viene restituita una copia difensiva per evitare modifiche esterne
	 * allo stato interno dell'Entity.
	 *
	 * @return data di invio della notifica
	 */
	public Date getDataInvio() {
		if (dataInvio == null) {
			return null;
		}

		return new Date(dataInvio.getTime());
	}

	/**
	 * Restituisce il messaggio notificato al cittadino.
	 *
	 * @return testo della notifica
	 */
	public String getMessaggio() {
		return messaggio;
	}

	/**
	 * Restituisce il nome dello stato che ha generato la notifica.
	 *
	 * Il valore è testuale ed è coerente con la rappresentazione dello stato
	 * corrente della Segnalazione nello State Pattern.
	 *
	 * @return nome dello stato notificato
	 */
	public String getStatoNotificato() {
		return statoNotificato;
	}

	/**
	 * Restituisce il cittadino destinatario della notifica.
	 *
	 * Il destinatario è un'entità associata alla Notifica, non un oggetto
	 * contenuto. Il metodo deve essere usato con cautela dal Facade Entity
	 * per eventuali operazioni di mapping verso dati semplici.
	 *
	 * @return cittadino destinatario della notifica
	 */
	public Cittadino getDestinatario() {
		return destinatario;
	}

	/**
	 * Restituisce la segnalazione associata alla notifica.
	 *
	 * La segnalazione è un'entità associata alla Notifica e non viene
	 * modificata da questo metodo.
	 *
	 * @return segnalazione associata alla notifica
	 */
	public Segnalazione getSegnalazione() {
		return segnalazione;
	}

	/**
	 * Verifica se lo stato indicato è notificabile.
	 *
	 * Questa verifica non governa la transizione di stato della Segnalazione:
	 * controlla soltanto che la Notifica venga costruita per uno stato per
	 * cui è prevista la comunicazione al cittadino.
	 *
	 * @param stato nome dello stato da verificare
	 * @return true se lo stato è notificabile, false altrimenti
	 */
	private boolean isStatoNotificabile(String stato) {
		if (isVuoto(stato)) {
			return false;
		}

		String statoNormalizzato = stato.trim();

		return STATO_PRESA_IN_CARICO.equals(statoNormalizzato)
				|| STATO_RISOLTA.equals(statoNormalizzato);
	}

	/**
	 * Verifica se una stringa è nulla o vuota.
	 *
	 * Metodo privato di supporto alla validazione interna della classe.
	 * Non rappresenta una responsabilità pubblica del modello BCED.
	 *
	 * @param valore stringa da verificare
	 * @return true se la stringa è nulla o vuota, false altrimenti
	 */
	private boolean isVuoto(String valore) {
		return valore == null || valore.trim().isEmpty();
	}
}