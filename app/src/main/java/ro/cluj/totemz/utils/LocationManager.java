package ro.cluj.totemz.utils;

import android.location.Location;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface LocationManager {
    /**
     * starts silent location updates
     */
    void startLocationUpdates();

    /**
     * stops silent location updates
     */
    void stopLocationUpdates();

    /**
     * @return a hot observable emitting location updates
     * @throws IllegalStateException when there's no valid provider available
     */
    Observable<Location> getLocationUpdates();

    /**
     * starts silent is location enabled updates
     */
    void startIsLocationEnabledUpdates();

    /**
     * stops silent is location enabled updates
     */
    void stopIsLocationEnabledUpdates();

    /**
     * @return an observable emitting location enabled state
     */
    Observable<Boolean> getIsLocationEnabled();

    /**
     * Fetches a single item location provider
     *
     * @return a single containing the name of the provider
     */
    Single<String> getLocationProvider();
}
