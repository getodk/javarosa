package org.javarosa.xpath.expr;

import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.GeoShapeData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.util.GeoUtils;
import org.javarosa.xpath.XPathNodeset;
import org.javarosa.xpath.XPathTypeMismatchException;
import org.javarosa.xpath.XPathUnhandledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/** XPath function expression geographic logic */
class XPathFuncExprGeo {
    private static final Logger logger = LoggerFactory.getLogger(XPathFuncExprGeo.class.getSimpleName());

    /** How XPathPathExpr.getRefValue returns unparsable, therefore null, geo data */
    private static final String UNPARSABLE_GEO_DATA = "";

    List<GeoUtils.LatLong> getGpsCoordinatesFromNodeset(String name, Object argVal) {
        if (!(argVal instanceof XPathNodeset)) {
            throw new XPathUnhandledException("function \'" + name + "\' requires a field as the parameter.");
        }
        Object[] argList = ((XPathNodeset) argVal).toArgList();
        int repeatSize = argList.length;

        final List<GeoUtils.LatLong> latLongs = new ArrayList<>();

        if (repeatSize == 1) {
            Object arg = argList[0];
            if (arg.equals(UNPARSABLE_GEO_DATA)) throwMismatch(name);

            // Try to determine if the argument is of type GeoShapeData
            try {
                GeoShapeData geoShapeData = new GeoShapeData().cast(new UncastData(XPathFuncExpr.toString(arg)));
                for (GeoPointData point : geoShapeData.points) {
                    latLongs.add(new GeoUtils.LatLong(point.getPart(0), point.getPart(1)));
                }
            } catch (Exception e) {
                throwMismatch(name);
            }
        } else if (repeatSize >= 2) {
            // treat the input as a series of GeoPointData

            for (Object arg : argList) {
                if (arg.equals(UNPARSABLE_GEO_DATA)) throwMismatch(name);
                try {
                    GeoPointData geoPointData = new GeoPointData().cast(new UncastData(XPathFuncExpr.toString(arg)));
                    latLongs.add(new GeoUtils.LatLong(geoPointData.getPart(0), geoPointData.getPart(1)));
                } catch (Exception e) {
                    throwMismatch(name);
                }
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
