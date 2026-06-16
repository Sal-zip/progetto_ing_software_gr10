package testingWhiteBox;

import Entity.CategoriaSegnalazione;
import Entity.Cittadino;
import Entity.OperatoreComunale;
import Entity.Segnalazione;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite white box relativa all'aggiornamento dello stato della Segnalazione.
 *
 * La suite verifica direttamente la logica interna della Entity Segnalazione,
 * senza coinvolgere Boundary, Control, Facade Entity o Database.
 *
 * L'obiettivo è coprire i rami principali del ciclo di vita previsto dal
 * Pattern State:
 *
 * inviata -> presa_in_carico -> in_lavorazione -> risolta
 *
 * Il test è coerente con il modello BCED perché:
 * - la Boundary non sceglie arbitrariamente il nuovo stato;
 * - il Control coordina il caso d'uso;
 * - la transizione effettiva viene applicata dalla Entity Segnalazione;
 * - il Pattern State determina l'avanzamento ammesso.
 */
class AggiornamentoStatoSegnalazioneWhiteBoxTest {

    private static final String STATO_INVIATA = "inviata";
    private static final String STATO_PRESA_IN_CARICO = "presa_in_carico";
    private static final String STATO_IN_LAVORAZIONE = "in_lavorazione";
    private static final String STATO_RISOLTA = "risolta";

    /**
     * Verifica che una nuova Segnalazione venga inizializzata nello stato
     * iniziale previsto dal dominio.
     */
    @Test
    @DisplayName("WB_1: una nuova segnalazione nasce nello stato inviata")
    void wb1_nuovaSegnalazioneNasceInviata() {
        Segnalazione segnalazione = creaSegnalazioneTest();

        assertEquals(
                STATO_INVIATA,
                segnalazione.getNomeStatoCorrente()
        );
    }

    /**
     * Copre il ramo di transizione:
     *
     * inviata -> presa_in_carico
     */
    @Test
    @DisplayName("WB_2: aggiornaStato porta da inviata a presa_in_carico")
    void wb2_transizioneDaInviataAPresaInCarico() {
        Segnalazione segnalazione = creaSegnalazioneTestConOperatore();

        segnalazione.aggiornaStato();

        assertEquals(
                STATO_PRESA_IN_CARICO,
                segnalazione.getNomeStatoCorrente()
        );
    }

    /**
     * Copre il ramo di transizione:
     *
     * presa_in_carico -> in_lavorazione
     */
    @Test
    @DisplayName("WB_3: aggiornaStato porta da presa_in_carico a in_lavorazione")
    void wb3_transizioneDaPresaInCaricoAInLavorazione() {
        Segnalazione segnalazione = creaSegnalazioneTestConOperatore();

        segnalazione.aggiornaStato();

        assertEquals(
                STATO_PRESA_IN_CARICO,
                segnalazione.getNomeStatoCorrente()
        );

        segnalazione.aggiornaStato();

        assertEquals(
                STATO_IN_LAVORAZIONE,
                segnalazione.getNomeStatoCorrente()
        );
    }

    /**
     * Copre il ramo di transizione:
     *
     * in_lavorazione -> risolta
     */
    @Test
    @DisplayName("WB_4: aggiornaStato porta da in_lavorazione a risolta")
    void wb4_transizioneDaInLavorazioneARisolta() {
        Segnalazione segnalazione = creaSegnalazioneTestConOperatore();

        segnalazione.aggiornaStato();
        segnalazione.aggiornaStato();

        assertEquals(
                STATO_IN_LAVORAZIONE,
                segnalazione.getNomeStatoCorrente()
        );

        segnalazione.aggiornaStato();

        assertEquals(
                STATO_RISOLTA,
                segnalazione.getNomeStatoCorrente()
        );
    }

