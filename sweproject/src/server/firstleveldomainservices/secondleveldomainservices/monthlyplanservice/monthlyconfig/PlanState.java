package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.monthlyconfig;

public enum PlanState {
    DISPONIBILITA_APERTE("disponibilita_aperte"),
    GENERAZIONE_PIANO("genrazione_piano"),
    MODIFICHE_APERTE("modifiche_aperte");

    private final String state;

    PlanState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    // Metodo statico per ottenere enum da stringa (opzionale)
    public static PlanState fromString(String text) {
        for (PlanState s : PlanState.values()) {
            if (s.state.equalsIgnoreCase(text)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Stato non valido: " + text);
    }
}
