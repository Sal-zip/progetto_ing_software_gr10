package testingBlackBox;

import Boundary.GUIMonitoraggioAttivita;
import Database.JpaUtil;
import Entity.CategoriaSegnalazione;
import Entity.Cittadino;
import Entity.Segnalazione;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import javax.swing.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MonitoraAndamentoAttivitaBlackBoxTest {

    private GUIMonitoraggioAttivita guiMonitoraggio;
    private static String erroreIntercettato;
    private static Map<String, Object> sessioneOperatore;

    @BeforeAll
    static void initAll() {
        // Forza l'ambiente headless per evitare l'istanziazione di finestre grafiche pesanti
        System.setProperty("java.awt.headless", "true");

        // Configurazione unica della sessione Operatore Comunale
        sessioneOperatore = new HashMap<>();
        sessioneOperatore.put("idUtente", 2L);
        sessioneOperatore.put("nome", "Fabio");
        sessioneOperatore.put("cognome", "Verdi");
        sessioneOperatore.put("ruolo", "Operatore Comunale");

        // Inserimento unico nel DB per tutti i test (eseguito una sola volta)
        creaSegnalazioneDiTestUnica("Segnalazione Test Monitoraggio", 2026, Calendar.JANUARY, 15);
    }

    @BeforeEach
    void setUp() {
        erroreIntercettato = null;

        // Istanza riutilizzabile della GUI che intercetta i messaggi in una variabile locale
        guiMonitoraggio = new GUIMonitoraggioAttivita(sessioneOperatore) {
            @Override
            public void segnalaErroreMonitoraggio(String messaggio) {
                erroreIntercettato = messaggio;
            }

            @Override
            public void segnalaErroreMonitoraggio() {
                erroreIntercettato = "Errore durante il monitoraggio delle attività.";
            }
        };
    }

    @AfterAll
    static void tearDownAll() {
        // Pulizia finale eseguita una sola volta al termine dell'intera suite
        try {
            EntityManager em = JpaUtil.getInstance().getEntityManager();
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Segnalazione s WHERE s.titolo LIKE 'Segnalazione Test%'").executeUpdate();
            em.getTransaction().commit();
            em.close();
            System.out.println("[Test TearDown] Pulizia completata: rimosse segnalazioni di test.");
        } catch (Exception e) {
            System.err.println("[Test TearDown] Errore durante la rimozione dei dati: " + e.getMessage());
        }
    }

    // --- SCENARIO DI INPUT VALIDO (HAPPY PATH) ---

    @Test
    @DisplayName("TC_1: Intervallo valido")
    void tc1_intervalloValido() {
        simulaInputData(guiMonitoraggio, "01-01-2026", "31-01-2026");

        Map<String, Object> parametri = guiMonitoraggio.acquisisciParametriMonitoraggio();

        try {
            guiMonitoraggio.inviaRichiestaMonitoraggio(parametri);
            assertNull(erroreIntercettato, "Il report doveva essere elaborato senza errori.");
        } catch (Exception e) {
            fail("Non doveva sollevare alcuna eccezione per un intervallo valido.");
        }
    }

    // --- SCENARI DI ERRORE E ROBUSTEZZA ---

    @Test
    @DisplayName("TC_2: Violazione permessi (Ruolo Cittadino)")
    void tc2_violazionePermessi() {
        Map<String, Object> sessioneCittadino = new HashMap<>();
        sessioneCittadino.put("idUtente", 1L);
        sessioneCittadino.put("ruolo", "Cittadino");

        GUIMonitoraggioAttivita guiCittadino = new GUIMonitoraggioAttivita(sessioneCittadino) {
            @Override
            public void segnalaErroreMonitoraggio(String messaggio) {
                erroreIntercettato = messaggio;
            }
        };

        simulaInputData(guiCittadino, "01-01-2026", "31-01-2026");

        Map<String, Object> parametri = guiCittadino.acquisisciParametriMonitoraggio();
        guiCittadino.inviaRichiestaMonitoraggio(parametri);

        assertEquals("Accesso negato. Solo gli operatori possono visualizzare l'andamento.", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_3: Utente non autenticato (Sessione NULL)")
    void tc3_utenteNonAutenticato() {
        GUIMonitoraggioAttivita guiAnonima = new GUIMonitoraggioAttivita(null) {
            @Override
            public void segnalaErroreMonitoraggio(String messaggio) {
                erroreIntercettato = messaggio;
            }
        };

        simulaInputData(guiAnonima, "01-01-2026", "31-01-2026");

        Map<String, Object> parametri = guiAnonima.acquisisciParametriMonitoraggio();
        guiAnonima.inviaRichiestaMonitoraggio(parametri);

        assertEquals("Effettuare l'accesso per visualizzare i report.", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_4: Data Inizio futura rispetto ad oggi")
    void tc4_dataInizioFutura() {
        simulaInputData(guiMonitoraggio, "01-01-2050", "31-01-2026");

        Map<String, Object> parametri = guiMonitoraggio.acquisisciParametriMonitoraggio();
        guiMonitoraggio.inviaRichiestaMonitoraggio(parametri);

        assertEquals("La data di inizio non può essere successiva alla data odierna.", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_5: Formato Data Inizio non valido")
    void tc5_formatoDataInizioNonValido() {
        simulaInputData(guiMonitoraggio, "gennaio 26", "31-01-2026");

        Map<String, Object> parametri = guiMonitoraggio.acquisisciParametriMonitoraggio();
        guiMonitoraggio.inviaRichiestaMonitoraggio(parametri);

        assertEquals("Formato data di inizio non valido.", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_6: Intervallo Inizio rovesciato (Data Inizio > Data Fine)")
    void tc6_intervalloInizioRovesciato() {
        simulaInputData(guiMonitoraggio, "31-01-2026", "01-01-2026");

        Map<String, Object> parametri = guiMonitoraggio.acquisisciParametriMonitoraggio();
        guiMonitoraggio.inviaRichiestaMonitoraggio(parametri);

        assertEquals("La data di inizio deve essere antecedente o uguale alla data di fine.", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_7: Data Fine futura rispetto ad oggi")
    void tc7_dataFineFutura() {
        simulaInputData(guiMonitoraggio, "01-01-2026", "31-12-2050");

        Map<String, Object> parametri = guiMonitoraggio.acquisisciParametriMonitoraggio();
        guiMonitoraggio.inviaRichiestaMonitoraggio(parametri);

        assertEquals("La data di fine non può essere nel futuro.", erroreIntercettato);
    }

    @Test
    @DisplayName("TC_8: Formato Data Fine non valido o inesistente")
    void tc8_formatoDataFineNonValido() {
        simulaInputData(guiMonitoraggio, "01-01-2026", "31-02-2026");

        Map<String, Object> parametri = guiMonitoraggio.acquisisciParametriMonitoraggio();
        guiMonitoraggio.inviaRichiestaMonitoraggio(parametri);

        assertEquals("Formato data di fine non valido o inesistente.", erroreIntercettato);
    }

    // --- METODI DI UTILITY ---

    /**
     * Recupera in modo efficiente i JTextField dal pannello della GUI per popolarli.
     */
    private void simulaInputData(GUIMonitoraggioAttivita gui, String inizio, String fine) {
        try {
            // Sfrutta la Reflection solo per accedere ai JTextField interni una volta sola nel test
            java.lang.reflect.Field fieldInizio = GUIMonitoraggioAttivita.class.getDeclaredField("txtDataInizio");
            java.lang.reflect.Field fieldFine = GUIMonitoraggioAttivita.class.getDeclaredField("txtDataFine");

            fieldInizio.setAccessible(true);
            fieldFine.setAccessible(true);

            ((JTextField) fieldInizio.get(gui)).setText(inizio);
            ((JTextField) fieldFine.get(gui)).setText(fine);
        } catch (Exception e) {
            fail("Errore nell'iniezione dell'input simulato: " + e.getMessage());
        }
    }

    /**
     * Esegue l'inserimento nel database una sola volta per ridurre i tempi di persistenza.
     */
    private static void creaSegnalazioneDiTestUnica(String titolo, int anno, int mese, int giorno) {
        try {
            EntityManager em = JpaUtil.getInstance().getEntityManager();
            em.getTransaction().begin();

            Cittadino cittadino = em.find(Cittadino.class, 1L);
            if (cittadino == null) {
                cittadino = new Cittadino("Mario", "Rossi", "3331234567", "mario.rossi@email.it", "Password123!");
                em.persist(cittadino);
            }

            Calendar cal = Calendar.getInstance();
            cal.set(anno, mese, giorno);
            java.util.Date dataSpecificata = cal.getTime();

            Segnalazione s = new Segnalazione(
                    titolo, "Descrizione Test Monitoraggio", CategoriaSegnalazione.strada_dissestata,
                    "Napoli", "80125", "Via Claudio", "21", "Fuorigrotta",
                    cittadino
            );

            em.persist(s);
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            System.err.println("Errore di inizializzazione DB (Monitoraggio): " + e.getMessage());
        }
    }
}