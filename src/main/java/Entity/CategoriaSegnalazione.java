package Entity;

/**
 * Enum che rappresenta le categorie ammesse per una segnalazione.
 *
 * Viene usata come attributo della classe Segnalazione e persistita
 * tramite Hibernate con @Enumerated(EnumType.STRING).
 */
public enum CategoriaSegnalazione {

	illuminazione_guasta,
	strada_dissestata,
	rifiuti_abbandonati,
	pericolo_generico,
	arredo_urbano_danneggiato
}