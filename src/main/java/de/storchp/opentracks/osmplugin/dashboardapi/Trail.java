package de.storchp.opentracks.osmplugin.dashboardapi;

import java.util.ArrayList;
import java.util.List;


public class Trail {

    private List<SkiElements> trails = new ArrayList<>();

    private static Trail instance = null;

    private Trail() {
    }

    // Static method to create instance of Singleton class
    public static synchronized Trail getInstance() {
        if (instance == null)
            instance = new Trail();

        return instance;
    }


    public List<SkiElements> getTrails() {
        return trails;
    }

    public void addTrailData(SkiElements trail) {
        trails.add(trail);
    }

    public void clearData() {
        trails.clear();
    }

}

class Tags {
    public String name;
    public String piste_difficulty;
    public String piste_grooming;
    public String piste_type;
    public String ref;
    public String piste_ref;
    public String foot;
    public String route;
    public String gladed;

    public Tags(String name, String piste_difficulty, String piste_grooming,
                String piste_type, String ref, String piste_ref,
                String foot, String route, String gladed) {
        this.name = name;
        this.piste_difficulty = piste_difficulty;
        this.piste_grooming = piste_grooming;
        this.piste_type = piste_type;
        this.ref = ref;
        this.piste_ref = piste_ref;
        this.foot = foot;
        this.route = route;
        this.gladed = gladed;
    }

    public Tags(){

    }
}

