package jdm.model;

/**
 * @author jmacin1
 */
public class City {
    private int cityId;
    private String city;
    private int countryId;

    public City() {
    }

    public City(int cityId) {
        this.cityId = cityId;
    }

    public City(int cityId, String city, int countryId) {
        this.cityId = cityId;
        this.city = city;
        this.countryId = countryId;
    }
    
    public City(int cityId, String city) {
        this.cityId = cityId;
        this.city = city;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public int getCityId() {
        return cityId;
    }
    
    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }
    
    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public int getCountryId() {
        return countryId;
    }

    @Override public String toString() {
        return city;
    }
}
