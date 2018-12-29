package jdm.view;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import JMUtilities.DBConnection;
import JDMSchedApp.JDMScheduling;
import jdm.model.Appointment;
import jdm.model.Customer;
import jdm.model.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * FXML controller class
 * @author jmacin1
 */
public class AppointmentEditScreenController {

    @FXML private Label apptLabel;

    @FXML private TextField titleField;

    @FXML private ComboBox<String> startComboBox;

    @FXML private ComboBox<String> endComboBox;

    @FXML private DatePicker datePicker;

    @FXML private ComboBox<String> typeComboBox;

    @FXML private Button apptSaveButton;

    @FXML private Button apptCancelButton;

    @FXML private TableView<Customer> customerSelectTableView;

    @FXML private TableColumn<Customer, String> customerNameApptColumn;

    @FXML private TextField customerSearchField;

    private Stage dialogStage;
    private JDMScheduling mainApp;
    private boolean okClicked = false;
    private final ZoneId zid = ZoneId.systemDefault();
    private Appointment selectedAppt;
    private User currentUser;
    
    private ObservableList<Customer> masterData = FXCollections.observableArrayList();
    private final ObservableList<String> startTimes = FXCollections.observableArrayList();
    private final ObservableList<String> endTimes = FXCollections.observableArrayList();
    private final DateTimeFormatter timeDTF = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
    private final DateTimeFormatter dateDTF = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
    ObservableList<Appointment> apptTimeList;
    
    /**
     * returns true if the user clicked OK, false otherwise
     * allows application to decide whether to insert or update a record
     * @return
     */
    public boolean isOkClicked() {
        return okClicked;
    }    

    @FXML private void handleSave(ActionEvent event) {
        if (validateAppointment()){
            if (isOkClicked()) {
                updateAppt();            
            } 
            else {
                saveAppt();
            }
            dialogStage.close();
        }
    }

    @FXML private void handleCancel(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Cancel");
        alert.setHeaderText("Are You Certain You Want To Cancel?");
        alert.showAndWait()
        .filter(response -> response == ButtonType.OK)
        .ifPresent(response -> dialogStage.close());
    }
    
