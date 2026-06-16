package Entity;

import Database.ServizioPersistenza;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

    /**
     * ServizioSegnalazioni svolge il ruolo di Facade Entity dedicato alla gestione
     * delle segnalazioni del sistema.
     *
     * La classe espone verso il livello Control le operazioni relative a:
     * - creazione di nuove segnalazioni;
     * - consultazione delle segnalazioni inviate;
     * - consultazione delle segnalazioni ricevute o gestibili;
     * - visualizzazione dei dettagli di una segnalazione;
     * - recupero dell'evoluzione degli stati;
     * - aggiornamento dello stato tramite State Pattern;
     * - registrazione di note interne;
     * - generazione di notifiche di cambio stato;
     * - generazione dei risultati di monitoraggio.
     *
     * La classe è coerente con l'architettura BCED perché:
     * - non comunica direttamente con le Boundary;
     * - non gestisce componenti grafici;
     * - non accede direttamente ai Controller;
     * - incapsula l'accesso alla persistenza tramite ServizioPersistenza;
     * - non espone oggetti contenuti come Posizione, NotaInterna o AggiornamentoStato;
     * - restituisce al livello Control dati semplici o mappe di dati;
     * - mantiene nel package Entity la logica applicativa legata alle segnalazioni.
     *
     * Dopo la separazione dei Facade Entity, le responsabilità relative a utenti,
     * credenziali, accesso, registrazione e verifica del ruolo restano in
     * ServizioUtenti.
     */
    public class ServizioSegnalazioni {

        private ServizioPersistenza servizioPersistenza;

        /**
         * Costruisce il Facade Entity dedicato alle segnalazioni.
         *
         * Il Facade utilizza ServizioPersistenza come punto di accesso controllato
         * al package Database, senza esporre la persistenza ai Controller.
         */
        public ServizioSegnalazioni() {
            this.servizioPersistenza = new ServizioPersistenza();
        }

        /**
         * Registra una nuova segnalazione creata da un cittadino autenticato.

         * Il metodo recupera il cittadino, estrae dalla Map solo dati semplici
         * provenienti dalla Boundary e delega alla Entity Segnalazione la creazione
         * degli oggetti contenuti, come Posizione, e l'assegnazione dello stato iniziale.
         * Il metodo non espone oggetti contenuti e non costruisce direttamente
         * Posizione, NotaInterna o AggiornamentoStato.

         * @param idUtente identificativo dell'utente autenticato
         * @param datiSegnalazione dati semplici della segnalazione
         * @return id della segnalazione registrata, oppure null in caso di errore
         */

        public Long registraNuovaSegnalazione(
                Long idUtente,
                Map<String, Object> datiSegnalazione
        ) {

            if (idUtente == null) {
                System.err.println("[ServizioSegnalazioni] Registrazione annullata: idUtente nullo.");
                return null;
            }
            if (datiSegnalazione == null) {
                System.err.println("[ServizioSegnalazioni] Registrazione annullata: datiSegnalazione null.");
                return null;
            }
            try {
                Utente utente = servizioPersistenza.trovaPerId(
                        Utente.class,
                        idUtente
                );

                if (!(utente instanceof Cittadino)) {
                    System.err.println("[ServizioSegnalazioni] Utente non trovato o non cittadino. idUtente = " + idUtente);
                    return null;
                }
                Cittadino cittadino = (Cittadino) utente;

                String titolo = estraiStringa(datiSegnalazione.get("titolo"));
                String descrizione = estraiStringa(datiSegnalazione.get("descrizione"));
                CategoriaSegnalazione categoria = null;

                if (datiSegnalazione.get("categoria") instanceof CategoriaSegnalazione) {
                    categoria = (CategoriaSegnalazione) datiSegnalazione.get("categoria");
                }

                String citta = estraiStringa(datiSegnalazione.get("citta"));
                String cap = estraiStringa(datiSegnalazione.get("cap"));
                String strada = estraiStringa(datiSegnalazione.get("strada"));
                String numeroCivico = estraiStringa(datiSegnalazione.get("numeroCivico"));
                String areaComunale = estraiStringa(datiSegnalazione.get("areaComunale"));

                // --- VALIDAZIONE DATI OBBLIGATORI ---
                if (isVuoto(titolo)
                        || isVuoto(descrizione)
                        || categoria == null
                        || isVuoto(citta)
                        || isVuoto(cap)
                        || isVuoto(strada)
                        || isVuoto(numeroCivico)
                        || isVuoto(areaComunale)) {

                    System.err.println("[ServizioSegnalazioni] Dati obbligatori non validi.");
                    return null;
                }

                Segnalazione segnalazione = new Segnalazione(
                        titolo,
                        descrizione,
                        categoria,
                        citta,
                        cap,
                        strada,
                        numeroCivico,
                        areaComunale,
                        cittadino
                );

                // --- 2. ASSEGNAZIONE PARAMETRI FACOLTATIVI TRAMITE SETTER ---

                // Gestione della Data (Se fornita usa quella, altrimenti usa la data corrente)
                Date dataSegnalazione = null;
                if (datiSegnalazione.get("dataSegnalazione") instanceof Date) {
                    dataSegnalazione = (Date) datiSegnalazione.get("dataSegnalazione");
                }

                if (dataSegnalazione != null) {
                    segnalazione.setDataSegnalazione(dataSegnalazione);
                }

                // Gestione dell'Immagine Allegata
                String immagineAllegata = estraiStringa(datiSegnalazione.get("immagineAllegata"));
                if (!isVuoto(immagineAllegata)) {
                    segnalazione.setImmagineAllegata(immagineAllegata);
                } // Se è vuoto, non chiamiamo il setter (rimane il null di default impostato dal costruttore)

                // --- 3. PERSISTENZA ---
                servizioPersistenza.salva(segnalazione);

                if (segnalazione.getIdSegnalazione() == null) {
                    System.err.println("[ServizioSegnalazioni] Segnalazione salvata ma idSegnalazione non valorizzato.");
                    return null;
                }
                return segnalazione.getIdSegnalazione();

            } catch (RuntimeException e) {
                System.err.println("[ServizioSegnalazioni] Errore durante la registrazione della segnalazione.");
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Recupera le segnalazioni inviate dal cittadino autenticato.
         *
         * Il metodo restituisce una lista di mappe contenenti dati semplici, senza
         * esporre direttamente le istanze Entity al livello Control o Boundary.
         *
         * @param idUtente identificativo del cittadino autenticato
         * @return lista di mappe relative alle segnalazioni inviate
         */
        public List<Map<String, Object>> recuperaSegnalazioniInviate(Long idUtente) {

            if (idUtente == null) {
                return null;
            }

            Map<String, Object> campiRicerca = new HashMap<>();
            campiRicerca.put("cittadinoSegnalante.idUtente", idUtente);

            List<Segnalazione> segnalazioni = servizioPersistenza.cercaPerCampi(
                    Segnalazione.class,
                    campiRicerca
            );

            return mappaElencoSegnalazioni(segnalazioni);
        }

        /**
         * Recupera le segnalazioni ricevute o gestibili dall'operatore comunale.
         *
         * I criteri di filtro vengono passati come mappa di valori semplici.
         * In caso di filtro sullo stato, il campo corretto è nomeStatoCorrente,
         * coerente con lo State Pattern adottato da Segnalazione.
         *
         * @param idUtente identificativo dell'operatore autenticato
         * @param criteriFiltro criteri di filtro selezionati nella Boundary
         * @return lista di mappe relative alle segnalazioni ricevute o gestibili
         */
        public List<Map<String, Object>> recuperaSegnalazioniRicevute(
                Long idUtente,
                Map<String, Object> criteriFiltro
        ) {

            if (idUtente == null) {
                return null;
            }

            Map<String, Object> campiRicerca = new HashMap<>();

            if (criteriFiltro != null) {
                campiRicerca.putAll(criteriFiltro);
            }

            List<Segnalazione> segnalazioni = servizioPersistenza.cercaPerCampi(
                    Segnalazione.class,
                    campiRicerca
            );

            return mappaElencoSegnalazioni(segnalazioni);
        }

        /**
         * Recupera il dettaglio di una segnalazione.
         *
         * Il metodo restituisce una mappa di dati semplici. Posizione e NotaInterna
         * non vengono esposte direttamente tramite getter pubblici. Le notifiche
         * associate vengono recuperate tramite query dedicata e mappate in forma
         * semplice.
         *
         * @param idUtente identificativo dell'utente autenticato
         * @param idSegnalazione identificativo della segnalazione richiesta
         * @return mappa contenente i dati semplici del dettaglio, oppure null se non trovato
         */
        public Map<String, Object> recuperaDettaglioSegnalazione(
                Long idUtente,
                Long idSegnalazione
        ) {

            if (idUtente == null || idSegnalazione == null) {
                return null;
            }

            Segnalazione segnalazione = servizioPersistenza.trovaPerId(
                    Segnalazione.class,
                    idSegnalazione
            );

            if (segnalazione == null) {
                return null;
            }

            Map<String, Object> dettaglio = mappaSegnalazione(segnalazione);

            Map<String, Object> campiRicercaNotifiche = new HashMap<>();
            campiRicercaNotifiche.put("segnalazione.idSegnalazione", idSegnalazione);

            List<Notifica> notifiche = servizioPersistenza.cercaPerCampi(
                    Notifica.class,
                    campiRicercaNotifiche
            );

            dettaglio.put("notifiche", mappaNotificheSegnalazione(notifiche));

            /*
             * NotaInterna è contenuta strettamente in Segnalazione.
             * In questa versione del Facade non viene esposta direttamente.
             * Se nel BCED viene modellata una responsabilità esplicita di lettura
             * delle note interne, la mappatura dovrà restare controllata.
             */
            Map<String, Object> campiRicercaNote = new HashMap<>();
            campiRicercaNote.put("segnalazione.idSegnalazione", idSegnalazione);

            List<NotaInterna> note = servizioPersistenza.cercaPerCampi(
                    NotaInterna.class,
                    campiRicercaNote
            );

            dettaglio.put("noteInterne", mappaNoteInterneSegnalazione(note));

            return dettaglio;
        }


        /**
         * Recupera l'evoluzione degli stati di una segnalazione.
         *
         * AggiornamentoStato è creato internamente da Segnalazione durante la
         * transizione di stato. Il Facade non accede alla lista interna della
         * Segnalazione, ma recupera gli aggiornamenti tramite query dedicata e li
         * mappa in dati semplici.
         *
         * @param idUtente identificativo dell'utente autenticato
         * @param idSegnalazione identificativo della segnalazione
         * @return lista di mappe contenenti l'evoluzione degli stati
         */
        public List<Map<String, Object>> recuperaEvoluzioneSegnalazione(
                Long idUtente,
                Long idSegnalazione
        ) {

            if (idUtente == null || idSegnalazione == null) {
                return null;
            }

            Segnalazione segnalazione = servizioPersistenza.trovaPerId(
                    Segnalazione.class,
                    idSegnalazione
            );

            if (segnalazione == null) {
                return null;
            }

            Map<String, Object> campiRicerca = new HashMap<>();
            campiRicerca.put("segnalazione.idSegnalazione", idSegnalazione);

            List<AggiornamentoStato> aggiornamenti = servizioPersistenza.cercaPerCampi(
                    AggiornamentoStato.class,
                    campiRicerca
            );

            return mappaAggiornamentiStato(aggiornamenti);
        }

        /**
         * Aggiorna lo stato di una segnalazione applicando lo State Pattern.
         *
         * Il Facade non riceve il nuovo stato dalla Boundary o dal Control.
         * La transizione viene determinata internamente dalla Segnalazione tramite
         * il metodo aggiornaStato(), che delega allo stato corrente concreto.
         *
         * AggiornamentoStato viene creato internamente da Segnalazione, che ne
         * controlla il ciclo di vita.
         *
         * @param idUtente identificativo dell'operatore autenticato
         * @param idSegnalazione identificativo della segnalazione da aggiornare
         * @return nome del nuovo stato raggiunto, oppure null se l'aggiornamento fallisce
         */
        public String aggiornaStatoSegnalazione(
                Long idUtente,
                Long idSegnalazione
        ) {

            if (idUtente == null || idSegnalazione == null) {
                return null;
            }

            try {
                OperatoreComunale operatore = servizioPersistenza.trovaPerId(
                        OperatoreComunale.class,
                        idUtente
                );

                if (operatore == null) {
                    return null;
                }

                Segnalazione segnalazione = servizioPersistenza.trovaPerId(
                        Segnalazione.class,
                        idSegnalazione
                );

                if (segnalazione == null) {
                    return null;
                }

                String statoPrecedente = segnalazione.getNomeStatoCorrente();

                segnalazione.assegnaOperatore(operatore);
                segnalazione.aggiornaStato();

                String nuovoStato = segnalazione.getNomeStatoCorrente();

                if (isVuoto(nuovoStato) || nuovoStato.equals(statoPrecedente)) {
                    return null;
                }

                servizioPersistenza.aggiorna(segnalazione);

                return nuovoStato;

            } catch (RuntimeException e) {
                System.err.println("[ServizioSegnalazioni] Errore durante l'aggiornamento stato: "
                        + e.getMessage());
                return null;
            }
        }

        /**
         * Registra una nota interna associata a una segnalazione.
         *
         * NotaInterna è contenuta strettamente in Segnalazione. Il Facade non
         * crea direttamente NotaInterna e non la restituisce. La creazione viene
         * delegata a Segnalazione tramite aggiungiNotaInterna(...).
         *
         * @param idUtente identificativo dell'operatore autenticato
         * @param idSegnalazione identificativo della segnalazione
         * @param testoNota testo della nota interna
         * @return true se la nota viene registrata correttamente, false altrimenti
         */
        public boolean registraNotaInterna(
                Long idUtente,
                Long idSegnalazione,
                String testoNota
        ) {

            if (idUtente == null || idSegnalazione == null || isVuoto(testoNota)) {
                return false;
            }

            try {
                OperatoreComunale operatore = servizioPersistenza.trovaPerId(
                        OperatoreComunale.class,
                        idUtente
                );

                if (operatore == null) {
                    return false;
                }

                Segnalazione segnalazione = servizioPersistenza.trovaPerId(
                        Segnalazione.class,
                        idSegnalazione
                );

                if (segnalazione == null) {
                    return false;
                }

                segnalazione.aggiungiNotaInterna(
                        testoNota,
                        operatore
                );

                servizioPersistenza.aggiorna(segnalazione);

                return true;

            } catch (RuntimeException e) {
                System.err.println("[ServizioSegnalazioni] Errore durante la registrazione della nota interna: "
                        + e.getMessage());
                return false;
            }
        }

        /**
         * Genera una notifica di cambio stato per il cittadino segnalante.
         *
         * Il metodo esegue un controllo protettivo finale invocando il metodo pubblico
         * di verifica per assicurarsi che lo stato della segnalazione sia effettivamente notificabile.
         *
         * @param idSegnalazione identificativo della segnalazione aggiornata
         * @param nuovoStato nome dello stato raggiunto dalla segnalazione
         * @return id della notifica generata, oppure null se la generazione fallisce
         */
        public Long generaNotificaCambioStato(
                Long idSegnalazione,
                String nuovoStato
        ) {

            if (idSegnalazione == null || isVuoto(nuovoStato)) {
                return null;
            }

            // Usiamo direttamente il nuovo metodo di verifica che hai definito!
            if (!verificaStatoNotificabile(idSegnalazione, nuovoStato)) {
                return null;
            }

            // Se la verifica passa, carichiamo la segnalazione per costruire la Notifica
            Segnalazione segnalazione = servizioPersistenza.trovaPerId(
                    Segnalazione.class,
                    idSegnalazione
            );

            if (segnalazione == null || segnalazione.getCittadinoSegnalante() == null) {
                return null;
            }

            String messaggio = "La segnalazione "
                    + idSegnalazione
                    + " è stata aggiornata allo stato: "
                    + nuovoStato;

            try {
                Notifica notifica = new Notifica(
                        new Date(),
                        messaggio,
                        nuovoStato,
                        segnalazione.getCittadinoSegnalante(),
                        segnalazione
                );

                servizioPersistenza.salva(notifica);

                return notifica.getIdNotifica();

            } catch (RuntimeException e) {
                System.err.println("[ServizioSegnalazioni] Errore durante la generazione della notifica: "
                        + e.getMessage());
                return null;
            }
        }

        /**
         * Genera i risultati aggregati per il monitoraggio delle attività.
         *
         * Il metodo riceve criteri semplici dalla Boundary tramite il Controller
         * e restituisce una mappa contenente dati aggregati e, quando richiesto,
         * l'elenco delle segnalazioni risultanti.
         *
         * Per il filtro sull'area comunale si usa il path JPA
         * "posizione.areaComunale", senza esporre l'oggetto Posizione.
         *
         * Per il filtro sullo stato si usa "nomeStatoCorrente", coerente con
         * lo State Pattern.
         *
         * @param idUtente identificativo dell'operatore autenticato
         * @param criteriMonitoraggio criteri selezionati nella GUI di monitoraggio
         * @return mappa dei risultati aggregati, oppure null se il monitoraggio fallisce
         */
        public Map<String, Object> generaRisultatiMonitoraggio(
                Long idUtente,
                Map<String, Object> criteriMonitoraggio
        ) {

            if (idUtente == null || criteriMonitoraggio == null) {
                return null;
            }

            Map<String, Object> criteriRicerca = new HashMap<>();

            Object dataInizio = criteriMonitoraggio.get("dataInizio");
            Object dataFine = criteriMonitoraggio.get("dataFine");
            Object categoria = criteriMonitoraggio.get("categoria");
            Object areaComunale = criteriMonitoraggio.get("posizione.areaComunale");
            Object statoCorrente = criteriMonitoraggio.get("nomeStatoCorrente");

            String tipoMonitoraggio = estraiStringa(
                    criteriMonitoraggio.get("tipoMonitoraggio")
            );

            if (categoria != null) {
                criteriRicerca.put("categoria", categoria);
            }

            if (areaComunale != null && !isVuoto(estraiStringa(areaComunale))) {
                criteriRicerca.put("posizione.areaComunale", areaComunale);
            }

            if (statoCorrente != null && !isVuoto(estraiStringa(statoCorrente))) {
                criteriRicerca.put("nomeStatoCorrente", statoCorrente);
            }

            List<Segnalazione> segnalazioni = servizioPersistenza.cercaPerCampi(
                    Segnalazione.class,
                    criteriRicerca
            );

            List<Segnalazione> segnalazioniFiltrate = filtraSegnalazioniPerIntervallo(
                    segnalazioni,
                    dataInizio,
                    dataFine
            );

            Map<String, Object> risultati = new HashMap<>();

            risultati.put("tipoMonitoraggio", tipoMonitoraggio);
            risultati.put("totaleSegnalazioni", contaSegnalazioni(segnalazioniFiltrate));
            risultati.put("segnalazioniRisolte", contaSegnalazioniRisolte(segnalazioniFiltrate));
            risultati.put("segnalazioniAperte", contaSegnalazioniAperte(segnalazioniFiltrate));
            risultati.put("conteggioPerCategoria", calcolaDistribuzionePerCategoria(segnalazioniFiltrate));

            if ("inLavorazione".equals(tipoMonitoraggio)
                    || "riepilogoZona".equals(tipoMonitoraggio)) {
                risultati.put("segnalazioniRisultato", mappaElencoSegnalazioni(segnalazioniFiltrate));
            } else {
                risultati.put("segnalazioniRisultato", new java.util.ArrayList<Map<String, Object>>());
            }

            return risultati;
        }


        public boolean verificaStatoNotificabile(Long idSegnalazione, String nuovoStato) {
            if (idSegnalazione == null) {
                return false;
            }

            Segnalazione segnalazione = servizioPersistenza.trovaPerId(Segnalazione.class, idSegnalazione);
            if (segnalazione == null) {
                return false;
            }

            // Delega polimorficamente alla Entity che interroga lo State Pattern
            return segnalazione.isStatoCorrenteNotificabile();
        }

        /**
         * Mappa una lista di segnalazioni in una lista di mappe di dati semplici.
         *
         * @param segnalazioni lista di segnalazioni da mappare
         * @return lista di mappe contenenti dati semplici
         */
        private List<Map<String, Object>> mappaElencoSegnalazioni(
                List<Segnalazione> segnalazioni
        ) {

            List<Map<String, Object>> elencoMappato = new java.util.ArrayList<>();

            if (segnalazioni == null) {
                return elencoMappato;
            }

            for (Segnalazione segnalazione : segnalazioni) {
                elencoMappato.add(mappaSegnalazione(segnalazione));
            }

            return elencoMappato;
        }

        /**
         * Mappa una segnalazione in una Map di dati semplici.
         *
         * Il metodo non espone direttamente oggetti contenuti come Posizione,
         * NotaInterna o AggiornamentoStato. Vengono restituiti solo valori semplici
         * necessari alla visualizzazione nella Boundary.
         *
         * @param segnalazione segnalazione da mappare
         * @return mappa contenente i dati principali della segnalazione
         */
        private Map<String, Object> mappaSegnalazione(Segnalazione segnalazione) {

            Map<String, Object> dati = new HashMap<>();

            if (segnalazione == null) {
                return dati;
            }

            dati.put("idSegnalazione", segnalazione.getIdSegnalazione());
            dati.put("titolo", segnalazione.getTitolo());
            dati.put("descrizione", segnalazione.getDescrizione());
            dati.put("categoria", segnalazione.getCategoria());
            dati.put("dataSegnalazione", segnalazione.getDataSegnalazione());
            dati.put("immagineAllegata", segnalazione.getImmagineAllegata());

            dati.put("statoCorrente", segnalazione.getNomeStatoCorrente());
            dati.put("nomeStatoCorrente", segnalazione.getNomeStatoCorrente());
            dati.put("modificabileDalCittadino", segnalazione.isModificabileDalCittadino());

            /*
             * Posizione è contenuta strettamente in Segnalazione.
             * Non viene esposto l'oggetto Posizione, ma solo i suoi valori semplici,
             * recuperati tramite metodi controllati della Segnalazione.
             */
            dati.put("citta", segnalazione.getCitta());
            dati.put("cap", segnalazione.getCap());
            dati.put("strada", segnalazione.getStrada());
            dati.put("numeroCivico", segnalazione.getNumeroCivico());
            dati.put("areaComunale", segnalazione.getAreaComunale());

            if (segnalazione.getCittadinoSegnalante() != null) {
                dati.put("idCittadino", segnalazione.getCittadinoSegnalante().getIdUtente());
            } else {
                dati.put("idCittadino", null);
            }

            if (segnalazione.getOperatore() != null) {
                dati.put("idOperatore", segnalazione.getOperatore().getIdUtente());
            } else {
                dati.put("idOperatore", null);
            }

            return dati;
        }

        /**
         * Mappa una lista di notifiche associate alla segnalazione.
         *
         * @param notifiche notifiche da mappare
         * @return lista di mappe contenenti dati semplici delle notifiche
         */
        private List<Map<String, Object>> mappaNotificheSegnalazione(
                List<Notifica> notifiche
        ) {

            List<Map<String, Object>> notificheMappate = new java.util.ArrayList<>();

            if (notifiche == null) {
                return notificheMappate;
            }

            for (Notifica notifica : notifiche) {
                notificheMappate.add(mappaNotifica(notifica));
            }

            return notificheMappate;
        }

        /**
         * Mappa una notifica in una Map di dati semplici.
         *
         * @param notifica notifica da mappare
         * @return mappa contenente i dati principali della notifica
         */
        private Map<String, Object> mappaNotifica(Notifica notifica) {

            Map<String, Object> dati = new HashMap<>();

            if (notifica == null) {
                return dati;
            }

            dati.put("idNotifica", notifica.getIdNotifica());
            dati.put("dataInvio", notifica.getDataInvio());
            dati.put("messaggio", notifica.getMessaggio());
            dati.put("statoNotificato", notifica.getStatoNotificato());

            if (notifica.getSegnalazione() != null) {
                dati.put("idSegnalazione", notifica.getSegnalazione().getIdSegnalazione());
            } else {
                dati.put("idSegnalazione", null);
            }

            return dati;
        }

        /**
         * Mappa gli aggiornamenti di stato in dati semplici.
         *
         * @param aggiornamenti lista degli aggiornamenti di stato
         * @return lista di mappe contenenti dati semplici dell'evoluzione
         */
        private List<Map<String, Object>> mappaAggiornamentiStato(
                List<AggiornamentoStato> aggiornamenti
        ) {

            List<Map<String, Object>> aggiornamentiMappati = new java.util.ArrayList<>();

            if (aggiornamenti == null) {
                return aggiornamentiMappati;
            }

            for (AggiornamentoStato aggiornamento : aggiornamenti) {
                Map<String, Object> dati = new HashMap<>();

                if (aggiornamento != null) {
                    dati.put("idAggiornamento", aggiornamento.getIdAggiornamento());
                    dati.put("dataAggiornamento", aggiornamento.getDataAggiornamento());
                    dati.put("nuovoStato", aggiornamento.getNuovoStato());

                    if (aggiornamento.getOperatore() != null) {
                        dati.put("idOperatore", aggiornamento.getOperatore().getIdUtente());
                    } else {
                        dati.put("idOperatore", null);
                    }
                }

                aggiornamentiMappati.add(dati);
            }

            return aggiornamentiMappati;
        }

        /**
         * Filtra le segnalazioni rispetto a un intervallo temporale.
         *
         * @param segnalazioni lista di segnalazioni da filtrare
         * @param dataInizio data iniziale opzionale
         * @param dataFine data finale opzionale
         * @return lista filtrata
         */
        private List<Segnalazione> filtraSegnalazioniPerIntervallo(
                List<Segnalazione> segnalazioni,
                Object dataInizio,
                Object dataFine
        ) {

            List<Segnalazione> filtrate = new java.util.ArrayList<>();

            if (segnalazioni == null) {
                return filtrate;
            }

            Date inizio = null;
            Date fine = null;

            if (dataInizio instanceof Date) {
                inizio = (Date) dataInizio;
            }

            if (dataFine instanceof Date) {
                fine = (Date) dataFine;
            }

            for (Segnalazione segnalazione : segnalazioni) {
                if (segnalazione == null) {
                    continue;
                }

                if (rientraInIntervallo(
                        segnalazione.getDataSegnalazione(),
                        inizio,
                        fine
                )) {
                    filtrate.add(segnalazione);
                }
            }

            return filtrate;
        }

        /**
         * Verifica se una data rientra in un intervallo.
         *
         * @param data data da verificare
         * @param inizio limite inferiore opzionale
         * @param fine limite superiore opzionale
         * @return true se la data rientra nell'intervallo, false altrimenti
         */
        private boolean rientraInIntervallo(
                Date data,
                Date inizio,
                Date fine
        ) {

            if (data == null) {
                return false;
            }

            if (inizio != null && data.before(inizio)) {
                return false;
            }

            if (fine != null && data.after(fine)) {
                return false;
            }

            return true;
        }

        /**
         * Conta il numero totale di segnalazioni.
         *
         * @param segnalazioni lista di segnalazioni
         * @return numero di segnalazioni
         */
        private int contaSegnalazioni(List<Segnalazione> segnalazioni) {

            if (segnalazioni == null) {
                return 0;
            }

            return segnalazioni.size();
        }

        /**
         * Conta le segnalazioni risolte.
         *
         * @param segnalazioni lista di segnalazioni
         * @return numero di segnalazioni risolte
         */
        private int contaSegnalazioniRisolte(List<Segnalazione> segnalazioni) {

            if (segnalazioni == null) {
                return 0;
            }

            int conteggio = 0;

            for (Segnalazione segnalazione : segnalazioni) {
                if (segnalazione != null && segnalazione.isRisolta()) {
                    conteggio++;
                }
            }

            return conteggio;
        }

        /**
         * Conta le segnalazioni non risolte.
         *
         * @param segnalazioni lista di segnalazioni
         * @return numero di segnalazioni aperte
         */
        private int contaSegnalazioniAperte(List<Segnalazione> segnalazioni) {

            if (segnalazioni == null) {
                return 0;
            }

            int conteggio = 0;

            for (Segnalazione segnalazione : segnalazioni) {
                if (segnalazione != null && !segnalazione.isRisolta()) {
                    conteggio++;
                }
            }

            return conteggio;
        }

        /**
         * Calcola la distribuzione delle segnalazioni per categoria.
         *
         * @param segnalazioni lista di segnalazioni
         * @return mappa categoria-conteggio
         */
        private Map<CategoriaSegnalazione, Integer> calcolaDistribuzionePerCategoria(
                List<Segnalazione> segnalazioni
        ) {

            Map<CategoriaSegnalazione, Integer> distribuzione = new HashMap<>();

            if (segnalazioni == null) {
                return distribuzione;
            }

            for (Segnalazione segnalazione : segnalazioni) {
                if (segnalazione == null || segnalazione.getCategoria() == null) {
                    continue;
                }

                CategoriaSegnalazione categoria = segnalazione.getCategoria();

                distribuzione.put(
                        categoria,
                        distribuzione.getOrDefault(categoria, 0) + 1
                );
            }

            return distribuzione;
        }

        /**
         * Converte un valore generico in stringa normalizzata.
         *
         * @param valore valore da convertire
         * @return stringa normalizzata oppure null se il valore è nullo
         */
        private String estraiStringa(Object valore) {

            if (valore == null) {
                return null;
            }

            return valore.toString().trim();
        }

        /**
         * Verifica se una stringa è nulla o vuota.
         *
         * @param valore stringa da verificare
         * @return true se il valore è nullo o vuoto, false altrimenti
         */
        private boolean isVuoto(String valore) {
            return valore == null || valore.trim().isEmpty();
        }

        /**
         * Mappa una lista di note interne associate alla segnalazione in dati semplici.
         */
        private List<Map<String, Object>> mappaNoteInterneSegnalazione(
                List<NotaInterna> note
        ) {
            List<Map<String, Object>> noteMappate = new java.util.ArrayList<>();

            if (note == null) {
                return noteMappate;
            }

            for (NotaInterna nota : note) {
                Map<String, Object> dati = new HashMap<>();

                if (nota != null) {
                    dati.put("idNota", nota.getIdNota());
                    dati.put("dataInserimento", nota.getDataInserimento());
                    dati.put("testoNota", nota.getTesto()); // Mappa il testo nel campo 'testoNota' voluto dalla GUI

                    if (nota.getOperatore() != null) {
                        dati.put("idOperatore", nota.getOperatore().getIdUtente());
                    } else {
                        dati.put("idOperatore", null);
                    }
                }

                noteMappate.add(dati);
            }

            return noteMappate;
        }
    }

