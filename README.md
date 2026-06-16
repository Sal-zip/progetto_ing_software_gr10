# Progetto Ingegneria del Software - GR10

## Descrizione

Progetto universitario per il corso di Ingegneria del Software, finalizzato all’analisi, alla progettazione e allo sviluppo di un sistema per la gestione delle segnalazioni relative a problemi e disservizi nelle aree comunali.

Il sistema prevede funzionalità dedicate a due principali tipologie di utenti: cittadini e operatori comunali. I cittadini possono inviare e consultare le proprie segnalazioni, mentre gli operatori comunali possono gestire le segnalazioni ricevute, aggiornarne lo stato e monitorare l’andamento complessivo delle attività.

---

## Obiettivi del progetto

L’obiettivo del progetto è applicare i principali concetti dell’Ingegneria del Software alla modellazione di un sistema informativo, con particolare attenzione a:

- analisi dei requisiti;
- individuazione degli attori e dei casi d’uso;
- modellazione del dominio applicativo;
- assegnazione delle responsabilità alle classi;
- progettazione delle interazioni tra gli oggetti;
- organizzazione della documentazione di progetto.

---

## Casi d’uso individuati

I principali casi d’uso individuati per il sistema sono:

- Registrazione Utente
- Crea Nuova Segnalazione
- Inserisci Informazioni Aggiuntive
- Consulta Segnalazioni Inviate
- Consulta Segnalazioni Ricevute
- Visualizza Dettagli Segnalazione
- Aggiorna Stato Segnalazione
- Aggiungi Nota Interna
- Monitora Andamento Attività
- Ricerca Segnalazioni

La descrizione dettagliata dei casi d’uso, comprensiva di attori coinvolti, precondizioni, postcondizioni, scenario base, scenari alternativi ed eventuali extension point, è riportata nella documentazione di progetto.

---

## Documentazione

La documentazione del progetto è organizzata nella cartella `docs/`.

La cartella `docs/` contiene:

- la documentazione testuale del progetto;
- il progetto Visual Paradigm, all’interno del quale sono contenuti i modelli UML prodotti durante le attività di analisi e progettazione.

Il progetto Visual Paradigm comprende:

- Analisi testuale dei requisiti;
- Analisi testuale dei casi d’uso;
- Analisi testuale del dominio;
- Diagramma dei casi d’uso;
- System domain model;
- System domain model raffinato con responsabilità GRASP;
- System Model - Pattern BCED;
- Diagrammi di sequenza;
- Diagrammi di sequenza raffinati;
- Diagrammi di sequenza - Pattern BCED;
- Diagrammi di attività;
- Diagrammi di attività raffinati;
- Diagramma macchina a stati finiti
- Deployment Diagram

La cartella `src/` contiene invece l’implementazione del sistema software.

---

## Struttura della repository

```text
Progetto_Ing_Software_GR10/
│
├── README.md
│
├── docs/
│   ├── documentazione-progetto/
│   └── visual-paradigm/
│
└── src/
    └── codice-sorgente/
```

---

## Configurazione del database MySQL

Per eseguire e testare il progetto su una macchina esterna è necessario disporre di un server MySQL attivo e configurare correttamente le credenziali di accesso nel file `persistence.xml`.

La configurazione seguente mostra un esempio di creazione di un database e di un utente MySQL dedicato al progetto.

### 1. Accesso a MySQL

Accedere a MySQL con un utente amministratore, ad esempio `root`:

```sql
mysql -u root -p
```

Inserire la password dell’utente amministratore quando richiesta.

---

### 2. Creazione del database

Creare un database dedicato al progetto:

```sql
CREATE DATABASE segnalazioni_db;
```

È possibile usare un nome diverso, ma in tal caso lo stesso nome dovrà essere riportato anche nel file `persistence.xml`.

---

### 3. Creazione di un utente MySQL dedicato

Creare un utente specifico per il progetto:

```sql
CREATE USER 'segnalazioni_user'@'localhost' IDENTIFIED BY 'password_sicura';
```

In questo esempio:

