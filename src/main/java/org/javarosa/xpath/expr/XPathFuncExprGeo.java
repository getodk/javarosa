package org.javarosa.xpath.expr;

import java.util.ArrayList;
import java.util.List;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.GeoShapeData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.util.GeoUtils;
import org.javarosa.xpath.XPathNodeset;
import org.javarosa.xpath.XPathTypeMismatchException;
import org.javarosa.xpath.XPathUnhandledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** XPath function expression geographic logic */
class XPathFuncExprGeo {
    private static final Logger logger = LoggerFactory.getLogger(XPathFuncExprGeo.class.getSimpleName());

    List<GeoUtils.LatLong> getGpsCoordinatesFromNodeset(String name, Object argVal) {
        if (!(argVal instanceof XPathNodeset)) {
            throw new XPathUnhandledException("function '" + name + "' requires a field as the parameter.");
        }
        Object[] argList = ((XPathNodeset) argVal).toArgList();
        int repeatSize = argList.length;

        final List<GeoUtils.LatLong> latLongs = new ArrayList<>();

        if (repeatSize == 1) {
            // Try to determine if the argument is of type GeoShapeData
            try {
                GeoShapeData geoShapeData = new GeoShapeData().cast(new UncastData(XPathFuncExpr.toString(argList[0])));
                for (GeoPointData point : geoShapeData.points) {
                    latLongs.add(new GeoUtils.LatLong(point.getPart(0), point.getPart(1)));
                }
            } catch (Exception e) {
                throwMismatch(name);
            }
        } else if (repeatSize >= 2) {
            latLongs.addAll(geopointsToLatLongs(name, argList));
        }
        return latLongs;
    }

    public List<GeoUtils.LatLong> geopointsToLatLongs(String callingFunction, Object[] args) {
        List<GeoUtils.LatLong> latLongs = new ArrayList<>();
        for (Object arg : args) {
            try {
                GeoPointData geoPointData = new GeoPointData().cast(new UncastData(XPathFuncExpr.toString(arg)));
                latLongs.add(new GeoUtils.LatLong(geoPointData.getPart(0), geoPointData.getPart(1)));
            } catch (Exception e) {
                throwMismatch(callingFunction);
            }
        }

        return latLongs;
    }

    private void throwMismatch(String name) {
        String msg = "The function '" + name + "' received a value that does not represent GPS coordinates";
        logger.warn(msg);
        throw new XPathTypeMismatchException(msg);
    }
}
