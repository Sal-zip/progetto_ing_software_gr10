package Control;

import Entity.ServizioUtenti;

import java.util.Map;

/**
 * Controller tecnico di supporto per l'accesso dell'utente.
 *
 * L'accesso è trattato come precondizione tecnica alle funzionalità del sistema
 * e non come caso d'uso applicativo autonomo esposto dal ControllerServiziSistema.
 *
 * La classe è coerente con l'architettura BCED perché:
 * - riceve dalla GUI solo dati semplici di accesso;
 * - non accede direttamente al package Database;
 * - non usa ServizioPersistenza;
 * - non legge direttamente Credenziali;
 * - non crea Credenziali;
 * - non restituisce istanze Entity alla Boundary;
 * - delega al Facade Entity ServizioUtenti la verifica delle credenziali;
 * - restituisce alla Boundary solo dati semplici di sessione.
 */
public class ControllerAccessoUtente {

    private ServizioUtenti servizioUtenti;

    /**
     * Costruisce il controller inizializzando il Facade Entity.
     *
     * Il controller non accede direttamente alla persistenza, ma utilizza
     * ServizioUtenti come punto di accesso controllato al package Entity.
     */
    public ControllerAccessoUtente() {
        this.servizioUtenti = new ServizioUtenti();
    }

    /**
     * Verifica i dati di accesso inseriti nella GUI.
     *
     * Il metodo coordina la precondizione tecnica di autenticazione:
     * - valida formalmente email e password;
     * - delega a ServizioUtenti il recupero dei dati dell'utente autenticato;
     * - riceve una Map con i dati semplici di sessione;
     * - restituisce tali dati alla Boundary.
     *
     * Il controller non restituisce un'istanza Entity di Utente, in modo da
     * mantenere chiusa la separazione BCED tra Boundary, Control, Entity
     * e Database.
     *
     * @param datiAccesso mappa contenente email e password acquisite dalla GUI
     * @return mappa dei dati semplici di sessione, oppure null se l'accesso fallisce
     */
    public Map<String, Object> verificaCredenzialiAccesso(Map<String, Object> datiAccesso) {

        if (!validaDatiAccesso(datiAccesso)) {
            return null;
        }

        Map<String, Object> datiSessioneUtente = servizioUtenti.recuperaDatiUtenteDaCredenziali(datiAccesso);

        if (!verificaDatiSessioneUtente(datiSessioneUtente)) {
            return null;
        }

        return datiSessioneUtente;
    }

    /**
     * Verifica che i dati di sessione restituiti dal Facade Entity siano validi.
     *
     * Il metodo non accede al Database e non recupera direttamente Entity:
     * controlla soltanto che la Map restituita da ServizioUtenti contenga
     * almeno l'identificativo dell'utente autenticato e il ruolo necessario
     * alla configurazione della dashboard.
     *
     * @param datiSessioneUtente mappa contenente i dati semplici dell'utente autenticato
     * @return true se i dati di sessione sono utilizzabili, false altrimenti
     */
    private boolean verificaDatiSessioneUtente(Map<String, Object> datiSessioneUtente) {

        if (datiSessioneUtente == null) {
            return false;
        }

        Object idUtente = datiSessioneUtente.get("idUtente");
        String ruolo = valoreTestuale(datiSessioneUtente.get("ruolo"));

        return idUtente != null && !isVuoto(ruolo);
    }

    /**
     * Verifica la presenza formale dei dati necessari all'accesso.
     *
     * Questo metodo non verifica se le credenziali sono corrette:
     * controlla soltanto che email e password siano state fornite.
     *
     * La verifica effettiva dell'utente registrato viene delegata
     * a ServizioUtenti.
     *
     * @param datiAccesso mappa contenente i dati acquisiti dalla Boundary
     * @return true se email e password sono presenti, false altrimenti
     */
    private boolean validaDatiAccesso(Map<String, Object> datiAccesso) {

        if (datiAccesso == null) {
            return false;
        }

        String email = valoreTestuale(datiAccesso.get("email"));
        String password = valoreTestuale(datiAccesso.get("password"));

        return !isVuoto(email) && !isVuoto(password);
    }

    /**
     * Converte un valore generico in una stringa normalizzata.
     *
     * Metodo privato di supporto usato dal controller per leggere in modo
     * sicuro i dati provenienti dalla Map della Boundary.
     *
     * @param valore valore da convertire
     * @return stringa senza spazi iniziali/finali, oppure stringa vuota se il valore è nullo
     */
    private String valoreTestuale(Object valore) {
        if (valore == null) {
            return "";
        }

        return valore.toString().trim();
    }

    /**
     * Verifica se una stringa è nulla o vuota.
     *
     * Metodo privato di supporto alla validazione preliminare dei dati
     * ricevuti dalla Boundary. Non rappresenta una responsabilità di dominio
     * esposta nel modello BCED.
     *
     * @param valore stringa da verificare
     * @return true se il valore è nullo o vuoto, false altrimenti
     */
    private boolean isVuoto(String valore) {
        return valore == null || valore.trim().isEmpty();
    }
}