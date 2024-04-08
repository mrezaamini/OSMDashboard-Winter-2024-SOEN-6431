package de.storchp.opentracks.osmplugin.dashboardapi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SkiElements {
    public String type;
    public long id;
    public Bounds bounds;
    public ArrayList<Object> nodes;
    public ArrayList<Geometry> geometry;
    public Tags tags;

    public SkiElements(String type, long id, Bounds bounds, ArrayList<Object> nodes,
                       ArrayList<Geometry> geometry, Tags tags) {
        this.type = type;
        this.id = id;
        this.bounds = bounds;
        this.nodes = nodes;
        this.geometry = geometry;
        this.tags = tags;
    }

    public SkiElements() {

    }

    public static SkiElements parseJsonElement(JSONObject jsonObject) throws JSONException {
        SkiElements skiElements = new SkiElements();
        skiElements.type = jsonObject.optString("type", "");
        skiElements.id = jsonObject.optInt("id", 0);

        // Parsing bounds
        JSONObject boundsObj = jsonObject.optJSONObject("bounds");
        if (boundsObj != null) {
            skiElements.bounds = new Bounds();
            skiElements.bounds.minlat = boundsObj.optDouble("minlat", 0.0);
            skiElements.bounds.minlon = boundsObj.optDouble("minlon", 0.0);
            skiElements.bounds.maxlat = boundsObj.optDouble("maxlat", 0.0);
            skiElements.bounds.maxlon = boundsObj.optDouble("maxlon", 0.0);
        }

        // Parsing nodes
        JSONArray nodesArray = jsonObject.optJSONArray("nodes");
        if (nodesArray != null) {
            skiElements.nodes = new ArrayList<>();
            for (int i = 0; i < nodesArray.length(); i++) {
                skiElements.nodes.add(nodesArray.optLong(i, 0L));
            }
        }

        // Parsing geometry
        JSONArray geometryArray = jsonObject.optJSONArray("geometry");
        if (geometryArray != null) {
            for (int i = 0; i < geometryArray.length(); i++) {
                JSONObject geometryObj = geometryArray.optJSONObject(i);
                skiElements.geometry = new ArrayList<>();
                if (geometryObj != null) {
                    Geometry geometry = new Geometry();
                    geometry.lat = geometryObj.optDouble("lat", 0.0);
                    geometry.lon = geometryObj.optDouble("lon", 0.0);
                    skiElements.geometry.add(geometry);
                }
            }
        }

        // Parsing tags
        JSONObject tagsObj = jsonObject.optJSONObject("tags");
        if (tagsObj != null) {
            Tags tags = new Tags();
            tags.name = tagsObj.optString("name", "");
            tags.piste_difficulty = tagsObj.optString("piste:difficulty", "");
            tags.piste_type = tagsObj.optString("piste:type", "");
            tags.ref = tagsObj.optString("ref", "");
            skiElements.tags = tags;
        }

        // Printing parsed element
//        System.out.println("Type: " + skiElements.type);
//        System.out.println("ID: " + skiElements.id);
//        System.out.println("Tags: " + skiElements.tags.name);

        return skiElements;
    }

}

