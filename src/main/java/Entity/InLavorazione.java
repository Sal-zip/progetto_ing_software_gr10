package Entity;

/**
 * Stato che rappresenta una segnalazione in lavorazione.
 *
 * La segnalazione non è modificabile dal cittadino.
 * Il successivo aggiornamento la porta nello stato "risolta".
 */
public class InLavorazione implements StatoSegnalazione {

    @Override
    public void aggiornaStato(Segnalazione segnalazione) {

        if (segnalazione == null) {
            throw new IllegalArgumentException("La segnalazione non può essere nulla.");
        }

        segnalazione.setStatoCorrente(
                new Risolta(),
                Segnalazione.STATO_RISOLTA,
                false
        );
    }

    @Override
    public boolean isStatoNotificabile() {
        return false;
    }
}
