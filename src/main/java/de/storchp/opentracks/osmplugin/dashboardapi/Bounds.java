package de.storchp.opentracks.osmplugin.dashboardapi;

public class Bounds {
    public double minlat;
    public double minlon;
    public double maxlat;
    public double maxlon;

    // Constructor
    public Bounds(double minlat, double minlon, double maxlat, double maxlon) {
        this.minlat = minlat;
        this.minlon = minlon;
        this.maxlat = maxlat;
        this.maxlon = maxlon;
    }

    public Bounds() {

    }
}