- `segnalazioni_user` è il nome utente MySQL;
- `password_sicura` è la password associata all’utente;
- `localhost` indica che l’accesso avviene dalla stessa macchina su cui è in esecuzione MySQL.

È possibile modificare nome utente e password, purché vengano poi aggiornati anche nel file `persistence.xml`.

---

### 4. Assegnazione dei permessi

Assegnare all’utente i permessi sul database del progetto:

```sql
GRANT ALL PRIVILEGES ON segnalazioni_db.* TO 'segnalazioni_user'@'localhost';
```

Applicare le modifiche ai permessi:

```sql
FLUSH PRIVILEGES;
```

---

### 5. Verifica dell’accesso

Uscire da MySQL:

```sql
EXIT;
```

Poi verificare che il nuovo utente riesca ad accedere:

```sql
mysql -u segnalazioni_user -p
```

Dopo l’accesso, verificare la presenza del database:

```sql
SHOW DATABASES;
```

---

## Configurazione del file `persistence.xml`

Dopo aver creato il database e l’utente MySQL, è necessario aggiornare le credenziali nel file `persistence.xml`.

Il file si trova generalmente nel percorso:

```text
src/main/resources/META-INF/persistence.xml
```

All’interno del file cercare le proprietà relative alla connessione JDBC:

```xml
<property name="jakarta.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/segnalazioni_db"/>
<property name="jakarta.persistence.jdbc.user" value="segnalazioni_user"/>
<property name="jakarta.persistence.jdbc.password" value="password_sicura"/>
<property name="jakarta.persistence.jdbc.driver" value="com.mysql.cj.jdbc.Driver"/>
```

I valori da controllare e modificare sono:

- `jakarta.persistence.jdbc.url`: contiene host, porta e nome del database;
- `jakarta.persistence.jdbc.user`: contiene il nome utente MySQL;
- `jakarta.persistence.jdbc.password`: contiene la password dell’utente MySQL;
- `jakarta.persistence.jdbc.driver`: indica il driver JDBC MySQL.

Esempio completo:

```xml
<persistence-unit name="segnalazioniPU">
    <properties>
        <property name="jakarta.persistence.jdbc.driver" value="com.mysql.cj.jdbc.Driver"/>
        <property name="jakarta.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/segnalazioni_db"/>
        <property name="jakarta.persistence.jdbc.user" value="segnalazioni_user"/>
        <property name="jakarta.persistence.jdbc.password" value="password_sicura"/>

        <property name="hibernate.dialect" value="org.hibernate.dialect.MySQLDialect"/>
        <property name="hibernate.hbm2ddl.auto" value="update"/>
        <property name="hibernate.show_sql" value="true"/>
        <property name="hibernate.format_sql" value="true"/>
    </properties>
</persistence-unit>
```

Il nome della persistence unit deve essere coerente con quello usato nel codice Java, ad esempio:

```java
Persistence.createEntityManagerFactory("segnalazioniPU");
```

---

## Note per l’esecuzione

Prima di avviare il progetto verificare che:

- il server MySQL sia attivo;
- il database indicato nel file `persistence.xml` esista;
- l’utente MySQL configurato abbia i permessi sul database;
- le credenziali nel file `persistence.xml` siano corrette;
- il nome della persistence unit coincida con quello usato nel codice;
- le dipendenze Maven siano state scaricate correttamente.

In caso di errore di connessione al database, controllare in particolare:

- nome del database;
- username e password;
- porta MySQL, solitamente `3306`;
- presenza del driver MySQL Connector/J nel file `pom.xml`;
- correttezza del percorso del file `persistence.xml`.

---

## Tecnologie utilizzate

Il progetto utilizza:

- Java;
- Swing per l’interfaccia grafica;
- JPA/Hibernate per la persistenza;
- MySQL come database relazionale;
- Maven per la gestione delle dipendenze;
- Visual Paradigm per la modellazione UML.

---

## Note finali

Il progetto è stato realizzato a scopo didattico nell’ambito del corso di Ingegneria del Software.

L’implementazione segue l’organizzazione architetturale BCED, separando le responsabilità tra Boundary, Control, Entity e Database.
