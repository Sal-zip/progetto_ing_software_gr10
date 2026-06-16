package Entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity che rappresenta una segnalazione inserita da un cittadino.
 *
 * Nel pattern State, Segnalazione assume il ruolo di Context:
 * mantiene il riferimento allo StatoSegnalazione corrente e delega
 * allo stato concreto la responsabilità di applicare la transizione.
 *
 * La classe contiene strettamente:
 * - Posizione;
 * - NotaInterna;
 * - AggiornamentoStato.
 *
 * Per rispettare rigorosamente l'information hiding:
 * - non espone getPosizione();
 * - non espone getNoteInterne();
 * - non espone getAggiornamentiStato();
 * - non espone setter pubblici;
 * - non restituisce oggetti contenuti;
 * - crea internamente Posizione;
 * - crea internamente NotaInterna;
 * - crea internamente AggiornamentoStato.
 *
 * I metodi pubblici di dominio mantenuti sono quelli coerenti con il BCED:
 * - assegnaOperatore(...), che rappresenta la presa in gestione;
 * - aggiornaStato(), che applica la transizione tramite pattern State;
 * - aggiungiNotaInterna(...), che inserisce una nota interna.
 *
 * L'invio della notifica non è responsabilità di Segnalazione:
 * dopo l'aggiornamento dello stato, il Facade/Controller verifica
 * se il nuovo stato assunto è notificabile e coordina l'invio.
 */
@Entity
@Table(name = "segnalazioni")
public class Segnalazione {

	static final String STATO_INVIATA = "inviata";
	static final String STATO_PRESA_IN_CARICO = "presa_in_carico";
	static final String STATO_IN_LAVORAZIONE = "in_lavorazione";
	static final String STATO_RISOLTA = "risolta";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_segnalazione")
	private Long idSegnalazione;

	@Column(name = "titolo", nullable = false, length = 50)
	private String titolo;

	@Column(name = "descrizione", nullable = false, length = 1000)
	private String descrizione;

	@Enumerated(EnumType.STRING)
	@Column(name = "categoria", nullable = false, length = 50)
	private CategoriaSegnalazione categoria;

	/**
	 * Posizione contenuta strettamente nella segnalazione.
	 *
	 * Posizione è @Embeddable e non possiede un ciclo di vita autonomo.
	 * Viene creata internamente da Segnalazione e non viene esposta
	 * tramite getter.
	 */
	@Embedded
	private Posizione posizione;

	@Temporal(TemporalType.DATE)
	@Column(name = "data_segnalazione")
	private Date dataSegnalazione;

	@Column(name = "immagine_allegata")
	private String immagineAllegata;

	/**
	 * Stato corrente persistibile.
	 *
	 * Il valore viene salvato como String perché StatoSegnalazione
	 * è un'interfaccia comportamentale del pattern State.
	 */
	@Column(name = "stato_corrente", nullable = false, length = 30)
	private String nomeStatoCorrente;

	/**
	 * Stato corrente runtime.
	 *
	 * Non viene persistito perché rappresenta comportamento.
	 */
	@Transient
	private StatoSegnalazione statoCorrente;

	/**
	 * Indica se la segnalazione può essere modificata dal cittadino.
	 *
	 * Post-condizione della creazione:
	 * la segnalazione nasce nello stato "inviata" ed è modificabile
	 * dal cittadino.
	 *
	 * Dopo il primo aggiornamento di stato, il valore diventa false.
	 */
	@Column(name = "modificabile_dal_cittadino", nullable = false)
	private boolean modificabileDalCittadino;

