package jdm.model;

/**
 * @author jmacin1
 */

//object class to place in list for appointment types by month report
public class AppointmentReport {
    private String month;
    private String amount;
    private String type;    

    /**
     * @param month
     * @param type
     * @param amount
     */
    public AppointmentReport(String month, String type, String amount) {
        this.month = month;
        this.type = type;
        this.amount = amount;
    }

    public void setMonth(String month) {
        this.month = month;
    }
    
    public String getMonth() {
        return month;
    }

    public void setType(String type) {
        this.type = type;
    }
        
    public String getType() {
        return type;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
    
    public String getAmount() {
        return amount;
    }
}
