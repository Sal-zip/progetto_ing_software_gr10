package testingBlackBox;

import Boundary.GUIAggiornamentoStatoSegnalazione;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test black-box della Boundary GUIAggiornamentoStatoSegnalazione.
 *
 * La classe verifica solo i controlli preliminari gestiti dalla GUI:
 * - sessione utente valida;
 * - presenza dell'identificativo dell'utente autenticato;
 * - ruolo coerente con Operatore Comunale;
 * - presenza dell'identificativo della segnalazione selezionata.
 *
 * Le transizioni di stato non vengono testate in questa classe perché,
 * in coerenza con il Pattern State e con l'architettura BCED, la Boundary
 * non decide il nuovo stato e non verifica direttamente il ciclo di vita
 * della segnalazione.
 *
 * La validazione del passaggio di stato e l'avanzamento effettivo della
 * segnalazione sono responsabilità del livello Control/Entity e devono
 * essere verificati in test dedicati a ControllerAggiornamentoStatoSegnalazione,
 * ServizioSegnalazioni e Segnalazione/StatoSegnalazione.
 */
class AggiornamentoStatoSegnalazioneBlackBoxTest {

    private static final Long ID_SEGNALAZIONE_TEST = 1L;

    private GUIAggiornamentoStatoSegnalazione guiAggiornamento;
    private static String erroreIntercettato;
    private static Map<String, Object> sessioneOperatore;

    /**
     * Configura l'ambiente di test e prepara una sessione valida
     * di Operatore Comunale composta solo da dati semplici.
     */
    @BeforeAll
    static void initAll() {
        System.setProperty("java.awt.headless", "true");

        sessioneOperatore = new HashMap<>();
        sessioneOperatore.put("idUtente", 2L);
        sessioneOperatore.put("nome", "Fabio");
        sessioneOperatore.put("cognome", "Verdi");
        sessioneOperatore.put("ruolo", "Operatore Comunale");
    }

    /**
     * Inizializza la GUI prima di ogni test intercettando i messaggi
     * di errore e conferma, così da verificare il comportamento della
     * Boundary senza dipendere direttamente dalla visualizzazione grafica
     * dei popup.
     */
    @BeforeEach
    void setUp() {
        erroreIntercettato = null;

        guiAggiornamento = new GUIAggiornamentoStatoSegnalazione(
                sessioneOperatore,
                ID_SEGNALAZIONE_TEST
        ) {
            @Override
            public void segnalaErroreAggiornamentoStato(String messaggio) {
                erroreIntercettato = messaggio;
            }

            @Override
            public void comunicaConfermaAggiornamento() {
                erroreIntercettato = null;
            }
        };
    }

    @Test
    @DisplayName("TC_1: Creazione GUI con sessione operatore valida e segnalazione selezionata")
    void tc1_creazioneGuiConSessioneValida() {
        assertNotNull(guiAggiornamento);
        assertNotNull(guiAggiornamento.getContentPanel());
        assertNull(erroreIntercettato);
    }

    @Test
    @DisplayName("TC_2: Utente Cittadino non autorizzato")
    void tc2_utenteCittadinoNonAutorizzato() {
        Map<String, Object> sessioneCittadino = new HashMap<>();
        sessioneCittadino.put("idUtente", 1L);
        sessioneCittadino.put("nome", "Mario");
        sessioneCittadino.put("cognome", "Rossi");
        sessioneCittadino.put("ruolo", "Cittadino");

        GUIAggiornamentoStatoSegnalazione guiCittadino =
                new GUIAggiornamentoStatoSegnalazione(
                        sessioneCittadino,
                        ID_SEGNALAZIONE_TEST
                ) {
                    @Override
                    public void segnalaErroreAggiornamentoStato(String messaggio) {
                        erroreIntercettato = messaggio;
                    }

                    @Override
                    public void comunicaConfermaAggiornamento() {
                        erroreIntercettato = null;
                    }
                };

        guiCittadino.inviaRichiestaAggiornamento(ID_SEGNALAZIONE_TEST);

        assertEquals(
                "Privilegi insufficienti. Solo un Operatore Comunale può aggiornare lo stato.",
                erroreIntercettato
        );
    }

    @Test
    @DisplayName("TC_3: Utente non autenticato con sessione nulla")
    void tc3_utenteNonAutenticatoSessioneNulla() {
        GUIAggiornamentoStatoSegnalazione guiAnonima =
                new GUIAggiornamentoStatoSegnalazione(
                        null,
                        ID_SEGNALAZIONE_TEST
                ) {
                    @Override
                    public void segnalaErroreAggiornamentoStato(String messaggio) {
                        erroreIntercettato = messaggio;
                    }

                    @Override
                    public void comunicaConfermaAggiornamento() {
                        erroreIntercettato = null;
                    }
                };

        guiAnonima.inviaRichiestaAggiornamento(ID_SEGNALAZIONE_TEST);

        assertEquals(
                "Sessione utente non valida. Effettua nuovamente l'accesso.",
                erroreIntercettato
        );
    }

    @Test
    @DisplayName("TC_4: Sessione priva di idUtente")
    void tc4_sessionePrivaDiIdUtente() {
        Map<String, Object> sessioneSenzaId = new HashMap<>();
        sessioneSenzaId.put("nome", "Fabio");
        sessioneSenzaId.put("cognome", "Verdi");
        sessioneSenzaId.put("ruolo", "Operatore Comunale");

        GUIAggiornamentoStatoSegnalazione guiSessioneSenzaId =
                new GUIAggiornamentoStatoSegnalazione(
                        sessioneSenzaId,
                        ID_SEGNALAZIONE_TEST
                ) {
                    @Override
                    public void segnalaErroreAggiornamentoStato(String messaggio) {
                        erroreIntercettato = messaggio;
                    }

                    @Override
                    public void comunicaConfermaAggiornamento() {
                        erroreIntercettato = null;
                    }
                };

        guiSessioneSenzaId.inviaRichiestaAggiornamento(ID_SEGNALAZIONE_TEST);

        assertEquals(
                "Sessione utente non valida. Effettua nuovamente l'accesso.",
                erroreIntercettato
        );
    }

    @Test
    @DisplayName("TC_5: Nessuna segnalazione selezionata")
    void tc5_nessunaSegnalazioneSelezionata() {
        guiAggiornamento.inviaRichiestaAggiornamento(null);

        assertEquals(
                "Seleziona una segnalazione prima di confermare l'aggiornamento.",
                erroreIntercettato
        );
    }
}