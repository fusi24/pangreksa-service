package com.pangreksa.service.shared;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

public class GeometryUtil {
    private static final GeoJsonWriter writer = new GeoJsonWriter();
    private static final GeoJsonReader reader = new GeoJsonReader();

    public static String geometryToGeoJson(Geometry geom) {
        return geom == null ? null : writer.write(geom);
    }

    public static Geometry geoJsonToGeometry(String geoJson) throws ParseException {
        return (geoJson == null || geoJson.trim().isEmpty()) ? null : reader.read(geoJson);
    }
}
