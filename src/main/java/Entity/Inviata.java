package Entity;

/**
 * Stato iniziale della segnalazione.
 *
 * Una segnalazione in stato "inviata" è ancora modificabile
 * dal cittadino. Quando viene aggiornata, passa a "presa_in_carico"
 * e non è più modificabile dal cittadino.
 */
public class Inviata implements StatoSegnalazione {

    @Override
    public void aggiornaStato(Segnalazione segnalazione) {

        if (segnalazione == null) {
            throw new IllegalArgumentException("La segnalazione non può essere nulla.");
        }

        segnalazione.setStatoCorrente(
                new PresaInCarico(),
                Segnalazione.STATO_PRESA_IN_CARICO,
                false
        );
    }

    @Override
    public boolean isStatoNotificabile() {
        return false;
    }
}