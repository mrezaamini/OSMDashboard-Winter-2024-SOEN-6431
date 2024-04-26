package de.storchp.opentracks.osmplugin.dashboardapi;

import static de.storchp.opentracks.osmplugin.dashboardapi.APIConstants.LAT_LON_FACTOR;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import android.util.Pair;

import org.oscim.core.GeoPoint;

import java.text.SimpleDateFormat;


import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.storchp.opentracks.osmplugin.utils.MapUtils;
import de.storchp.opentracks.osmplugin.utils.TrackPointsDebug;

public class TrackPoint {

    public static final String _ID = "_id";
    public static final String TRACKID = "trackid";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String TIME = "time";
    public static final String TYPE = "type";
    public static final String SPEED = "speed";

    public static final String ELEVATION = "elevation";
    public static final double PAUSE_LATITUDE = 100.0;

    public static final List<Pair<Double, String>> speedTimeEntries = new ArrayList<>();
    public static final List<Pair<Double, Double>> speedElevationEntries = new ArrayList<>();

    protected static final String[] PROJECTION_V1 = {
            _ID,
            TRACKID,
            LATITUDE,
            LONGITUDE,
            TIME,
            SPEED,
            ELEVATION
    };

    protected static final String[] PROJECTION_V2 = {
            _ID,
            TRACKID,
            LATITUDE,
            LONGITUDE,
            TIME,
            TYPE,
            SPEED,
            ELEVATION
    };

    private final long trackPointId;

    private final long trackRecordId; // Renamed to avoid confusion with TRACKID

    private final GeoPoint latLong;
    private final boolean pause;
    private final double speed;
    private final Integer type;
    private final Date time;

    private final double elevation;

    public TrackPoint(long trackRecordId, long trackPointId, double latitude, double longitude, Integer type, double speed, double elevation, Date time) {

        this.trackRecordId = trackRecordId;
        this.trackPointId = trackPointId;
        if (MapUtils.isValid(latitude, longitude)) {
            this.latLong = new GeoPoint(latitude, longitude);
        } else {
            latLong = null;
        }
        this.pause = type != null ? type == 3 : latitude == PAUSE_LATITUDE;
        this.speed = speed;

        this.elevation = elevation;

        this.time = time;
        this.type = type;
    }

    public boolean hasValidLocation() {
        return latLong != null;
    }

    public boolean isPause() {
        return pause;
    }

    @Override
    public String toString() {
        return "TrackPoint{" +
                "trackPointId=" + trackPointId +
                ", trackId=" + trackRecordId +
                ", latLong=" + latLong +
                ", pause=" + pause +
                ", speed=" + speed +
                ",time=" + time+
                ",elevation=" + elevation+
                '}';
    }
    /**
     * Reads the TrackPoints from the Content Uri and split by segments.
     * Pause TrackPoints and different Track IDs split the segments.
     */
    public static TrackPointsBySegments readTrackPointsBySegments(ContentResolver resolver, Uri data, long lastTrackPointId, int protocolVersion) {
        var debug = new TrackPointsDebug();
        var segments = new ArrayList<List<TrackPoint>>();
        var projection = PROJECTION_V2;
        var typeQuery = " AND " + TrackPoint.TYPE + " IN (-2, -1, 0, 1, 3)";
        if (protocolVersion < 2) { // fallback to old Dashboard API
            projection = PROJECTION_V1;
            typeQuery = "";
        }
        try (Cursor cursor = resolver.query(data, projection, TrackPoint._ID + "> ?" + typeQuery, new String[]{Long.toString(lastTrackPointId)}, null)) {
            TrackPoint lastTrackPoint = null;
            List<TrackPoint> segment = null;
            while (cursor.moveToNext()) {
                //debug.trackpointsReceived++;
                var trackPointId = cursor.getLong(cursor.getColumnIndexOrThrow(TrackPoint._ID));
                var trackRecordId = cursor.getLong(cursor.getColumnIndexOrThrow(TrackPoint.TRACKID));
                var latitude = cursor.getInt(cursor.getColumnIndexOrThrow(TrackPoint.LATITUDE)) / LAT_LON_FACTOR;
                var longitude = cursor.getInt(cursor.getColumnIndexOrThrow(TrackPoint.LONGITUDE)) / LAT_LON_FACTOR;
                var typeIndex = cursor.getColumnIndex(TrackPoint.TYPE);
                var speed = cursor.getDouble(cursor.getColumnIndexOrThrow(TrackPoint.SPEED));
                var timeValue = cursor.getLong(cursor.getColumnIndexOrThrow(TrackPoint.TIME));

                var elevation = cursor.getDouble(cursor.getColumnIndexOrThrow(TrackPoint.ELEVATION));


                Date time = new Date(timeValue);

                TrackPoint.addSpeedTimeEntry(speed, String.valueOf(time));
                TrackPoint.addSpeedElevationEntry(speed,elevation);


                Integer type = null;
                if (typeIndex > -1) {
                    type = cursor.getInt(typeIndex);

                if (lastTrackPoint == null || lastTrackPoint.trackRecordId != trackRecordId) {
                    segment = new ArrayList<>();
                    segments.add(segment);
                }


                lastTrackPoint = new TrackPoint(trackRecordId, trackPointId, latitude, longitude, type, speed,elevation,time);

                if (lastTrackPoint.hasValidLocation()) {
                    segment.add(lastTrackPoint);
                } else if (!lastTrackPoint.isPause()) {
                    //debug.trackpointsInvalid++;
                }
                if (lastTrackPoint.isPause()) {
                    debug.setTrackpointsPause(debug.getTrackpointsPause() + 1);
                    if (!lastTrackPoint.hasValidLocation()) {
                        if (segment.size() > 0) {
                            var previousTrackpoint = segment.get(segment.size() - 1);
                            if (previousTrackpoint.hasValidLocation()) {

                                segment.add(new TrackPoint(trackRecordId, trackPointId, previousTrackpoint.getLatLong().getLatitude(), previousTrackpoint.getLatLong().getLongitude(), type, speed,elevation,time));

                            }
                        }
                        lastTrackPoint = null;
                    }
                    lastTrackPoint = null;
                }
            }
        }
        //debug.segments = segments.size();

        return new TrackPointsBySegments(segments, debug);
    }
    }

    public long getTrackPointId() {
        return trackPointId;
    }

    public long getTrackId() {
        return trackRecordId;
    }

    public GeoPoint getLatLong() {
        return latLong;
    }

    public double getSpeed() {
        return speed;
    }

    public Date getTime(){
        return time;
    }
    public Integer getType() {
        return type;
    }



    public static void addSpeedTimeEntry(double speed, String time) {
        speedTimeEntries.add(new Pair<>(speed, time));
        //Log.d("TrackPointData", "Entry added - Speed: " + speed + ", Time: " + time);
    }
    // Method to clear the list (optional, but useful for managing memory)
    public static void clearSpeedTimeEntries() {
        speedTimeEntries.clear();
    }
//    }

    public static void addSpeedElevationEntry(double speed, double elevation) {
        speedElevationEntries.add(new Pair<>(speed, elevation));
    }

    // Method to clear the list for managing memory
    public static void clearSpeedElevationEntries() {
        speedElevationEntries.clear();
    }

}