    /**
     * Verifica l'intero ciclo di vita ammesso dalla Segnalazione.
     *
     * Il test controlla che le chiamate consecutive ad aggiornaStato()
     * seguano esclusivamente l'ordine previsto dal dominio.
     */
    @Test
    @DisplayName("WB_5: aggiornaStato segue l'intero ciclo di vita previsto")
    void wb5_cicloDiVitaCompleto() {
        Segnalazione segnalazione = creaSegnalazioneTestConOperatore();

        assertEquals(
                STATO_INVIATA,
                segnalazione.getNomeStatoCorrente()
        );

        segnalazione.aggiornaStato();

        assertEquals(
                STATO_PRESA_IN_CARICO,
                segnalazione.getNomeStatoCorrente()
        );

        segnalazione.aggiornaStato();

        assertEquals(
                STATO_IN_LAVORAZIONE,
                segnalazione.getNomeStatoCorrente()
        );

        segnalazione.aggiornaStato();

        assertEquals(
                STATO_RISOLTA,
                segnalazione.getNomeStatoCorrente()
        );
    }

    /**
     * Copre il ramo terminale del ciclo di vita.
     *
     * Una segnalazione già risolta non deve avanzare ulteriormente.
     * Poiché lo stato risolta rappresenta uno stato terminale del Pattern State,
     * un'ulteriore richiesta di aggiornamento produce un'eccezione applicativa
     * e lo stato corrente resta invariato.
     */
    @Test
    @DisplayName("WB_6: una segnalazione risolta non può avanzare oltre lo stato risolta")
    void wb6_segnalazioneRisoltaNonAvanzaOltre() {
        Segnalazione segnalazione = creaSegnalazioneTestConOperatore();

        segnalazione.aggiornaStato();
        segnalazione.aggiornaStato();
        segnalazione.aggiornaStato();

        assertEquals(
                STATO_RISOLTA,
                segnalazione.getNomeStatoCorrente()
        );

        assertThrows(
                IllegalStateException.class,
                segnalazione::aggiornaStato
        );

        assertEquals(
                STATO_RISOLTA,
                segnalazione.getNomeStatoCorrente()
        );
    }

    /**
     * Verifica la modificabilità della segnalazione da parte del cittadino
     * durante le fasi non terminali del ciclo di vita.
     *
     * Secondo l'implementazione, il cittadino può modificare la segnalazione
     * solo finché essa si trova nello stato iniziale inviata. Dopo la presa in
     * carico da parte dell'operatore comunale, la segnalazione entra nel flusso
     * di gestione comunale e non è più modificabile dal cittadino.
     */
    @Test
    @DisplayName("WB_7: una segnalazione è modificabile dal cittadino solo nello stato inviata")
    void wb7_segnalazioneModificabileDalCittadinoSoloSeInviata() {
        Segnalazione segnalazione = creaSegnalazioneTestConOperatore();

        assertEquals(
                STATO_INVIATA,
                segnalazione.getNomeStatoCorrente()
        );

        assertTrue(
                segnalazione.isModificabileDalCittadino()
        );

        segnalazione.aggiornaStato();

        assertEquals(
                STATO_PRESA_IN_CARICO,
                segnalazione.getNomeStatoCorrente()
        );

        assertFalse(
                segnalazione.isModificabileDalCittadino()
        );

        segnalazione.aggiornaStato();

        assertEquals(
                STATO_IN_LAVORAZIONE,
                segnalazione.getNomeStatoCorrente()
        );

        assertFalse(
                segnalazione.isModificabileDalCittadino()
        );
    }

    /**
     * Verifica il ramo in cui la segnalazione è risolta.
     *
     * In questo stato la segnalazione non può più essere modificata dal
     * cittadino, coerentemente con la nota presente nel class diagram.
     */
    @Test
    @DisplayName("WB_8: una segnalazione risolta non è modificabile dal cittadino")
    void wb8_segnalazioneRisoltaNonModificabileDalCittadino() {
        Segnalazione segnalazione = creaSegnalazioneTestConOperatore();

        segnalazione.aggiornaStato();
        segnalazione.aggiornaStato();
        segnalazione.aggiornaStato();

        assertEquals(
                STATO_RISOLTA,
                segnalazione.getNomeStatoCorrente()
        );

        assertFalse(
                segnalazione.isModificabileDalCittadino()
        );
    }

