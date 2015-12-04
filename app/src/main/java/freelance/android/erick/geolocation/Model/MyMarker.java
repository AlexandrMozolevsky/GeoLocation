package freelance.android.erick.geolocation.Model;

/**
 * Created by erick on 2.12.15.
 */
public class MyMarker
{
    private String mLabel;
    private int mIcon;
    private Double mLatitude;
    private Double mLongitude;

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getTaxiName() {
        return taxiName;
    }

    public void setTaxiName(String taxiName) {
        this.taxiName = taxiName;
    }

    public String getVoditelName() {
        return voditelName;
    }

    public void setVoditelName(String voditelName) {
        this.voditelName = voditelName;
    }

    private String cost;
    private String taxiName;
    private String voditelName;


    public MyMarker(String label, int icon, Double latitude, Double longitude, String cost, String taxiName, String voditelName)
    {
        this.mLabel = label;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mIcon = icon;
        this.cost = cost;
        this.taxiName = taxiName;
        this.voditelName = voditelName;
    }

    public String getmLabel()
    {
        return mLabel;
    }

    public void setmLabel(String mLabel)
    {
        this.mLabel = mLabel;
    }

    public int getmIcon()
    {
        return mIcon;
    }

    public void setmIcon(int icon)
    {
        this.mIcon = icon;
    }

    public Double getmLatitude()
    {
        return mLatitude;
    }

    public void setmLatitude(Double mLatitude)
    {
        this.mLatitude = mLatitude;
    }

    public Double getmLongitude()
    {
        return mLongitude;
    }

    public void setmLongitude(Double mLongitude)
    {
        this.mLongitude = mLongitude;
    }
}