    /**
     * initializes AppointmentEditScreen
     * @param dialogStage
     * @param currentUser 
     */
    public void setDialogStage(Stage dialogStage, User currentUser) {
        this.dialogStage = dialogStage;
        this.currentUser = currentUser;
        
        populateTypeList();
        customerNameApptColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        masterData = populateCustomerList();
        
        //wrap the ObservableList in a FilteredList (initially display all data).
        FilteredList<Customer> filteredData = new FilteredList<>(masterData, p -> true);

        //lambda. set the filter predicate whenever the filter changes.
        customerSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(customer -> {
                //if the filter text is empty display all persons.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                } 
                //compare the name of every customer with the filter text.
                String lowerCaseFilter = newValue.toLowerCase();
                if (customer.getCustomerName().toLowerCase().contains(lowerCaseFilter)) {
                    return true; //filter matches first name.
                }
                return false; //does not match.
            });
        });

        //puts FilteredList in a SortedList. 
        SortedList<Customer> sortedData = new SortedList<>(filteredData);

        //binds the SortedList comparator to the TableView comparator.
        sortedData.comparatorProperty().bind(customerSelectTableView.comparatorProperty());

        //add sorted and filtered data to the table.
        customerSelectTableView.setItems(sortedData);
        
        /**
         * sets time based on business hours being 8am - 5pm
         * locks available time within business hours
         */
	LocalTime time = LocalTime.of(8, 0);
	do {
		startTimes.add(time.format(timeDTF));
		endTimes.add(time.format(timeDTF));
		time = time.plusMinutes(15);
	} 
        while(!time.equals(LocalTime.of(17, 15)));
		startTimes.remove(startTimes.size() - 1);
		endTimes.remove(0);
        
        datePicker.setValue(LocalDate.now());

        startComboBox.setItems(startTimes);
	endComboBox.setItems(endTimes);
	startComboBox.getSelectionModel().select(LocalTime.of(8, 0).format(timeDTF));
	endComboBox.getSelectionModel().select(LocalTime.of(8, 15).format(timeDTF));
    }
    
    /**
     * transposes appointment details from selected appointment in Appointment screen
     * into Appointment Edit Screen
     * @param appointment 
     */
    public void setAppointment(Appointment appointment) {
        okClicked = true;
        selectedAppt = appointment;
        
        String start = appointment.getStart();
        
        LocalDateTime startLDT = LocalDateTime.parse(start, dateDTF);
        String end = appointment.getEnd();
        LocalDateTime endLDT = LocalDateTime.parse(end, dateDTF);        
        
        apptLabel.setText("Edit Appointment");
        titleField.setText(appointment.getTitle());
        typeComboBox.setValue(appointment.getDescription());
        customerSelectTableView.getSelectionModel().select(appointment.getCustomer());
        datePicker.setValue(LocalDate.parse(appointment.getStart(), dateDTF));
        startComboBox.getSelectionModel().select(startLDT.toLocalTime().format(timeDTF));
        endComboBox.getSelectionModel().select(endLDT.toLocalTime().format(timeDTF)); 
    }
    
    /**
     * inserts new appointment record
     * sets local date time
     */
    private void saveAppt() {
        LocalDate localDate = datePicker.getValue();
	LocalTime startTime = LocalTime.parse(startComboBox.getSelectionModel().getSelectedItem(), timeDTF);
	LocalTime endTime = LocalTime.parse(endComboBox.getSelectionModel().getSelectedItem(), timeDTF);
        
        LocalDateTime startDT = LocalDateTime.of(localDate, startTime);
        LocalDateTime endDT = LocalDateTime.of(localDate, endTime);

        ZonedDateTime startUTC = startDT.atZone(zid).withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime endUTC = endDT.atZone(zid).withZoneSameInstant(ZoneId.of("UTC"));            
	
	Timestamp startsqlts = Timestamp.valueOf(startUTC.toLocalDateTime()); //this value can be inserted into database
        Timestamp endsqlts = Timestamp.valueOf(endUTC.toLocalDateTime()); //this value can be inserted into database        
        
        try {
                PreparedStatement pst = DBConnection.getConn().prepareStatement("INSERT INTO Appointment"
                + "(customerId, title, description, location, contact, url, start, end, createDate, createdBy, lastUpdate, lastUpdateBy)"
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)");
            
                pst.setString(1, customerSelectTableView.getSelectionModel().getSelectedItem().getCustomerId());
                pst.setString(2, titleField.getText());
                pst.setString(3, typeComboBox.getValue());
                pst.setString(4, "");
                pst.setString(5, "");
                pst.setString(6, "");
                pst.setTimestamp(7, startsqlts);
                pst.setTimestamp(8, endsqlts);
                pst.setString(9, currentUser.getUsername());
                pst.setString(10, currentUser.getUsername());
                int result = pst.executeUpdate();
                if (result == 1) {//one row was affected; namely the one that was inserted!
                    System.out.println("New Appointment Saved");
                } 
                else {
                    System.out.println("Dang, Appointment Didn't Save. Please Check Fields.");
                }
            } 
        catch (SQLException ex) {
            ex.printStackTrace();
            }
    }
    
    /**
     * updates edited appointment record in database
     */
    private void updateAppt() {
        LocalDate localDate = datePicker.getValue();
	LocalTime startTime = LocalTime.parse(startComboBox.getSelectionModel().getSelectedItem(), timeDTF);
	LocalTime endTime = LocalTime.parse(endComboBox.getSelectionModel().getSelectedItem(), timeDTF);
        
        LocalDateTime startDT = LocalDateTime.of(localDate, startTime);
        LocalDateTime endDT = LocalDateTime.of(localDate, endTime);

        ZonedDateTime startUTC = startDT.atZone(zid).withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime endUTC = endDT.atZone(zid).withZoneSameInstant(ZoneId.of("UTC"));            
	
	Timestamp startsqlts = Timestamp.valueOf(startUTC.toLocalDateTime()); //this value can be inserted into database
        Timestamp endsqlts = Timestamp.valueOf(endUTC.toLocalDateTime()); //this value can be inserted into database        
        
        try {
            PreparedStatement pst = DBConnection.getConn().prepareStatement("UPDATE Appointment "
             + "SET customerId = ?, title = ?, description = ?, start = ?, end = ?, lastUpdate = CURRENT_TIMESTAMP, lastUpdateBy = ? "
             + "WHERE appointmentId = ?");
            
             pst.setString(1, customerSelectTableView.getSelectionModel().getSelectedItem().getCustomerId());
             pst.setString(2, titleField.getText());
             pst.setString(3, typeComboBox.getValue());
             pst.setTimestamp(4, startsqlts);
             pst.setTimestamp(5, endsqlts);
             pst.setString(6, currentUser.getUsername());
             pst.setString(7, selectedAppt.getAppointmentId());
             int result = pst.executeUpdate();
             if (result == 1) {//one row was affected; namely the one that was inserted!
                System.out.println("Appointment Updated");
                }
             else {
                System.out.println("Dang, Appointment Didn't Update. Please Check Fields.");
                }
            }
        catch (SQLException ex) {
            ex.printStackTrace();
            }
    }
    
    /**
     * populates appointment type list
     */
    private void populateTypeList() {
        ObservableList<String> typeList = FXCollections.observableArrayList();
        typeList.addAll("Consultation", "New Account", "Follow Up", "Close Account");
        typeComboBox.setItems(typeList);
    }
    
    /**
     * populates list of customer names
     * @return customerList
     */
    protected ObservableList<Customer> populateCustomerList() {
        String tCustomerId;
        String tCustomerName;
        
        ObservableList<Customer> customerList = FXCollections.observableArrayList();
        try(
        PreparedStatement statement = DBConnection.getConn().prepareStatement(
        "SELECT Customer.customerId, Customer.customerName " +
        "FROM Customer, Address, City, Country " +
        "WHERE Customer.addressId = Address.addressId AND Address.cityId = City.cityId AND City.countryId = Country.countryId");
            ResultSet rs = statement.executeQuery();){
           
            while (rs.next()) {
                tCustomerId = rs.getString("Customer.customerId");
                tCustomerName = rs.getString("Customer.customerName");
                customerList.add(new Customer(tCustomerId, tCustomerName));
            }
        } 
        catch (SQLException sqe) {
            System.out.println("Check Your SQL");
            sqe.printStackTrace();
        } 
        catch (Exception e) {
            System.out.println("Something Besides The SQL Went Wrong. Check The Database Connection.");
        }
        return customerList;
    }

    /**
     * validates appointment information before inserting or updating records
     * @return true if valid, false if there is an error in fields
     */
    private boolean validateAppointment() {
        String title = titleField.getText();
        String type = typeComboBox.getValue();
        Customer customer = customerSelectTableView.getSelectionModel().getSelectedItem();
        LocalDate localDate = datePicker.getValue();
	LocalTime startTime = LocalTime.parse(startComboBox.getSelectionModel().getSelectedItem(), timeDTF);
	LocalTime endTime = LocalTime.parse(endComboBox.getSelectionModel().getSelectedItem(), timeDTF);
        
        LocalDateTime startDT = LocalDateTime.of(localDate, startTime);
        LocalDateTime endDT = LocalDateTime.of(localDate, endTime);

        ZonedDateTime startUTC = startDT.atZone(zid).withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime endUTC = endDT.atZone(zid).withZoneSameInstant(ZoneId.of("UTC"));            
	        
        String errorMessage = "";
        //first checks to see if inputs are null
        if (title == null || title.length() == 0) {
            errorMessage += "Enter an Appointment title.\n"; 
        }
        if (type == null || type.length() == 0) {
            errorMessage += "Select an Appointment type.\n";  
        } 
        if (customer == null) {
            errorMessage += "Select a Customer.\n"; 
        } 
        if (startUTC == null) {
            errorMessage += "Select a Start time"; 
        }         
        if (endUTC == null) {
            errorMessage += "Select an End time.\n"; 
            //checks to make sure Start and End times are not the same
            } 
        else if (endUTC.equals(startUTC) || endUTC.isBefore(startUTC)){
                errorMessage += "End Time Must Be Later Than Start Time.\n";
            } 
        else try {
                //checks user's existing appointments for time conflicts
                if (hasApptConflict(startUTC, endUTC)){
                    errorMessage += "Appointment Time Conflicts With An Existing Appointment. Please Select A New Time.\n";
                }
        } 
        catch (SQLException ex) {
            Logger.getLogger(AppointmentEditScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (errorMessage.length() == 0) {
            return true;
        } 
        else {
            // Show the error message.
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please Correct Invalid Appointment Fields");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }
    
    /**
     * SELECT statement searches database against proposed times
     * @param newStart
     * @param newEnd
     * @return true if result set contains any matching appointments
     * @throws SQLException 
     */
    private boolean hasApptConflict(ZonedDateTime newStart, ZonedDateTime newEnd) throws SQLException {
        String apptID;
        String consultant;
        if (isOkClicked()) {
            //edited appointment
            apptID = selectedAppt.getAppointmentId();
            consultant = selectedAppt.getUser();
        } 
        else {
            //new appointment
            apptID = "0";
            consultant = currentUser.getUsername();
        }
        System.out.println("ApptID: " + apptID);
        
        try{
            PreparedStatement pst = DBConnection.getConn().prepareStatement(
            "SELECT * FROM Appointment "
            + "WHERE (? BETWEEN start AND end OR ? BETWEEN start AND end OR ? < start AND ? > end) "
            + "AND (createdBy = ? AND appointmentID != ?)");
            pst.setTimestamp(1, Timestamp.valueOf(newStart.toLocalDateTime()));
            pst.setTimestamp(2, Timestamp.valueOf(newEnd.toLocalDateTime()));
            pst.setTimestamp(3, Timestamp.valueOf(newStart.toLocalDateTime()));
            pst.setTimestamp(4, Timestamp.valueOf(newEnd.toLocalDateTime()));
            pst.setString(5, consultant);
            pst.setString(6, apptID);
            ResultSet rs = pst.executeQuery();
            if(rs.next()) {
                return true;
            }   
        } 
        catch (SQLException sqe) {
            System.out.println("Check your SQL");
            sqe.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("Something Besides The SQL Went Wrong In Start And End Times.");
            e.printStackTrace();
        }
        return false;
    }
}
