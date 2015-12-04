package freelance.android.erick.geolocation.Manger;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;

import freelance.android.erick.geolocation.Model.MyMarker;
import freelance.android.erick.geolocation.R;

/**
 * Created by erick on 2.12.15.
 */
public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter
{
    HashMap<Marker, MyMarker> mMarkersHashMap;
    LayoutInflater layoutInflater;
    public MarkerInfoWindowAdapter(HashMap<Marker, MyMarker> mMarkersHashMap, LayoutInflater layoutInflater) {
        this.mMarkersHashMap = mMarkersHashMap;
        this.layoutInflater = layoutInflater;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View v  = this.layoutInflater.inflate(R.layout.infowindow_layout, null);
        ImageView markerIcon = (ImageView) v.findViewById(R.id.marker_icon);
        TextView markerLabel = (TextView) v.findViewById(R.id.marker_label);
        Button showButton = (Button) v.findViewById(R.id.showButton);

        if(null != mMarkersHashMap.get(marker)) {
            showButton.setText("Call");
            final MyMarker myMarker = mMarkersHashMap.get(marker);
            markerIcon.setImageResource(R.drawable.taxi);
            markerLabel.setText(myMarker.getmLabel());
        } else {
            showButton.setText("Call me");
            markerLabel.setText("Put me out!");
        }
        return v;
    }

//    private int manageMarkerIcon(String markerIcon) {
//        switch (markerIcon) {
//            case "icon1":
//                return R.drawable.taxi;
//            case "icon2":
//                return R.drawable.taxi;
//            case "icon3":
//                return R.drawable.taxi;
//            case "icon4":
//                return R.drawable.taxi;
//            case "icon5":
//                return R.drawable.taxi;
//            case "icon6":
//                return R.drawable.taxi;
//            case "icon7":
//                return R.drawable.taxi;
//            default:
//                return R.drawable.taxi;
//        }
//    }
}