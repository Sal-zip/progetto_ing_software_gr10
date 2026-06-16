package Entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Specializzazione di Utente che rappresenta il cittadino.
 *
 * Il cittadino eredita i dati comuni da Utente e non espone
 * direttamente le credenziali contenute nella superclasse.
 */
@Entity
@DiscriminatorValue("CITTADINO")
public class Cittadino extends Utente {

    protected Cittadino() {
        /*
         * Costruttore richiesto da Hibernate/JPA.
         */
    }

    public Cittadino(String nome,
                     String cognome,
                     String recapitoTelefonico,
                     String email,
                     String password) {

        super(nome, cognome, recapitoTelefonico, email, password);
    }
}