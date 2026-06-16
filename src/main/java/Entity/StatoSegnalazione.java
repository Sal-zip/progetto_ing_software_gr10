package Entity;

/**
 * Interfaccia che rappresenta il ruolo "State" nel pattern State.
 *
 * Ogni stato concreto della segnalazione implementa questa interfaccia
 * e definisce il proprio comportamento quando viene richiesto
 * l'aggiornamento dello stato.
 *
 * L'interfaccia contiene solo il metodo comportamentale, coerentemente
 * con il pattern State mostrato nell'esempio della slide.
 */
public interface StatoSegnalazione {

    /**
     * Metodo invocato dal Context, cioè dalla classe Segnalazione.
     *
     * La Segnalazione non decide direttamente quale sarà il nuovo stato,
     * ma delega allo stato corrente la responsabilità di determinare
     * se esiste una transizione ammessa.
     *
     * @param segnalazione oggetto Context sul quale lo stato concreto
     *                     può applicare la transizione.
     */
    void aggiornaStato(Segnalazione segnalazione);
    boolean isStatoNotificabile();
}