	/**
	 * Cittadino autore della segnalazione.
	 *
	 * È un'associazione: la segnalazione non controlla il ciclo di vita
	 * del cittadino.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_cittadino", nullable = false)
	private Cittadino cittadinoSegnalante;

	/**
	 * Operatore comunale associato alla gestione della segnalazione.
	 *
	 * La transizione dallo stato "inviata" allo stato "presa_in_carico"
	 * è conseguenza della presa in gestione da parte dell'operatore.
	 * Per questo motivo l'operatore deve essere assegnato prima
	 * dell'aggiornamento effettivo dello stato.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_operatore")
	private OperatoreComunale operatore;

	/**
	 * Note interne contenute nella segnalazione.
	 *
	 * La lista non viene esposta all'esterno.
	 */
	@OneToMany(
			mappedBy = "segnalazione",
			cascade = CascadeType.ALL,
			fetch = FetchType.EAGER,
			orphanRemoval = true
	)
	private List<NotaInterna> noteInterne = new ArrayList<>();

	/**
	 * Storico degli aggiornamenti di stato.
	 *
	 * La lista non viene esposta all'esterno.
	 */
	@OneToMany(mappedBy = "segnalazione", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private List<AggiornamentoStato> aggiornamentiStato = new ArrayList<>();

	/**
	 * Costruttore richiesto da Hibernate/JPA.
	 */
	protected Segnalazione() {
	}

	/**
	 * Costruttore applicativo.
	 *
	 * La Segnalazione riceve dati semplici relativi alla posizione
	 * e crea internamente l'oggetto Posizione, rispettando il contenimento.
	 *
	 * La segnalazione nasce nello stato "inviata" ed è modificabile
	 * dal cittadino, come previsto dalla post-condizione del caso d'uso
	 * di creazione.
	 */
	public Segnalazione(String titolo,
	                    String descrizione,
	                    CategoriaSegnalazione categoria,
	                    String citta,
	                    String cap,
	                    String strada,
	                    String numeroCivico,
	                    String areaComunale,
	                    Cittadino cittadinoSegnalante) {

		if (isVuoto(titolo)) {
			throw new IllegalArgumentException("Il titolo non può essere vuoto.");
		}

		if (isVuoto(descrizione)) {
			throw new IllegalArgumentException("La descrizione non può essere vuota.");
		}

		if (categoria == null) {
			throw new IllegalArgumentException("La categoria non può essere nulla.");
		}

		if (cittadinoSegnalante == null) {
			throw new IllegalArgumentException("Il cittadino segnalante non può essere nullo.");
		}

		this.titolo = titolo.trim();
		this.descrizione = descrizione.trim();
		this.categoria = categoria;

		this.posizione = new Posizione(
				citta,
				cap,
				strada,
				numeroCivico,
				areaComunale
		);

		this.cittadinoSegnalante = cittadinoSegnalante;
		this.dataSegnalazione = null;
		this.immagineAllegata = null;

		inizializzaStatoInviata();
	}

	public void setDataSegnalazione(Date dataSegnalazione) {
		this.dataSegnalazione = dataSegnalazione;
	}

	public void setImmagineAllegata(String immagineAllegata) {
		this.immagineAllegata = immagineAllegata;
	}

	/**
	 * Metodo invocato automaticamente da Hibernate dopo il caricamento
	 * della entity dal database.
	 *
	 * Ricostruisce lo stato comportamentale transient partendo dal nome
	 * dello stato corrente persistito.
	 */
	@PostLoad
	private void dopoCaricamento() {
		ricostruisciStatoCorrente();
	}

	/**
	 * Metodo pubblico modellato nel BCED.
	 *
	 * Rappresenta la presa in gestione della segnalazione da parte
	 * di un OperatoreComunale.
	 *
	 * L'operatore viene associato prima dell'aggiornamento effettivo
	 * dello stato, così Segnalazione può registrare lo storico usando
	 * l'associazione interna.
	 */
	public void assegnaOperatore(OperatoreComunale operatore) {

		if (operatore == null) {
			throw new IllegalArgumentException("L'operatore non può essere nullo.");
		}

		this.operatore = operatore;
	}

	/**
	 * Metodo pubblico modellato nel BCED.
	 *
	 * Richiede l'avanzamento dello stato della segnalazione.
	 * La Segnalazione non decide direttamente il nuovo stato:
	 * delega allo StatoSegnalazione corrente, secondo il pattern State.
	 *
	 * Per registrare lo storico, la Segnalazione usa l'OperatoreComunale
	 * già associato tramite l'attributo operatore.
	 */
	public void aggiornaStato() {

		if (operatore == null) {
			throw new IllegalStateException(
					"Non è possibile aggiornare lo stato senza un operatore comunale associato."
			);
		}

		inizializzaStatoCorrenteSeNecessario();

		String statoPrecedente = nomeStatoCorrente;

		statoCorrente.aggiornaStato(this);

		if (isVuoto(nomeStatoCorrente) || nomeStatoCorrente.equals(statoPrecedente)) {
			throw new IllegalStateException("Lo stato della segnalazione non è stato aggiornato.");
		}

		registraAggiornamentoStato();
	}

	/**
	 * Metodo pubblico modellato nel BCED.
	 *
	 * Aggiunge una nota interna alla segnalazione.
	 * NotaInterna è contenuta strettamente in Segnalazione, quindi viene
	 * creata internamente e non viene restituita all'esterno.
	 */
	public void aggiungiNotaInterna(String testoNota,
	                                OperatoreComunale operatore) {

		if (isVuoto(testoNota)) {
			throw new IllegalArgumentException("Il testo della nota interna non può essere vuoto.");
		}

		if (operatore == null) {
			throw new IllegalArgumentException("L'operatore non può essere nullo.");
		}

		NotaInterna notaInterna = new NotaInterna(
				testoNota,
				new Date(),
				this,
				operatore
		);

		noteInterne.add(notaInterna);
	}

	/**
	 * Metodo tecnico di collaborazione interna al package Entity.
	 *
	 * Serve agli stati concreti del pattern State per aggiornare il Context.
	 * Non è pubblico e non deve essere invocato da Boundary o Control.
	 */
	void setStatoCorrente(StatoSegnalazione nuovoStato,
	                      String nomeNuovoStato,
	                      boolean modificabile) {

		if (nuovoStato == null) {
			throw new IllegalArgumentException("Lo stato corrente non può essere nullo.");
		}

		if (isVuoto(nomeNuovoStato)) {
			throw new IllegalArgumentException("Il nome del nuovo stato non può essere vuoto.");
		}

		this.statoCorrente = nuovoStato;
		this.nomeStatoCorrente = nomeNuovoStato.trim();
		this.modificabileDalCittadino = modificabile;
	}

	public Long getIdSegnalazione() {
		return idSegnalazione;
	}

	public String getTitolo() {
		return titolo;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public CategoriaSegnalazione getCategoria() {
		return categoria;
	}

	public Date getDataSegnalazione() {
		return copiaData(dataSegnalazione);
	}

	public String getImmagineAllegata() {
		return immagineAllegata;
	}

	public String getNomeStatoCorrente() {
		return nomeStatoCorrente;
	}

	public boolean isModificabileDalCittadino() {
		return modificabileDalCittadino;
	}

	public boolean isRisolta() {
		return STATO_RISOLTA.equals(nomeStatoCorrente);
	}

	public Cittadino getCittadinoSegnalante() {
		return cittadinoSegnalante;
	}

	public OperatoreComunale getOperatore() {
		return operatore;
	}

	/**
	 * Restituisce la città della posizione associata alla segnalazione.
	 *
	 * Il metodo espone solo un dato semplice della Posizione contenuta,
	 * senza restituire il riferimento all'oggetto Posizione.
	 *
	 * @return città della posizione, oppure stringa vuota se non disponibile
	 */
	public String getCitta() {
		if (posizione == null) {
			return "";
		}

		return posizione.getCitta();
	}

	/**
	 * Restituisce il CAP della posizione associata alla segnalazione.
	 *
	 * @return CAP della posizione, oppure stringa vuota se non disponibile
	 */
	public String getCap() {
		if (posizione == null) {
			return "";
		}

		return posizione.getCap();
	}

	/**
	 * Restituisce la strada della posizione associata alla segnalazione.
	 *
	 * @return strada della posizione, oppure stringa vuota se non disponibile
	 */
	public String getStrada() {
		if (posizione == null) {
			return "";
		}

		return posizione.getStrada();
	}

	/**
	 * Restituisce il numero civico della posizione associata alla segnalazione.
	 *
	 * @return numero civico, oppure stringa vuota se non disponibile
	 */
	public String getNumeroCivico() {
		if (posizione == null) {
			return "";
		}

		return posizione.getNumeroCivico();
	}

	/**
	 * Restituisce l'area comunale della posizione associata alla segnalazione.
	 *
	 * @return area comunale, oppure stringa vuota se non disponibile
	 */
	public String getAreaComunale() {
		if (posizione == null) {
			return "";
		}

		return posizione.getAreaComunale();
	}

	/**
	 * Registra nello storico il nuovo stato assunto dalla segnalazione.
	 *
	 * Il metodo è privato perché rappresenta un dettaglio interno
	 * dell'operazione pubblica aggiornaStato().
	 *
	 * AggiornamentoStato viene creato internamente da Segnalazione,
	 * così lo storico resta sotto il controllo della classe contenitrice.
	 */
	private void registraAggiornamentoStato() {

		if (operatore == null) {
			throw new IllegalStateException(
					"Non è possibile registrare l'aggiornamento senza un operatore comunale associato."
			);
		}

		AggiornamentoStato aggiornamentoStato = new AggiornamentoStato(
				new Date(),
				nomeStatoCorrente,
				this,
				operatore
		);

		aggiornamentiStato.add(aggiornamentoStato);
	}

	/**
	 * Inizializza la segnalazione nello stato iniziale "inviata".
	 */
	private void inizializzaStatoInviata() {
		this.nomeStatoCorrente = STATO_INVIATA;
		this.statoCorrente = new Inviata();
		this.modificabileDalCittadino = true;
	}

	/**
	 * Ricostruisce lo stato corrente se il riferimento transient
	 * non è presente.
	 */
	private void inizializzaStatoCorrenteSeNecessario() {
		if (statoCorrente == null) {
			ricostruisciStatoCorrente();
		}
	}

	/**
	 * Ricostruisce l'oggetto StatoSegnalazione corrente a partire
	 * dal nome persistito nel database.
	 */
	private void ricostruisciStatoCorrente() {

		if (isVuoto(nomeStatoCorrente)) {
			inizializzaStatoInviata();
			return;
		}

		switch (nomeStatoCorrente) {
			case STATO_INVIATA:
				this.statoCorrente = new Inviata();
				this.modificabileDalCittadino = true;
				break;

			case STATO_PRESA_IN_CARICO:
				this.statoCorrente = new PresaInCarico();
				this.modificabileDalCittadino = false;
				break;

			case STATO_IN_LAVORAZIONE:
				this.statoCorrente = new InLavorazione();
				this.modificabileDalCittadino = false;
				break;

			case STATO_RISOLTA:
				this.statoCorrente = new Risolta();
				this.modificabileDalCittadino = false;
				break;

			default:
				throw new IllegalStateException(
						"Stato corrente non valido: " + nomeStatoCorrente
				);
		}
	}

	/**
	 * Verifica se lo stato corrente della segnalazione prevede una notifica.
	 * * Delega il controllo all'oggetto State transient corrente, rispettando
	 * l'incapsulamento del pattern.
	 */
	public boolean isStatoCorrenteNotificabile() {
		inizializzaStatoCorrenteSeNecessario();
		return statoCorrente.isStatoNotificabile();
	}

	private Date copiaData(Date data) {
		if (data == null) {
			return null;
		}

		return new Date(data.getTime());
	}

	private boolean isVuoto(String valore) {
		return valore == null || valore.trim().isEmpty();
	}
}