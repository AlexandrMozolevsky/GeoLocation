package freelance.android.erick.geolocation.Model;

/**
 * Created by erick on 3.12.15.
 */
public class Polyline {
    private String points;

    public String getPoints ()
    {
        return points;
    }

    public void setPoints (String points)
    {
        this.points = points;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [points = "+points+"]";
    }
}