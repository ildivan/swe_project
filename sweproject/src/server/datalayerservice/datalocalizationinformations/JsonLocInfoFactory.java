package server.datalayerservice.datalocalizationinformations;

public class JsonLocInfoFactory implements ILocInfoFactory{

    private static final String ACTIVITY_PATH = "JF/activities.json";
    private static final String ACTIVITY_MEMBER_NAME = "activities";
    private static final String ACTIVITY_KEY_DESC = "title";
    private static final String PLACES_PATH = "JF/places.json";
    private static final String PLACES_MEMBER_NAME = "places";
    private static final String PLACES_KEY_DESC = "name";
    private static final String GENERAL_CONFIGS_KEY_DESCRIPTION = "configType";
    private static final String GENERAL_CONFIGS_MEMBER_NAME = "configs";
    private static final String GENERAL_CONFIG_PATH = "JF/configs.json";
    private static final String VOLUNTEER_PATH = "JF/volunteers.json";
    private static final String VOLUNTEER_MEMBER_NAME = "volunteers";
    private static final String VOLUNTEER_KEY_DESC = "name";
    private static final String MONTHLY_CONFIG_KEY_DESC = "type";
    private static final String MONTHLY_CONFIG_MEMEBER_NAME = "mc";
    private static final String MONTHLY_CONFIG_PATH = "JF/monthlyConfigs.json";
    private static final String MONTHLY_PLAN_PATH = "JF/monthlyPlan.json";
    private static final String MONTHLY_PLAN_MEMBER_NAME = "monthlyPlan";
    private static final String MONTHLY_PLAN_KEY_DESC = "date";
    private static final String USERS_KEY_DESCRIPTION = "name";
    private static final String USERS_MEMBER_NAME = "users";
    private static final String USERS_PATH = "JF/users.json";
    private static final String ARCHIVE_PATH = "JF/archive.json";
    private static final String ARCHIVE_MEMBER_NAME = "activityArchive";

    /**
     * metodo che ritorna le informazioni di localizzazione per dati salvati in
     * JSON files per le visite
     */
    @Override
    public JsonDataLocalizationInformation getActivityLocInfo() {
        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(ACTIVITY_PATH);
        locInfo.setMemberName(ACTIVITY_MEMBER_NAME);
        locInfo.setKeyDesc(ACTIVITY_KEY_DESC);
        return locInfo;
    }

    /**
     * metodo che ritorna le informazioni di localizzazione per dati salvati in
     * JSON files per i luoghi
     */
    @Override
    public JsonDataLocalizationInformation getPlaceLocInfo() {
        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(PLACES_PATH);
        locInfo.setMemberName(PLACES_MEMBER_NAME);
        locInfo.setKeyDesc(PLACES_KEY_DESC);
        return locInfo;
    }

    /**
     * metodo che ritorna le informazioni di localizzazione per dati salvati in
     * JSON files per gli utenti e le loro informazioni di autenticazione
     */
    @Override
    public JsonDataLocalizationInformation getUserLocInfo() {
        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(USERS_PATH);
        locInfo.setMemberName(USERS_MEMBER_NAME);
        locInfo.setKeyDesc(USERS_KEY_DESCRIPTION);
        return locInfo;
    }

    /**
     * metodo che ritorna le informazioni di localizzazione per dati salvati in
     * JSON files per i volontari
     */
    @Override
    public JsonDataLocalizationInformation getVolunteerLocInfo() {
        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(VOLUNTEER_PATH);
        locInfo.setMemberName(VOLUNTEER_MEMBER_NAME);
        locInfo.setKeyDesc(VOLUNTEER_KEY_DESC);
        return locInfo;
    }

    /**
     * metodo che ritorna le informazioni di localizzazione per dati salvati in
     * JSON files per i config gerali
     */
    @Override
    public JsonDataLocalizationInformation getConfigLocInfo() {
        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(GENERAL_CONFIG_PATH);
        locInfo.setMemberName(GENERAL_CONFIGS_MEMBER_NAME);
        locInfo.setKeyDesc(GENERAL_CONFIGS_KEY_DESCRIPTION);
        return locInfo;
    }

    /**
     * metodo che ritorna le informazioni di localizzazione per dati salvati in
     * JSON files per i config del piano mensile
     */
    @Override
    public JsonDataLocalizationInformation getMonthlyConfigLocInfo() {
       JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(MONTHLY_CONFIG_PATH);
        locInfo.setMemberName(MONTHLY_CONFIG_MEMEBER_NAME);
        locInfo.setKeyDesc(MONTHLY_CONFIG_KEY_DESC);
        return locInfo;
    }

    /**
     * metodo che ritorna le informazioni di localizzazione per dati salvati in
     * JSON files per il piano del mese
     */
    @Override
    public JsonDataLocalizationInformation getMonthlyPlanLocInfo() {
       JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(MONTHLY_PLAN_PATH);
        locInfo.setMemberName(MONTHLY_PLAN_MEMBER_NAME);
        locInfo.setKeyDesc(MONTHLY_PLAN_KEY_DESC);
        return locInfo;
    }

    /**
     * metodo che ritorna le informazioni di localizzazione per dati salvati in
     * JSON files per l'archivio
     */
    @Override
    public JsonDataLocalizationInformation getArchiveLocInfo() {
        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(ARCHIVE_PATH);
        locInfo.setMemberName(ARCHIVE_MEMBER_NAME);
        return locInfo;
    }
    
}
