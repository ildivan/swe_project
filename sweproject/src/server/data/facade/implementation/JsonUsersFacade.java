package server.data.facade.implementation;

import java.util.List;
import java.util.Optional;

import com.google.gson.JsonObject;

import server.data.User;
import server.data.facade.interfaces.IUsersFacade;
import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.JsonDataLocalizationInformation;
import server.data.json.datalayer.datareadwrite.JsonReadWrite;
import server.jsonfactoryservice.JsonFactoryService;

public class JsonUsersFacade implements IUsersFacade {

    private final JsonDataLayer datalayer;
    private final IJsonLocInfoFactory locInfoFactory;
    private final JsonFactoryService jsonFactoryService = new JsonFactoryService();

    public JsonUsersFacade(JsonReadWrite jsonReadWrite, IJsonLocInfoFactory locInfoFactory) {
        this.datalayer = new JsonDataLayer(jsonReadWrite);
        this.locInfoFactory = locInfoFactory;
    }
    
    @Override
    public void addUsers(List<User> users) {
        assert users != null  : "La lista degli utenti non può essere null";

        JsonDataLocalizationInformation locInfo = locInfoFactory.getUserLocInfo();
    
        for (User user : users) {
            datalayer.add(jsonFactoryService.createJson(user), locInfo);
        }
    }

    @Override
    public boolean addUser(User user) {
        assert user != null : "L'utente non può essere null";

        JsonDataLocalizationInformation locInfo = locInfoFactory.getUserLocInfo();
        locInfo.setKey(user.getName());

        if (datalayer.exists(locInfo)) {
            return false; // User already exists
        }

        datalayer.add(jsonFactoryService.createJson(user), locInfo);
        return datalayer.exists(locInfo);
    }

    @Override
    public List<User> getUsers() {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getUserLocInfo();
        List<JsonObject> userJOList = datalayer.getAll(locInfo);

        return jsonFactoryService.createObjectList(userJOList, User.class);
    }

    @Override
    public User getUser(String username) {
        assert username != null && !username.trim().isEmpty() : "Il nome utente non può essere vuoto";

        JsonDataLocalizationInformation locInfo = locInfoFactory.getUserLocInfo();
        locInfo.setKey(username);

        JsonObject userJO = datalayer.get(locInfo);
        if (userJO == null) {
            return null;
        }

        return jsonFactoryService.createObject(userJO, User.class);
    }

    @Override
    public boolean modifyUser(
        String username,
        Optional<String> newName,
        Optional<String> newPassword,
        Optional<String> newRole,
        Optional<Boolean> newActive,
        Optional<Boolean> newDeleted
    ) {
        assert username != null && !username.trim().isEmpty() : "Il nome utente non può essere vuoto";

        User user = getUser(username);
        
        if (user == null) {
            return false;
        }

        if (newName.isPresent()) {
            user.setName(newName.get());
        }
        if (newPassword.isPresent()) {
            user.setPassword(newPassword.get());
        }
        if (newRole.isPresent()) {
            user.setRole(newRole.get());
        }
        if (newActive.isPresent()) {
            user.setActive(newActive.get());
        }
        if (newDeleted.isPresent()) {
            user.setIsDeleted(newDeleted.get());
        }

        return saveUser(username, user);
    }

    private boolean saveUser(String username, User user) {
        assert username != null && !username.trim().isEmpty() : "Il nome utente non può essere vuoto";

        JsonDataLocalizationInformation locInfo = locInfoFactory.getUserLocInfo();
        locInfo.setKey(username);

        JsonObject userJO = jsonFactoryService.createJson(user);
        return datalayer.modify(userJO, locInfo);
    }

    @Override
    public boolean deleteUser(String username) {
        assert username != null && !username.trim().isEmpty() : "Il nome utente non può essere vuoto";

        JsonDataLocalizationInformation locInfo = locInfoFactory.getUserLocInfo();
        locInfo.setKey(username);

        datalayer.delete(locInfo);
        return !datalayer.exists(locInfo);
    }

    @Override
    public boolean doesUserExist(String username) {
        assert username != null && !username.trim().isEmpty() : "Il nome utente non può essere vuoto";

        JsonDataLocalizationInformation locInfo = locInfoFactory.getUserLocInfo();
        locInfo.setKey(username);

        return datalayer.exists(locInfo);
    }
}
