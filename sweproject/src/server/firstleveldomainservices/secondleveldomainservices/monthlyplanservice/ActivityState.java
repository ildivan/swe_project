package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice;

public enum ActivityState {
    PROPOSTA("Visita proposta: visualizzata agli utenti e in attesa di iscrizioni."),
    COMPLETA("Visita completa: ha raggiunto il numero massimo di partecipanti, non è più visualizzata."),
    CONFERMATA("Visita confermata: chiusa la fase di iscrizione, si svolgerà regolarmente."),
    CANCELLATA("Visita cancellata: non ha raggiunto il numero minimo di partecipanti entro la scadenza."),
    EFFETTUATA("Visita effettuata: la visita si è svolta e viene archiviata.");

    private final String descrizione;

    ActivityState(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
