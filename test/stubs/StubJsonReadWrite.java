package stubs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;

import server.data.json.datalayer.datareadwrite.IJsonReadWrite;

public class StubJsonReadWrite implements IJsonReadWrite {
        public Map<String, List<JsonObject>> fileData = new HashMap<>();
        public Set<String> createdFiles = new HashSet<>();
        public String lastWritePath = null;
        public String lastWriteMember = null;
        public List<JsonObject> lastWriteList = null;

        @Override
        public  List<JsonObject> readFromFile(String path, String memberName) {
            return fileData.getOrDefault(path + ":" + memberName, new ArrayList<>());
        }

        @Override
        public Boolean writeToFile(String path, List<JsonObject> list, String memberName) {
            fileData.put(path + ":" + memberName, new ArrayList<>(list));
            lastWritePath = path;
            lastWriteMember = memberName;
            lastWriteList = new ArrayList<>(list);
            return true;
        }

        @Override
        public boolean createJSONEmptyFile(String path) {
            createdFiles.add(path);
            return true;
        }
    }