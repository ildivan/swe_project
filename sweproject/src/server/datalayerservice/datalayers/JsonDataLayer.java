package server.datalayerservice.datalayers;

import com.google.gson.JsonObject;

import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.datalayerservice.datareadwrite.IJsonReadWrite;
import server.datalayerservice.datareadwrite.JsonReadWrite;

import java.io.File;
import java.util.*;
public class JsonDataLayer implements IDataLayer<JsonDataLocalizationInformation> {

    private static final IJsonReadWrite jsonReadWrite = new JsonReadWrite();

    public JsonDataLayer() {
        super();
    }


    @Override
    public void add(JsonObject jsonObject, JsonDataLocalizationInformation info) {
        assert info != null;
        assert jsonObject != null;

        if (!checkFileExistance(info)) {
            jsonReadWrite.createJSONEmptyFile(info.getPath());
        }

        List<JsonObject> list = jsonReadWrite.readFromFile(info.getPath(), info.getMemberName());
        if (list == null) list = new ArrayList<>();

        list.add(jsonObject);
        jsonReadWrite.writeToFile(info.getPath(), list, info.getMemberName());
    }

    @Override
    public boolean modify(JsonObject jsonObject, JsonDataLocalizationInformation info) {
        assert info != null;
        assert jsonObject != null;

        if (!checkFileExistance(info)) {
            jsonReadWrite.createJSONEmptyFile(info.getPath());
        }

        List<JsonObject> list = jsonReadWrite.readFromFile(info.getPath(), info.getMemberName());
        for (int i = 0; i < list.size(); i++) {
            JsonObject current = list.get(i);
            if (current.get(info.getKeyDesc()).getAsString().equals(info.getKey())) {
                list.set(i, jsonObject);
                jsonReadWrite.writeToFile(info.getPath(), list, info.getMemberName());
                return true;
            }
        }
        return false;
    }

    @Override
    public void delete(JsonObject jsonObject, JsonDataLocalizationInformation info) {
        assert info != null;
        assert jsonObject != null;

        if (!checkFileExistance(info)) {
            jsonReadWrite.createJSONEmptyFile(info.getPath());
        }

        List<JsonObject> list = jsonReadWrite.readFromFile(info.getPath(), info.getMemberName());
        list.removeIf(o -> o.get(info.getKeyDesc()).getAsString().equals(info.getKey()));
        jsonReadWrite.writeToFile(info.getPath(), list, info.getMemberName());
    }

    @Override
    public JsonObject get(JsonDataLocalizationInformation info) {
        assert info != null;

        if (!checkFileExistance(info)) {
            jsonReadWrite.createJSONEmptyFile(info.getPath());
        }

        List<JsonObject> list = jsonReadWrite.readFromFile(info.getPath(), info.getMemberName());
        for (JsonObject o : list) {
            if (o.has(info.getKeyDesc()) && o.get(info.getKeyDesc()).getAsString().equals(info.getKey())) {
                return o;
            }
        }
        return null;
    }

    @Override
    public boolean exists(JsonDataLocalizationInformation info) {
        assert info != null;
        return get(info) != null;
    }

    @Override
    public boolean checkFileExistance(JsonDataLocalizationInformation info) {
        assert info != null;
        File file = new File(info.getPath());
        return file.exists();
    }

    @Override
    public void createJSONEmptyFile(JsonDataLocalizationInformation info) {
        assert info != null;
        jsonReadWrite.createJSONEmptyFile(info.getPath());
    }

    @Override
    public List<JsonObject> getList(JsonDataLocalizationInformation info) {
        assert info != null;

        if (!checkFileExistance(info)) {
            jsonReadWrite.createJSONEmptyFile(info.getPath());
        }

        List<JsonObject> list = jsonReadWrite.readFromFile(info.getPath(), info.getMemberName());
        if (list == null) return null;

        List<JsonObject> result = (
            list.stream().filter((o) -> o.get(info.getKeyDesc()).getAsString().equals(info.getKey())).toList()
        );

        return result.isEmpty() ? null : result;
    }

    @Override
    public List<JsonObject> getAll(JsonDataLocalizationInformation info) {
        assert info != null;

        if (!checkFileExistance(info)) {
            jsonReadWrite.createJSONEmptyFile(info.getPath());
        }
        List<JsonObject> result = jsonReadWrite.readFromFile(info.getPath(), info.getMemberName());
        assert result != null;
        return result;
    }
}
