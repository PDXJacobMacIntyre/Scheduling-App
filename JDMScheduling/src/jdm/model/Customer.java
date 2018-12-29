package jdm.model;

/**
 * @author jmacin1
 */
public class Customer {
    private String customerId;
    private String customerName;
    private String address;
    private String address2;
    private City city;
    private String country;
    private String postalCode;
    private String phone;
    
    public Customer(){
    }
    
    public Customer(String customerId, String customerName, String address, String address2, City city, String country, String postalCode, String phone) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.address = address;
        this.address2 = address2;
        this.city = city;
        this.country = country;
        this.postalCode = postalCode;
        this.phone = phone;
    }
    
    public Customer (String customerId, String customerName){
        this.customerId = customerId;
        this.customerName = customerName;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public String getAddress() {
        return address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getAddress2() {
        return address2;
    }

    public void setCity(City city) {
        this.city = city;
    }
    
    public City getCity() {
        return city;
    }
    
    public int getCityId(City object) {
            return object.getCityId();
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }
    
    @Override public String toString() {
        return customerName;
    }
}
