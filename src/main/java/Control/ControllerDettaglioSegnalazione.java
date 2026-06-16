package Control;

import Entity.ServizioUtenti;
import Entity.ServizioSegnalazioni;

import java.util.List;
import java.util.Map;

/**
 * Coordina la visualizzazione del dettaglio e dell'evoluzione di una segnalazione.
 *
 * Il controller non accede direttamente al package Database e non manipola
 * direttamente le Entity. Recupera dati già disaccoppiati tramite ServizioUtenti.
 */
public class ControllerDettaglioSegnalazione {

	private ServizioUtenti servizioUtenti;
	private ServizioSegnalazioni servizioSegnalazioni;

	public ControllerDettaglioSegnalazione() {

		this.servizioUtenti = new ServizioUtenti();
		this.servizioSegnalazioni = new ServizioSegnalazioni();
	}

	public boolean verificaAccessoDettaglio(
			Long idUtente,
			Long idSegnalazione
	) {

		if (idUtente == null || idSegnalazione == null) {
			return false;
		}

		Map<String, Object> dettaglio =
				servizioSegnalazioni.recuperaDettaglioSegnalazione(
						idUtente,
						idSegnalazione
				);

		if (dettaglio == null || dettaglio.isEmpty()) {
			return false;
		}

		if (servizioUtenti.verificaUtenteOperatore(idUtente)) {
			return true;
		}

		if (servizioUtenti.verificaUtenteCittadino(idUtente)) {
			Long idCittadino = estraiLong(dettaglio.get("idCittadino"));

			return idCittadino != null && idCittadino.equals(idUtente);
		}

		return false;
	}

	/**
	 * Recupera il dettaglio della segnalazione.
	 *
	 * Il dettaglio viene restituito come Map dal Facade Entity ServizioUtenti.
	 * In questo modo il controller non manipola direttamente le Entity e non
	 * perde eventuali informazioni associate, come evoluzione o notifiche.
	 */
	public Map<String, Object> recuperaDettaglioSegnalazione(
			Long idUtente,
			Long idSegnalazione
	) {

		if (idUtente == null || idSegnalazione == null) {
			return null;
		}

		if (!verificaAccessoDettaglio(idUtente, idSegnalazione)) {
			return null;
		}

		Map<String, Object> dettaglio =
				servizioSegnalazioni.recuperaDettaglioSegnalazione(
						idUtente,
						idSegnalazione
				);

		if (dettaglio == null) {
			return null;
		}

		List<Map<String, Object>> evoluzione =
				servizioSegnalazioni.recuperaEvoluzioneSegnalazione(
						idUtente,
						idSegnalazione
				);

		dettaglio.put("evoluzione", evoluzione);

		return dettaglio;
	}

	public List<Map<String, Object>> recuperaEvoluzioneSegnalazione(
			Long idUtente,
			Long idSegnalazione
	) {

		if (idUtente == null || idSegnalazione == null) {
			return null;
		}

		if (!verificaAccessoDettaglio(idUtente, idSegnalazione)) {
			return null;
		}

		return servizioSegnalazioni.recuperaEvoluzioneSegnalazione(
				idUtente,
				idSegnalazione
		);
	}

	private Long estraiLong(Object valore) {

		if (valore == null) {
			return null;
		}

		if (valore instanceof Long) {
			return (Long) valore;
		}

		if (valore instanceof Integer) {
			return ((Integer) valore).longValue();
		}

		try {
			return Long.parseLong(valore.toString());
		} catch (NumberFormatException e) {
			return null;
		}
	}
}