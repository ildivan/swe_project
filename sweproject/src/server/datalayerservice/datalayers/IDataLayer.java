package server.datalayerservice.datalayers;

import java.util.List;

import com.google.gson.JsonObject;

import server.datalayerservice.datalocalizationinformations.IDataLocalizationInformation;

public interface IDataLayer<T extends IDataLocalizationInformation<T>> {

/**
 * Adds a new JSON object to the data layer with the specified localization information.
 *
 * @param jsonObject The JSON object to be added.
 * @param localizationInformation The localization information associated with the JSON object.
 */
void add(JsonObject jsonObject, T localizationInformation);

/**
 * Modifies an existing JSON object in the data layer with the specified localization information.
 *
 * @param jsonObject The JSON object to be modified.
 * @param localizationInformation The localization information associated with the JSON object.
 * @return true if the modification was successful, false otherwise.
 */
boolean modify(JsonObject jsonObject, T localizationInformation);

/**
 * Deletes a JSON object from the data layer with the specified localization information.
 *
 * @param jsonObject The JSON object to be deleted.
 * @param localizationInformation The localization information associated with the JSON object.
 */
void delete(T localizationInformation);

/**
 * Retrieves a JSON object from the data layer with the specified localization information.
 *
 * @param localizationInformation The localization information associated with the JSON object.
 * @return The retrieved JSON object.
 */
JsonObject get(T localizationInformation);

/**
 * Checks if a JSON object exists in the data layer with the specified localization information.
 *
 * @param localizationInformation The localization information associated with the JSON object.
 * @return true if the JSON object exists, false otherwise.
 */
boolean exists(T localizationInformation);

/**
 * Checks if a file associated with the JSON object and localization information exists.
 *
 * @param localizationInformation The localization information associated with the JSON object.
 * @return true if the file exists, false otherwise.
 */
boolean checkFileExistance(T localizationInformation);

/**
 * Creates an empty JSON file in the data layer with the specified localization information.
 *
 * @param localizationInformation The localization information associated with the JSON object.
 */
void createJSONEmptyFile(T localizationInformation);

/**
 * Retrieves a list of JSON objects from the data layer with the specified localization information.
 *
 * @param localizationInformation The localization information associated with the JSON objects.
 * @return A list of JSON objects matching the filter criteria.
 */
List<JsonObject> getList(T localizationInformation);

/**
 * Retrieves all JSON objects from the data layer with the specified localization information.
 *
 * @param localizationInformation The localization information associated with the JSON objects.
 * @return A list of all JSON objects in the data layer.
 */
List<JsonObject> getAll(T localizationInformation);


}

