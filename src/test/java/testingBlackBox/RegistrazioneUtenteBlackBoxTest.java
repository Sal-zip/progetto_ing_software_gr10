package testingBlackBox;

import Boundary.GUIRegistrazioneUtente;
import Database.JpaUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RegistrazioneUtenteBlackBoxTest {

    private GUIRegistrazioneUtente guiRegistrazione;
    private String erroreIntercettato;

    @BeforeEach
    void setUp() {
        // Disattiviamo la modalità headless per consentire la visualizzazione dei pop-up grafici a schermo
        System.setProperty("java.awt.headless", "false");

        erroreIntercettato = null;

        // Istanza anonima che memorizza l'errore per gli assert e invoca il supermetodo per mostrare la GUI
        guiRegistrazione = new GUIRegistrazioneUtente() {
            @Override
            public void segnalaErroreRegistrazione(String messaggio) {
                erroreIntercettato = messaggio;
                // Richiama il comportamento originale della Boundary per mostrare il JOptionPane a schermo
                super.segnalaErroreRegistrazione(messaggio);
            }

            @Override
            public void comunicaConfermaRegistrazione() {
                // Richiama il comportamento originale della Boundary per mostrare il JOptionPane a schermo
                super.comunicaConfermaRegistrazione();
            }
        };
    }

    // --- SCENARIO DI INPUT VALIDO (HAPPY PATH) ---

    @Test
    @DisplayName("TC_1: Tutti gli input validi")
    void tc1_tuttiInputValidi() {
        Map<String, Object> dati = new HashMap<>();
        dati.put("nome", "Mario");
        dati.put("cognome", "Rossi");
        dati.put("email", "mario.rossi@email.it");
        dati.put("recapitoTelefonico", "3331234567");
        dati.put("ruolo", "Cittadino");
        dati.put("password", "PasswordSicura123!");
        dati.put("confermaPassword", "PasswordSicura123!");

        guiRegistrazione.inviaDatiRegistrazione(dati);
        assertNull(erroreIntercettato, "Non doveva essere riscontrato alcun errore di validazione.");
    }

    // --- SCENARI DI ERRORE (ROBUSTEZZA NOME / COGNOME) ---

    @Test
    @DisplayName("TC_2: Nome vuoto")
    void tc2_nomeVuoto() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("nome", "");

        guiRegistrazione.inviaDatiRegistrazione(dati);
        assertEquals("Il campo Nome è obbligatorio", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_3: Nome troppo lungo o con simboli/numeri")
    void tc3_nomeTroppoLungoOSimboli() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("nome", "Mario123!");

        guiRegistrazione.inviaDatiRegistrazione(dati);
        assertEquals("Formato Nome non valido o troppo lungo", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_4: Nome minuscolo iniziale")
    void tc4_nomeMinuscoloIniziale() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("nome", "mario");

        guiRegistrazione.inviaDatiRegistrazione(dati);

        assertEquals("Il Nome deve iniziare con una lettera maiuscola", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_5: Cognome vuoto")
    void tc5_cognomeVuoto() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("cognome", "");

        guiRegistrazione.inviaDatiRegistrazione(dati);
        assertEquals("Il campo Cognome è obbligatorio", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_6: Cognome troppo lungo o con simboli/numeri")
    void tc6_cognomeTroppoLungoOSimboli() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("cognome", "Rossi@#");

        guiRegistrazione.inviaDatiRegistrazione(dati);
        assertEquals("Formato Cognome non valido o troppo lungo", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_7: Cognome minuscolo iniziale")
    void tc7_cognomeMinuscoloIniziale() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("cognome", "rossi");

        guiRegistrazione.inviaDatiRegistrazione(dati);

        assertEquals("Il Cognome deve iniziare con una lettera maiuscola", erroreIntercettato);
    }

    // --- SCENARI DI ERRORE (ROBUSTEZZA EMAIL) ---

    @Test
    @DisplayName("TC_8: Email senza carattere '@'")
    void tc8_emailSenzaChiocciola() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("email", "mariorossi.it");

        guiRegistrazione.inviaDatiRegistrazione(dati);
        assertEquals("Formato Email non valido", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_9: Email vuota")
    void tc9_emailVuota() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("email", "");

        guiRegistrazione.inviaDatiRegistrazione(dati);
        assertEquals("Il campo Email è obbligatorio", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_10: Email con spazi interni")
    void tc10_emailConSpazi() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("email", "mario rossi@email.it");

        guiRegistrazione.inviaDatiRegistrazione(dati);
        // CORRETTO: Allineato all'eccezione cumulativa lanciata dalla validazione della GUI
        assertEquals("Formato Email non valido", erroreIntercettato);
    }





    @Test
    @DisplayName("TC_11: Email associata ad un account già esistente")
    void tc11_emailGiaEsistente() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("email", "admin@comune.it");

        guiRegistrazione.inviaDatiRegistrazione(dati);
        assertEquals("L'indirizzo Email risulta già registrato", erroreIntercettato);
    }

    // --- SCENARI DI ERRORE (ROBUSTEZZA TELEFONO / RUOLO) ---

    @Test
    @DisplayName("TC_12: Telefono con caratteri non numerici")
    void tc12_telefonoNonNumerico() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("recapitoTelefonico", "333-1234AB");

        guiRegistrazione.inviaDatiRegistrazione(dati);
        assertEquals("Il recapito deve contenere solo numeri", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_13: Telefono con lunghezza errata")
    void tc13_telefonoLunghezzaErrata() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("recapitoTelefonico", "333123");

        guiRegistrazione.inviaDatiRegistrazione(dati);
        assertEquals("La lunghezza del recapito non è valida", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_14: Ruolo non selezionato (mancante)")
    void tc14_ruoloMancante() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("ruolo", "");

        guiRegistrazione.inviaDatiRegistrazione(dati);
        assertEquals("Selezionare il proprio ruolo dal menù", erroreIntercettato);
    }

    // --- SCENARI DI ERRORE (ROBUSTEZZA PASSWORD) ---

    @Test
    @DisplayName("TC_15: Password troppo corta (< 8 caratteri)")
    void tc15_passwordTroppoCorta() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("password", "pass12");
        dati.put("confermaPassword", "pass12");

        guiRegistrazione.inviaDatiRegistrazione(dati);
        assertEquals("La password deve contenere almeno 8 caratteri", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_16: Password debole (mancano requisiti di complessità)")
    void tc16_passwordDebole() {
        Map<String, Object> dati = templateDatiValidi();
        dati.put("password", "passwordfacile");
        dati.put("confermaPassword", "passwordfacile");

        guiRegistrazione.inviaDatiRegistrazione(dati);
        assertEquals("La password deve contenere numeri, maiuscole e simboli", erroreIntercettato);
    }

    // --- METODO UTILITY ---
    private Map<String, Object> templateDatiValidi() {
        Map<String, Object> dati = new HashMap<>();
        dati.put("nome", "Mario");
        dati.put("cognome", "Rossi");
        dati.put("email", "mario.rossi@email.it");
        dati.put("recapitoTelefonico", "3331234567");
        dati.put("ruolo", "Cittadino");
        dati.put("password", "PasswordSicura123!");
        dati.put("confermaPassword", "PasswordSicura123!");
        return dati;
    }

    @AfterEach
    void tearDown() {
        try {
            EntityManager em = JpaUtil.getInstance().getEntityManager();
            em.getTransaction().begin();

            em.createQuery("DELETE FROM Utente u WHERE u.credenziali.email = :email")
                    .setParameter("email", "mario.rossi@email.it")
                    .executeUpdate();

            em.getTransaction().commit();
            em.close();
            System.out.println("[Test TearDown] Pulizia chirurgica completata: rimosso utente di test.");
        } catch (Exception e) {
            System.err.println("[Test TearDown] Errore durante la pulizia mirata del DB: " + e.getMessage());
        }
    }
}