    /**
     * Verifica che una singola chiamata ad aggiornaStato() non possa saltare
     * direttamente da inviata a risolta.
     *
     * Questo test evidenzia che il ciclo di vita è rigido e che il nuovo stato
     * non viene scelto liberamente dalla Boundary.
     */
    @Test
    @DisplayName("WB_9: aggiornaStato non consente salti diretti da inviata a risolta")
    void wb9_nonConsenteSaltoDirettoDaInviataARisolta() {
        Segnalazione segnalazione = creaSegnalazioneTestConOperatore();

        segnalazione.aggiornaStato();

        assertNotEquals(
                STATO_RISOLTA,
                segnalazione.getNomeStatoCorrente()
        );

        assertEquals(
                STATO_PRESA_IN_CARICO,
                segnalazione.getNomeStatoCorrente()
        );
    }

    /**
     * Verifica che una singola chiamata ad aggiornaStato() non possa saltare
     * direttamente da presa_in_carico a risolta.
     */
    @Test
    @DisplayName("WB_10: aggiornaStato non consente salti diretti da presa_in_carico a risolta")
    void wb10_nonConsenteSaltoDirettoDaPresaInCaricoARisolta() {
        Segnalazione segnalazione = creaSegnalazioneTestConOperatore();

        segnalazione.aggiornaStato();

        assertEquals(
                STATO_PRESA_IN_CARICO,
                segnalazione.getNomeStatoCorrente()
        );

        segnalazione.aggiornaStato();

        assertNotEquals(
                STATO_RISOLTA,
                segnalazione.getNomeStatoCorrente()
        );

        assertEquals(
                STATO_IN_LAVORAZIONE,
                segnalazione.getNomeStatoCorrente()
        );
    }

    /**
     * Crea una segnalazione valida per i test white box e le associa
     * un operatore comunale.
     *
     * L'operatore viene assegnato perché, nella normale catena BCED,
     * ServizioSegnalazioni assegna l'operatore alla Segnalazione prima
     * di invocare aggiornaStato().
     *
     * @return segnalazione di test inizializzata nello stato inviata
     */
    private Segnalazione creaSegnalazioneTestConOperatore() {
        Segnalazione segnalazione = creaSegnalazioneTest();
        OperatoreComunale operatore = creaOperatoreTest();

        segnalazione.assegnaOperatore(operatore);

        return segnalazione;
    }

    /**
     * Crea una segnalazione valida per i test white box.
     *
     * Il metodo costruisce solo oggetti Entity in memoria e non accede alla
     * persistenza. In questo modo il test resta focalizzato sulla logica
     * interna della classe Segnalazione.
     *
     * @return segnalazione di test inizializzata nello stato inviata
     */
    private Segnalazione creaSegnalazioneTest() {
        Cittadino cittadino = creaCittadinoTest();

        return new Segnalazione(
                "Segnalazione Test White Box",
                "Descrizione di test per aggiornamento stato",
                CategoriaSegnalazione.strada_dissestata,
                "Napoli",
                "80125",
                "Via Claudio",
                "21",
                "Fuorigrotta",
                cittadino
        );
    }

    /**
     * Crea un cittadino valido da associare alla segnalazione di test.
     *
     * @return cittadino di test
     */
    private Cittadino creaCittadinoTest() {
        return new Cittadino(
                "Mario",
                "Rossi",
                "3331234567",
                "mario.rossi@test.it",
                "Password123!"
        );
    }

    /**
     * Crea un operatore comunale valido da associare alla segnalazione
     * prima dell'aggiornamento dello stato.
     *
     * @return operatore comunale di test
     */
    private OperatoreComunale creaOperatoreTest() {
        return new OperatoreComunale(
                "Fabio",
                "Verdi",
                "3339876543",
                "fabio.verdi@test.it",
                "Password123!"
        );
    }
}
