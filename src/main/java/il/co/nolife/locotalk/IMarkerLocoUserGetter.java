package il.co.nolife.locotalk;

import com.google.android.gms.maps.model.Marker;

import il.co.nolife.locotalk.DataTypes.LocoUser;

/**
 * Created by Victor Belski on 9/12/2015.
 */
public interface IMarkerLocoUserGetter {
    LocoUser GetUser(Marker marker);
}
