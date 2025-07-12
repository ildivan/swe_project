package server.data.facade.implementation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonObject;

import server.data.facade.interfaces.IActivitiesFacade;
import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.JsonDataLocalizationInformation;
import server.data.json.datalayer.datareadwrite.IJsonReadWrite;
import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.Address;
import server.firstleveldomainservices.Place;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityInfo;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityRecord;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityState;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.DailyPlan;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlan;
import server.jsonfactoryservice.JsonFactoryService;

public class JsonActivitiesFacade implements IActivitiesFacade {
    
    private JsonDataLayer dataLayer;
    private final IJsonLocInfoFactory locInfoFactory;
    private final JsonFactoryService jsonFactoryService = new JsonFactoryService();

    public JsonActivitiesFacade(IJsonReadWrite readWrite, 
                            IJsonLocInfoFactory locInfoFactory) {
        this.dataLayer = new JsonDataLayer(readWrite);
        this.locInfoFactory = locInfoFactory;
    }

    @Override
    public List<Activity> getActivities() {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getActivityLocInfo();

        List<JsonObject> activitiesJO = dataLayer.getAll(locInfo);
        List<Activity> activities = jsonFactoryService.createObjectList(activitiesJO, Activity.class);

        return activities;
    }

    @Override
    public List<Activity> getChangedActivities() {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedActivitiesLocInfo();

        List<JsonObject> activitiesJO = dataLayer.getAll(locInfo);
        List<Activity> activities = jsonFactoryService.createObjectList(activitiesJO, Activity.class);

        return activities;
    }

    @Override
    public Activity getActivity(String activityName) {
        assert activityName != null && !activityName.trim().isEmpty() : "Il nome dell'attività non può essere vuoto";

        JsonDataLocalizationInformation locInfo = locInfoFactory.getActivityLocInfo();
        locInfo.setKey(activityName);
        assert dataLayer.exists(locInfo) : "L'attività non esiste";
        return jsonFactoryService.createObject(dataLayer.get(locInfo), Activity.class);
    }

    @Override
    public Activity getChangedActivity(String activityName) {
        assert activityName != null && !activityName.trim().isEmpty() : "Il nome dell'attività non può essere vuoto";

        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedActivitiesLocInfo();
        locInfo.setKey(activityName);
        assert dataLayer.exists(locInfo) : "L'attività non esiste";
        return jsonFactoryService.createObject(dataLayer.get(locInfo), Activity.class);
    }

    @Override
    public Activity addActivity(
            Place place,
            String name,
            String description,
            Address meetingPoint,
            LocalDate firstProgrammableDate,
            LocalDate lastProgrammableDate,
            String[] programmableDays,
            LocalTime programmableHour,
            LocalTime duration,
            boolean bigliettoNecessario,
            int maxPartecipanti,
            int minPartecipanti,
            String[] volunteers
    ){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedActivitiesLocInfo();
        Activity activity = new Activity(
            place.getName(),
            name,
            description,
            meetingPoint,
            firstProgrammableDate,
            lastProgrammableDate,
            programmableDays,
            programmableHour,
            duration,
            bigliettoNecessario,
            maxPartecipanti,
            minPartecipanti,
            volunteers
        );
        dataLayer.add(jsonFactoryService.createJson(activity), locInfo);
        return activity;
    }

    @Override
    public List<ActivityRecord> getActivitiesByState(ActivityState desiredState, MonthlyPlan monthlyPlan) {
        
        if(monthlyPlan == null){
            return null;
        }

        List<ActivityRecord> result = new ArrayList<>();

        for (Map.Entry<LocalDate, DailyPlan> dailyEntry : monthlyPlan.getMonthlyPlan().entrySet()) {
            LocalDate date = dailyEntry.getKey();
            DailyPlan dailyPlan = dailyEntry.getValue();
    
            for (Map.Entry<String, ActivityInfo> activityEntry : dailyPlan.getPlan().entrySet()) {
                String activityName = activityEntry.getKey();
                ActivityInfo activityInfo = activityEntry.getValue();
    
                if (activityInfo.getState() == desiredState) {
                    result.add(new ActivityRecord(date, activityName, activityInfo));
                }
            }
        }
        return result;
    }

    @Override
    public boolean modifyActivity(
            String activityName,
            Optional<String> title,
            Optional<String> description,
            Optional<Address> newMeetingPoint,
            Optional<LocalDate> firstDate,
            Optional<LocalDate> lastDate,
            Optional<String[]> newDays,
            Optional<LocalTime> newHour,
            Optional<LocalTime> newDuration,
            Optional<Boolean> newTicket,
            Optional<Integer> newMax,
            Optional<Integer> newMin,
            Optional<String[]> volunteers
        ){
        
        if (!doesActivityExist(activityName))
            return false;
            
        Activity activity = getChangedActivity(activityName);

        if (title.isPresent() && !title.get().isBlank()) 
            activity.setTitle(title.get());

        if (description.isPresent() &&!description.get().isBlank()) 
            activity.setDescription(description.get());

        if( newMeetingPoint.isPresent() ) activity.setMeetingPoint(newMeetingPoint.get());

        if (firstDate.isPresent()) activity.setFirstProgrammableDate(firstDate.get());

        if (lastDate.isPresent()) activity.setLastProgrammableDate(lastDate.get());

        if (newDays.isPresent()) activity.setProgrammableDays(newDays.get());

        if (newHour.isPresent()) activity.setProgrammableHour(newHour.get());

        if (newDuration.isPresent()) activity.setDurationAsLocalTime(newDuration.get());

        if (newTicket.isPresent() )activity.setBigliettoNecessario(newTicket.get());
        
        if(newMax.isPresent()) activity.setMaxPartecipanti(newMax.get());
        if(newMin.isPresent()) activity.setMinPartecipanti(newMin.get());

        if (volunteers.isPresent()) {
            activity.setVolunteers(volunteers.get());
        }

        return saveActivity(activity, activityName);
    }


    private boolean saveActivity(Activity activity, String oldTitle) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedActivitiesLocInfo();
        locInfo.setKey(oldTitle);
        
        JsonObject activityJO = jsonFactoryService.createJson(activity);
        boolean modified = dataLayer.modify(activityJO, locInfo);
        
        return modified;
    }

    @Override
    public boolean deleteActivity(String activityName) {
        assert activityName != null && !activityName.trim().isEmpty() : "Il nome dell'attività non può essere vuoto";

        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedActivitiesLocInfo();
        locInfo.setKey(activityName);
        if (!dataLayer.exists(locInfo)) {
            return false; // L'attività non esiste
        }
        dataLayer.delete(locInfo);
        return !dataLayer.exists(locInfo);
    }

    @Override
    public boolean doesActivityExist(String activityName) {
        assert activityName != null && !activityName.trim().isEmpty() : "Il nome dell'attività non può essere vuoto";

        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedActivitiesLocInfo();
        locInfo.setKey(activityName);
        return dataLayer.exists(locInfo);
    }

}
