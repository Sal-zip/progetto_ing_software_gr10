package testingBlackBox;

import Boundary.GUICreazioneSegnalazione;
import Entity.CategoriaSegnalazione;
import Database.JpaUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CreaNuovaSegnalazioneBlackBoxTest {

    private GUICreazioneSegnalazione guiCreazione;
    private Map<String, Object> sessioneUtente;
    private String erroreIntercettato;

    @BeforeEach
    void setUp() {
        // Disattiviamo la modalità headless per consentire la visualizzazione dei pop-up grafici a schermo
        System.setProperty("java.awt.headless", "false");

        // Configura una sessione fittizia per un Cittadino valido (Pre-condizione)
        sessioneUtente = new HashMap<>();
        sessioneUtente.put("idUtente", 1L);
        sessioneUtente.put("nome", "Mario");
        sessioneUtente.put("cognome", "Rossi");
        sessioneUtente.put("ruolo", "Cittadino");

        erroreIntercettato = null;

        // Istanza anonima che memorizza l'errore per gli assert e invoca il supermetodo per mostrare la GUI
        guiCreazione = new GUICreazioneSegnalazione(sessioneUtente) {
            @Override
            public void segnalaErroreCreazione(String messaggio) {
                erroreIntercettato = messaggio;
                // Richiama il comportamento originale della Boundary per mostrare il JOptionPane di errore a schermo
                super.segnalaErroreCreazione(messaggio);
            }

            @Override
            public void comunicaConfermaCreazione() {
                // Richiama il comportamento originale della Boundary per mostrare il JOptionPane di successo a schermo
                super.comunicaConfermaCreazione();
            }
        };
    }

    @AfterEach
    void tearDown() {
        // Cancella chirurgicamente solo la segnalazione creata per il test
        try {
            EntityManager em = JpaUtil.getInstance().getEntityManager();
            em.getTransaction().begin();

            em.createQuery("DELETE FROM Segnalazione s WHERE s.titolo = :titolo")
                    .setParameter("titolo", "Buca stradale")
                    .executeUpdate();

            em.getTransaction().commit();
            em.close();
            System.out.println("[Test TearDown] Pulizia chirurgica completata: rimossa segnalazione di test.");
        } catch (Exception e) {
            System.err.println("[Test TearDown] Errore in pulizia DB: " + e.getMessage());
        }
    }

    // --- SCENARI INPUT VALIDI (HAPPY PATH) ---
    // Ciascun caso felice mostrerà il pop-up di successo a schermo prima di passare alla Dashboard

    @Test
    @DisplayName("TC_1: Tutti gli input validi completi")
    void tc1_tuttiInputValidi() {
        Map<String, Object> dati = templateDatiValidi();
        guiCreazione.inviaDatiSegnalazione(dati);

        assertNull(erroreIntercettato, "Non doveva essere riscontrato alcun errore di validazione.");
    }

    @Test
    @DisplayName("TC_2: Input validi, campi opzionali omessi")
    void tc2_campiOpzionaliOmessi() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("dataSegnalazione", null);
        dati.put("immagineAllegata", null);

        guiCreazione.inviaDatiSegnalazione(dati);
        assertNull(erroreIntercettato, "L'omissione di campi opzionali non deve bloccare l'invio.");
    }

    @Test
    @DisplayName("TC_3: Tutti input validi, Data omessa")
    void tc3_dataOmessaAllegatoValido() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("dataSegnalazione", null);

        guiCreazione.inviaDatiSegnalazione(dati);
        assertNull(erroreIntercettato, "La data omessa non deve bloccare la segnalazione.");
    }

    @Test
    @DisplayName("TC_4: Tutti input validi, Allegato omesso")
    void tc4_allegatoOmesso() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("immagineAllegata", null);

        guiCreazione.inviaDatiSegnalazione(dati);
        assertNull(erroreIntercettato, "L'allegato omesso non deve bloccare la segnalazione.");
    }

    // --- SCENARI DI ERRORE TITOLO / DESCRIZIONE / CATEGORIA ---

    @Test
    @DisplayName("TC_5: Titolo vuoto")
    void tc5_titoloVuoto() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("titolo", "");

        guiCreazione.inviaDatiSegnalazione(dati);
        assertEquals("Campo Titolo obbligatorio", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_6: Titolo troppo lungo")
    void tc6_titoloTroppoLungo() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("titolo", "Bucaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        guiCreazione.inviaDatiSegnalazione(dati);
        assertEquals("II Titolo supera la lunghezza massima consentita", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_7: Descrizione vuota")
    void tc7_descrizioneVuota() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("descrizione", "");

        guiCreazione.inviaDatiSegnalazione(dati);
        assertEquals("Campo Descrizione obbligatorio", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_8: Descrizione troppo lunga")
    void tc8_descrizioneTroppoLunga() {
        Map<String, Object> dati = templateDatiValidi();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1005; i++) {
            sb.append("a");
        }
        dati.put("descrizione", sb.toString());

        guiCreazione.inviaDatiSegnalazione(dati);
        assertEquals("La Descrizione supera la lunghezza massima consentita", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_9: Categoria mancante")
    void tc9_categoriaMancante() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("categoria", "Seleziona categoria");

        guiCreazione.inviaDatiSegnalazione(dati);
        assertEquals("Selezionare una Categoria!", erroreIntercettato);
    }

    // --- SCENARI DI ERRORE LOCALIZZAZIONE (POSIZIONE) ---

    @Test
    @DisplayName("TC_10: Città vuota")
    void tc10_cittaVuota() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("citta", "");

        guiCreazione.inviaDatiSegnalazione(dati);
        assertEquals("Campo Città obbligatorio per la posizione!", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_11: Città con numeri")
    void tc11_cittaConNumeri() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("citta", "Napoli123");

        guiCreazione.inviaDatiSegnalazione(dati);
        assertEquals("La Città non può contenere numeri o caratteri speciali", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_12: CAP vuoto")
    void tc12_capVuoto() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("cap", "");

        guiCreazione.inviaDatiSegnalazione(dati);
        assertEquals("Campo CAP obbligatorio !", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_13: CAP con lunghezza errata")
    void tc13_capLunghezzaErrata() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("cap", "8012");

        guiCreazione.inviaDatiSegnalazione(dati);
        assertEquals("II CAP deve essere esattamente di 5 cifre", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_14: CAP con caratteri non numerici")
    void tc14_capNonNumerico() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("cap", "801AB");

        guiCreazione.inviaDatiSegnalazione(dati);
        assertEquals("Il CAP deve contenere solo numeri", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_15: Strada vuota")
    void tc15_stradaVuota() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("strada", "");

        guiCreazione.inviaDatiSegnalazione(dati);
        assertEquals("Campo Strada obbligatorio !", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_16: Strada con caratteri speciali vietati")
    void tc16_stradaCaratteriVietati() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("strada", "Via Claudio @");

        guiCreazione.inviaDatiSegnalazione(dati);
        assertEquals("Formato strada non valido", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_17: Civico vuoto")
    void tc17_civicoVuoto() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("numeroCivico", "");

        guiCreazione.inviaDatiSegnalazione(dati);
        assertEquals("Campo Civico obbligatorio !", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_18: Civico formato non valido")
    void tc18_civicoNonValido() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("numeroCivico", "N.D.");

        guiCreazione.inviaDatiSegnalazione(dati);
        assertEquals("Formato civico non valido", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_19: Area comunale vuota")
    void tc19_areaVuota() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("areaComunale", "");

        guiCreazione.inviaDatiSegnalazione(dati);
        assertEquals("Campo Area Comunale obbligatorio !", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_20: Area fuori dal territorio comunale")
    void tc20_areaFuoriComune() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("areaComunale", "Milano Centro");

        guiCreazione.inviaDatiSegnalazione(dati);
        assertEquals("Area non appartenente al territorio comunale", erroreIntercettato);
    }

    // --- SCENARI DATA / ALLEGATI ---

    @Test
    @DisplayName("TC_21: Data nel futuro")
    void tc21_dataFutura() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("dataSegnalazione", "31/12/2099");

        guiCreazione.inviaDatiSegnalazione(dati);
        assertEquals("La data non può essere nel futuro", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_22: Data formato non interpretabile")
    void tc22_dataIncomprensibile() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("dataSegnalazione", "ieri mattina");

        guiCreazione.inviaDatiSegnalazione(dati);
        assertEquals("Formato data non valido", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_23: Formato allegato non supportato (.exe)")
    void tc23_allegatoNonSupportato() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("immagineAllegata", "virus.exe");

        guiCreazione.inviaDatiSegnalazione(dati);
        assertEquals("Formato file non consentito. Usare JPG/PNG", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_24: Dimensione allegato superiore a 5MB")
    void tc24_allegatoTroppoGrande() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("immagineAllegata", "video.mp4 (100MB)");

        guiCreazione.inviaDatiSegnalazione(dati);
        assertEquals("II file supera la dimensione massima di 5MB", erroreIntercettato);
    }

    // --- STRUMENTO UTILITY TEMPLATE ---
    private Map<String, Object> templateDatiValidi() {
        Map<String, Object> dati = new HashMap<>();
        dati.put("titolo", "Buca stradale");
        dati.put("descrizione", "Buca profonda");
        dati.put("categoria", CategoriaSegnalazione.strada_dissestata);
        dati.put("citta", "Napoli");
        dati.put("cap", "80125");
        dati.put("strada", "Via Claudio");
        dati.put("numeroCivico", "21");
        dati.put("areaComunale", "Fuorigrotta");
        dati.put("dataSegnalazione", "15/05/2026");
        dati.put("immagineAllegata", "buca.jpg");
        return dati;
    }
}