package jdm.model;

/**
 * appointment class
 * @author jmacin1
 */
public class Appointment {
    private String appointmentId;
    private Customer customer;
    private String title;
    private String description;
    private String start;
    private String end;
    private String user;
    
    public Appointment() {
    }

    public Appointment(String appointmentId) {
        this.appointmentId = appointmentId;
    }
    
    //constructor
    public Appointment(String appointmentId, String start, String end, String title, String description, Customer customer, String user) {
        this.appointmentId = appointmentId;    
        this.start = start;
        this.end = end;
        this.title = title;
        this.description = description;
        this.customer = customer;
        this.user = user;
    }
    
    public Appointment(String start, String end, String user) {
        this.start = start;
        this.end = end;
        this.user = user;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
    
    public void setStart(String start) {
        this.start = start;
    }

    public String getStart() {
        return start;
    }
    
    public void setEnd(String end) {
        this.end = end;
    }
    
    public String getEnd() {
        return end;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Customer getCustomer() {
        return customer;
    }
    
    public void setUser(String user) {
        this.user = user;
    }
    
    public String getUser() {
        return user;
    }
}
