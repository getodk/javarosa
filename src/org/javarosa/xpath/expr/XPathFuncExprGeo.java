package org.javarosa.xpath.expr;

import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.GeoShapeData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.util.GeoUtils;
import org.javarosa.xpath.XPathNodeset;
import org.javarosa.xpath.XPathTypeMismatchException;
import org.javarosa.xpath.XPathUnhandledException;

import java.util.ArrayList;
import java.util.List;

/** XPath function expression geographic logic */
class XPathFuncExprGeo {
    List<GeoUtils.LatLong> getGpsCoordinatesFromNodeset(String name, Object argVal) {
        if (!(argVal instanceof XPathNodeset)) {
            throw new XPathUnhandledException("function \'" + name + "\' requires a field as the parameter.");
        }
        Object[] argList = ((XPathNodeset) argVal).toArgList();
        int repeatSize = argList.length;

        final List<GeoUtils.LatLong> latLongs = new ArrayList<>();

        if (repeatSize == 1) {
            // Try to determine if the argument is of type GeoShapeData
            try {
                GeoShapeData geoShapeData = new GeoShapeData().cast(new UncastData(XPathFuncExpr.toString(argList[0])));
                if (geoShapeData.points.size() > 2) {
                    for (GeoPointData point : geoShapeData.points) {
                        latLongs.add(new GeoUtils.LatLong(point.getPart(0), point.getPart(1)));
                    }
                }
            } catch (Exception e) {
                throw new XPathTypeMismatchException("The function \'" + name + "\' received a value that does not represent GPS coordinates: " + argList[0]);
            }
        } else if (repeatSize > 2) {
            // treat the input as a series of GeoPointData

            for (Object arg : argList) {
                try {
                    GeoPointData geoPointData = new GeoPointData().cast(new UncastData(XPathFuncExpr.toString(arg)));
                    latLongs.add(new GeoUtils.LatLong(geoPointData.getPart(0), geoPointData.getPart(1)));
                } catch (Exception e) {
                    throw new XPathTypeMismatchException("The function \'" + name + "\' received a value that does not represent GPS coordinates: " + arg);
                }
            }
        }
        return latLongs;
    }
}
