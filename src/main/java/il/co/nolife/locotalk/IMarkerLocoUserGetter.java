package il.co.nolife.locotalk;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by Victor Belski on 9/12/2015.
 */
public interface IMarkerLocoUserGetter {
    LocoUser GetUser(Marker marker);
}
