package Entity;

/**
 * Stato finale della segnalazione.
 *
 * Una segnalazione risolta non può più essere modificata
 * dal cittadino e non prevede ulteriori transizioni.
 */
public class Risolta implements StatoSegnalazione {

    @Override
    public void aggiornaStato(Segnalazione segnalazione) {
        throw new IllegalStateException(
                "Una segnalazione risolta non può avanzare ulteriormente di stato."
        );
    }

    @Override
    public boolean isStatoNotificabile() {
        return true;
    }

}