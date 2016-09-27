package ro.cluj.totemz.utils;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.Collection;

import rx.functions.Func1;

/**
 * Util class for geometry operations
 */
public class GeometryUtils {
    private GeometryUtils() {
    }

    /**
     * Returns bound box for all given coordinates
     */
    public static <T> LatLngBounds bounds(Collection<T> list, Func1<T, LatLng> mapper) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        double minLng = Double.MAX_VALUE;
        double maxLng = -Double.MAX_VALUE;
        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;

        LatLng coordinates;
        for (T item : list) {
            coordinates = mapper.call(item);
            minLng = Math.min(minLng, coordinates.longitude);
            maxLng = Math.max(maxLng, coordinates.longitude);
            minLat = Math.min(minLat, coordinates.latitude);
            maxLat = Math.max(maxLat, coordinates.latitude);
        }

        return new LatLngBounds(new LatLng(minLat, minLng), new LatLng(maxLat, maxLng));
    }

    /**
     * Returns bound box for all given coordinates
     */
    public static LatLngBounds bounds(Collection<LatLng> coordinates) {
        return bounds(coordinates, new Func1<LatLng, LatLng>() {
            @Override
            public LatLng call(LatLng latLng) {
                return latLng;
            }
        });
    }
}
