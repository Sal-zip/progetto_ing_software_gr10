package Entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Specializzazione di Utente che rappresenta l'operatore comunale.
 *
 * L'operatore eredita i dati comuni da Utente e non espone
 * direttamente le credenziali contenute nella superclasse.
 */
@Entity
@DiscriminatorValue("OPERATORE_COMUNALE")
public class OperatoreComunale extends Utente {

    protected OperatoreComunale() {
        /*
         * Costruttore richiesto da Hibernate/JPA.
         */
    }

    public OperatoreComunale(String nome,
                             String cognome,
                             String recapitoTelefonico,
                             String email,
                             String password) {

        super(nome, cognome, recapitoTelefonico, email, password);
    }
}