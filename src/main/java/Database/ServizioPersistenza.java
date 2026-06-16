package Database;

// ServizioPersistenza rappresenta una facade generica di persistenza che incapsula l’accesso al database e rende trasparente ai controller la gestione dei dati persistenti. La classe espone operazioni generiche di salvataggio, aggiornamento e ricerca degli oggetti, evitando che il package Control dipenda direttamente da DAO specifici o da dettagli tecnici legati a Hibernate/JPA. Questa soluzione consente di centralizzare l’accesso alla persistenza, mantenendo i controller indipendenti dall’implementazione concreta del database e favorendo una maggiore modularità architetturale.

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class ServizioPersistenza {

	private JpaUtil jpaUtil;

	public ServizioPersistenza() {
		this.jpaUtil = JpaUtil.getInstance();
	}

	/**
	 * Salva un nuovo oggetto persistente.
	 *
	 * Metodo coerente con il modello:
	 * +salva(oggetto : T) : void
	 */
	public <T> void salva(T oggetto) {

		if (oggetto == null) {
			throw new IllegalArgumentException("L'oggetto da salvare non può essere null.");
		}

		EntityManager entityManager = jpaUtil.getEntityManager();
		EntityTransaction transazione = entityManager.getTransaction();

		try {
			transazione.begin();
			entityManager.persist(oggetto);
			transazione.commit();
		} catch (RuntimeException e) {
			if (transazione.isActive()) {
				transazione.rollback();
			}

			throw e;
		} finally {
			entityManager.close();
		}
	}

	/**
	 * Aggiorna un oggetto persistente già esistente.
	 *
	 * Metodo coerente con il modello:
	 * +aggiorna(oggetto : T) : void
	 */
	public <T> void aggiorna(T oggetto) {

		if (oggetto == null) {
			throw new IllegalArgumentException("L'oggetto da aggiornare non può essere null.");
		}

		EntityManager entityManager = jpaUtil.getEntityManager();
		EntityTransaction transazione = entityManager.getTransaction();

		try {
			transazione.begin();
			entityManager.merge(oggetto);
			transazione.commit();
		} catch (RuntimeException e) {
			if (transazione.isActive()) {
				transazione.rollback();
			}

			throw e;
		} finally {
			entityManager.close();
		}
	}

	/**
	 * Cerca oggetti di una certa classe in base a un singolo attributo.
	 *
	 * Metodo coerente con il modello:
	 * +cercaPerAttributo(classe : Class<T>, attributo : String, valore : Object) : List<T>
	 *
	 * Supporta anche attributi annidati, ad esempio:
	 * "credenziali.email"
	 */
	public <T> List<T> cercaPerAttributo(
			Class<T> classe,
			String attributo,
			Object valore
	) {

		if (classe == null) {
			throw new IllegalArgumentException("La classe di ricerca non può essere null.");
		}

		if (attributo == null || attributo.trim().isEmpty()) {
			throw new IllegalArgumentException("L'attributo di ricerca non può essere vuoto.");
		}

		EntityManager entityManager = jpaUtil.getEntityManager();

		try {
			String nomeParametro = generaNomeParametro(attributo);

			String jpql = "SELECT e FROM "
					+ classe.getSimpleName()
					+ " e WHERE e."
					+ attributo
					+ " = :"
					+ nomeParametro;

			TypedQuery<T> query = entityManager.createQuery(jpql, classe);
			query.setParameter(nomeParametro, valore);

			return query.getResultList();
		} finally {
			entityManager.close();
		}
	}

	/**
	 * Cerca oggetti di una certa classe in base a più campi.
	 *
	 * Metodo coerente con il modello:
	 * +cercaPerCampi(classe : Class<T>, campiRicerca : Map<String, Object>) : List<T>
	 *
	 * Supporta anche campi annidati, ad esempio:
	 * "credenziali.email"
	 * "credenziali.password"
	 */
	public <T> List<T> cercaPerCampi(
			Class<T> classe,
			Map<String, Object> campiRicerca
	) {

		if (classe == null) {
			throw new IllegalArgumentException("La classe di ricerca non può essere null.");
		}

		EntityManager entityManager = jpaUtil.getEntityManager();

		try {
			StringBuilder jpql = new StringBuilder();

			jpql.append("SELECT e FROM ")
					.append(classe.getSimpleName())
					.append(" e");

			if (campiRicerca != null && !campiRicerca.isEmpty()) {
				jpql.append(" WHERE ");

				int indice = 0;

				for (String campo : campiRicerca.keySet()) {
					if (campo == null || campo.trim().isEmpty()) {
						throw new IllegalArgumentException("Un campo di ricerca non può essere vuoto.");
					}

					if (indice > 0) {
						jpql.append(" AND ");
					}

					String nomeParametro = generaNomeParametro(campo);

					jpql.append("e.")
							.append(campo)
							.append(" = :")
							.append(nomeParametro);

					indice++;
				}
			}

			TypedQuery<T> query = entityManager.createQuery(jpql.toString(), classe);

			if (campiRicerca != null && !campiRicerca.isEmpty()) {
				for (Map.Entry<String, Object> entry : campiRicerca.entrySet()) {
					String nomeParametro = generaNomeParametro(entry.getKey());
					query.setParameter(nomeParametro, entry.getValue());
				}
			}

			return query.getResultList();
		} finally {
			entityManager.close();
		}
	}

	/**
	 * Recupera un oggetto persistente tramite chiave primaria.
	 *
	 * Metodo coerente con il modello:
	 * +trovaPerId(classe : Class<T>, id : Long) : T
	 */
	public <T> T trovaPerId(Class<T> classe, Long id) {

		if (classe == null || id == null) {
			return null;
		}

		EntityManager entityManager = jpaUtil.getEntityManager();

		try {
			return entityManager.find(classe, id);
		} finally {
			entityManager.close();
		}
	}

	/**
	 * Converte un campo annidato in un nome valido per parametro JPQL.
	 *
	 * Esempio:
	 * "credenziali.email" diventa "credenziali_email".
	 */
	private String generaNomeParametro(String campo) {
		return campo.replace(".", "_");
	}
}
