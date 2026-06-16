package Database;

/*
 * JpaUtil rappresenta una classe di supporto tecnico del package Database,
 * responsabile della gestione dell’accesso a JPA/Hibernate.
 *
 * Il suo compito è fornire al ServizioPersistenza gli strumenti necessari
 * per interagire con il database, in particolare tramite EntityManager
 * ed EntityManagerFactory.
 *
 * In questo modo la creazione e la gestione delle risorse JPA rimangono
 * confinate nel package Database, senza propagare dettagli infrastrutturali
 * verso il livello Control.
 *
 * La classe può essere modellata come Singleton, poiché rappresenta un punto
 * centralizzato per ottenere l’accesso alla persistenza.
 */

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {

    private static JpaUtil instance;
    private EntityManagerFactory emf;

    private JpaUtil() {
        this.emf = Persistence.createEntityManagerFactory("Sistema_Gestione_Segnalazioni_Comunali");
    }

    public static JpaUtil getInstance() {
        if (instance == null) {
            instance = new JpaUtil();
        }

        return instance;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void chiudi() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}

