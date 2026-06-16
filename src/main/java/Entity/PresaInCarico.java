package Entity;

/**
 * Stato che rappresenta una segnalazione presa in carico
 * da un operatore comunale.
 *
 * La segnalazione non è più modificabile dal cittadino.
 * Il successivo aggiornamento la porta nello stato "in_lavorazione".
 */
public class PresaInCarico implements StatoSegnalazione {

    @Override
    public void aggiornaStato(Segnalazione segnalazione) {

        if (segnalazione == null) {
            throw new IllegalArgumentException("La segnalazione non può essere nulla.");
        }

        segnalazione.setStatoCorrente(
                new InLavorazione(),
                Segnalazione.STATO_IN_LAVORAZIONE,
                false
        );
    }

    @Override
    public boolean isStatoNotificabile() {
        return true;
    }
}