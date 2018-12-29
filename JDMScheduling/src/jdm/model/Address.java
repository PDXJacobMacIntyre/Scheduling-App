package jdm.model;

/**
 * address class
 * @author jmacin1
 */
public class Address {
    private Integer addressId;
    private String address;
    private String address2;
    private int cityId;
    private String postalCode;
    private String phone;

    public Address() {
    }

    public Address(Integer addressId) {
        this.addressId = addressId;
    }

    //constructor
    public Address(Integer addressId, String address, String address2, int cityId, String postalCode, String phone) {
        this.addressId = addressId;
        this.address = address;
        this.address2 = address2;
        this.cityId = cityId;
        this.postalCode = postalCode;
        this.phone = phone;
    }

    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }

    public Integer getAddressId() {
        return addressId;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress2() {
        return address2;
    }
    
    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }
    
    @Override public int hashCode() {
        int hash = 0;
        hash += (addressId != null ? addressId.hashCode() : 0);
        return hash;
    }

    //address validation
    @Override public boolean equals(Object object) {
        if (!(object instanceof Address)) {
            return false;
        }
        Address other = (Address) object;
        if ((this.addressId == null && other.addressId != null) || (this.addressId != null && !this.addressId.equals(other.addressId))) {
            return false;
        }
        return true;
    }

    @Override public String toString() {
        return "model.Address[ addressId=" + addressId + " ]";
    }
}