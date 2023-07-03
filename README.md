# StepByStep-Android-App

StepByStep è un'applicazione sviluppata per esplorare la funzionalità dei widget in Android con lo scopo di migliorare l'esperienza dell'utente e fornire funzionalità utili.

Per funzionare correttamente, l'app richiede accesso alla posizione e alle notifiche. È richiesta una versione Android pari o superiore ad Android 12 (S) (API Level 31).

## Authors

* **Filippo Galli** - [FilippoGalli001](https://github.com/FilippoGalli001)
* **Simone Peraro** - [SimonReese](https://github.com/SimonReese)
* **Giovanni Faedo** - [giova211001](https://github.com/giova211001)

## 1. Funzionalità dell'applicazione
L'applicazione consente agli utenti di monitorare e registrare le loro sessioni di camminata o corsa utilizzando i dati del GPS. Permette agli utenti di visualizzare le sessioni completate e modificare i parametri personali all'interno di una sezione di configurazione. L'applicazione è composta da quattro schermate principali:

### 1.1 Home
Fornisce agli utenti una dashboard per monitorare i principali parametri delle sessioni svolte durante la giornata. La schermata Home mostra statistiche significative per il giorno, come la distanza percorsa, i passi effettuati e la percentuale di calorie bruciate in base all'obiettivo impostato dall'utente.

### 1.2 Run
Consente all'utente di avviare e interrompere una sessione di corsa e visualizza informazioni in tempo reale sull'attività, tra cui distanza, calorie bruciate, tempo trascorso e passi. È possibile avviare il servizio di localizzazione premendo il pulsante di avvio. Al termine della sessione, i dati vengono salvati all'interno di un database.

### 1.3 Stats
Permette agli utenti di visualizzare tutte le sessioni salvate suddivise per settimana utilizzando un RecyclerView e un grafico. Gli utenti possono anche accedere ai dati delle settimane precedenti.

### 1.4 Config
Consente agli utenti di impostare informazioni personali come nome, peso, età, genere e obiettivo calorico.

<img align="center" src=./readme/preview.gif width=200px alt="">

Per facilitare l'uso, sono stati inoltre implementati tre widget:

- **ListWidget**: Fornisce una lista dinamica delle sessioni entro un determinato intervallo di tempo, come configurato nelle impostazioni del widget.

- **RunWidget**: Mostra informazioni in tempo reale sulla sessione di camminata/corsa e consente all'utente di avviare e interrompere la sessione direttamente dal widget.

- **GraphWidget**: Presenta un grafico settimanale basato su una proprietà della sessione selezionata.

## 2. Funzionalità dei widget
I tre widget implementati consentono agli utenti di visualizzare i dati memorizzati nel database e interagire direttamente con le funzioni dell'applicazione. Tutti i widget supportano il ridimensionamento dinamico in base alle dimensioni del widget stesso.

### 2.1 ListWidget
Il widget della lista delle sessioni è un widget di raccolta che mostra un elenco delle sessioni salvate dall'utente. Quando viene aggiunto alla schermata principale, visualizza tutte le sessioni registrate nella settimana corrente. Toccando una sessione si apre una visualizzazione dettagliata tramite un'Activity. Il widget adatta i dati visualizzati in base allo spazio disponibile. L'Activity di configurazione consente all'utente di selezionare l'intervallo temporale per la visualizzazione dei dati, scegliendo tra tre opzioni: sessioni al giorno, alla settimana o al mese.

### 2.2 RunWidget
Il widget di corsa è un widget di controllo che consente agli utenti di avviare la registrazione di una sessione anche senza aprire l'applicazione. La sua funzionalità rispecchia quella del Fragment Run, ma offre un'esperienza più immediata. Quando il servizio viene avviato, gli utenti possono visualizzare informazioni in tempo reale sulla loro posizione, velocità, distanza e calorie bruciate. Ridimensionando il widget, alcune di queste informazioni vengono nascoste per consentire l'interazione continua con i pulsanti di controllo. Inoltre, è presente un'attività di configurazione che consente all'utente di nascondere i dati che non si desidera visualizzare.

### 2.3 GraphWidget
Il widget del grafico è un widget informativo che consente all'utente di visualizzare grafici delle sessioni registrate durante la settimana corrente. L'immagine del grafico si adatta allo spazio disponibile durante il ridimensionamento. Gli utenti possono avviare l'attività di configurazione per selezionare il tipo di dato da rappresentare nel grafico, in base alle loro preferenze, tra distanza percorsa, calorie bruciate o durata della sessione.
