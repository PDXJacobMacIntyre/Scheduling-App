package jdm.view;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import JMUtilities.DBConnection;
import JDMSchedApp.JDMScheduling;
import jdm.model.Appointment;
import jdm.model.Customer;
import jdm.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

/**
 * FXML controller class
 * @author jmacin1
 */
public class AppointmentScreenController {
     
    @FXML private TableView<Appointment> apptTableView;

    @FXML private TableColumn<Appointment, ZonedDateTime> startApptColumn;

    @FXML private TableColumn<Appointment, LocalDateTime> endApptColumn;
    
    @FXML private TableColumn<Appointment, String> titleApptColumn;

    @FXML private TableColumn<Appointment, String> typeApptColumn;

    @FXML private TableColumn<Appointment, Customer> customerApptColumn;

    @FXML private TableColumn<Appointment, String> consultantApptColumn;

    @FXML private RadioButton weekRadioButton;

    @FXML private RadioButton monthRadioButton;
    
    @FXML private ToggleGroup apptToggleGroup;
    
    private JDMScheduling mainApp;
    private final DateTimeFormatter timeDTF = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
    private final ZoneId newzid = ZoneId.systemDefault();
    private User currentUser;
    ObservableList<Appointment> apptList;
    
    /**
     * initializes appointment screen
     * @param mainApp
     * @param currentUser 
     */    
    public void setAppointmentScreen(JDMScheduling mainApp, User currentUser) {
	this.mainApp = mainApp;
        this.currentUser = currentUser;
        
        apptToggleGroup = new ToggleGroup();
        this.weekRadioButton.setToggleGroup(apptToggleGroup);
        this.monthRadioButton.setToggleGroup(apptToggleGroup);
        
        startApptColumn.setCellValueFactory(new PropertyValueFactory<>("start"));
        endApptColumn.setCellValueFactory(new PropertyValueFactory<>("end"));
        titleApptColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeApptColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        customerApptColumn.setCellValueFactory(new PropertyValueFactory<>("customer"));
        consultantApptColumn.setCellValueFactory(new PropertyValueFactory<>("user"));
        
        apptList = FXCollections.observableArrayList();
        populateAppointmentList();
        apptTableView.getItems().setAll(apptList);        
    }
    
    /**
     * filters to show appointments from current and local date/time to a month out
     * lambda 
     * @param event 
     */
    @FXML void handleApptMonth(ActionEvent event) {
        //create instances of LocalDate class  
        LocalDate now = LocalDate.now();
        LocalDate nowPlus1Month = now.plusMonths(1);
        //wrap the ObservableList in a FilteredList because initially displays all data
        FilteredList<Appointment> filteredData = new FilteredList<>(apptList);
        //sets row filter predicate every time the filter changes. 
        filteredData.setPredicate(row -> {
            //uses local date to set variable with start time from DateTimeFormatter
            LocalDate rowDate = LocalDate.parse(row.getStart(), timeDTF);
            //returns all instances of rowDate minus 1 day from now and
            //before 1 month from now, i.e. this month.
            return (rowDate.isAfter(now.minusDays(1)) && rowDate.isBefore(nowPlus1Month));
        });
        //sets filtered data to table view
        apptTableView.setItems(filteredData);
    }
    
    /**
     * filters to show appointments from current and local date/time to a week out
     * @param event 
     */
    @FXML void handleApptWeek(ActionEvent event) {
        
        LocalDate now = LocalDate.now();
        LocalDate nowPlus7 = now.plusDays(7);
        LocalDate nowMinus1 = now.minusDays(1);
        FilteredList<Appointment> filteredData = new FilteredList<>(apptList);
        filteredData.setPredicate(row -> {
            LocalDate rowDate = LocalDate.parse(row.getStart(), timeDTF);
            return (rowDate.isAfter(nowMinus1) && rowDate.isBefore(nowPlus7));
        });
        apptTableView.setItems(filteredData);
    }
    
    //appointment delete handler
    @FXML void handleDeleteAppt(ActionEvent event) {
        Appointment selectedAppointment = apptTableView.getSelectionModel().getSelectedItem();
        
        if (selectedAppointment != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Are You Certain You Want To Delete " + selectedAppointment.getTitle() + " Scheduled For " + selectedAppointment.getStart() + "?");
            alert.showAndWait()
            .filter(response -> response == ButtonType.OK)
            .ifPresent(response -> {//if delete confirmed, appt deleted and appointment screen is shown with new list minus deleted appt
                deleteAppointment(selectedAppointment);
                mainApp.showAppointmentScreen(currentUser);
                });
        } 
        else {//if an appointment has not been selected, display warning
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Appointment Selected For Deletion");
            alert.setContentText("Please Select An Appointment In The Table To Delete");
            alert.showAndWait();
        }
    }
    
    //edit appointment handler / error warning
    @FXML void handleEditAppt(ActionEvent event) {
        Appointment selectedAppointment = apptTableView.getSelectionModel().getSelectedItem();
        
        if (selectedAppointment != null) {
            boolean okClicked = mainApp.showEditApptScreen(selectedAppointment, currentUser);
            mainApp.showAppointmentScreen(currentUser);
        } 
        else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Appointment Selected");
            alert.setContentText("Please Select An Appointment From The Table");
            alert.showAndWait();
        }
    }
    
    //new appointment handler, uses local date time
    @FXML void handleNewAppt(ActionEvent event) throws IOException{
        boolean okClicked = mainApp.showNewApptScreen(currentUser);
        mainApp.showAppointmentScreen(currentUser);
    }
    
    //populates appointment list from db
    private void populateAppointmentList() {
        try{ 
        PreparedStatement statement = DBConnection.getConn().prepareStatement(
        "SELECT Appointment.appointmentId, Appointment.customerId, Appointment.title, Appointment.description, "
                + "Appointment.`start`, Appointment.`end`, Customer.customerId, Customer.customerName, Appointment.createdBy "
                + "FROM Appointment, Customer "
                + "WHERE Appointment.customerId = Customer.customerId "
                + "ORDER BY `start`");
            ResultSet rs = statement.executeQuery();
           
            while (rs.next()) {
                String tAppointmentId = rs.getString("Appointment.appointmentId");
                Timestamp tsStart = rs.getTimestamp("Appointment.start");
                ZonedDateTime newzdtStart = tsStart.toLocalDateTime().atZone(ZoneId.of("UTC"));
        	ZonedDateTime newLocalStart = newzdtStart.withZoneSameInstant(newzid);

                Timestamp tsEnd = rs.getTimestamp("Appointment.end");
                ZonedDateTime newzdtEnd = tsEnd.toLocalDateTime().atZone(ZoneId.of("UTC"));
        	ZonedDateTime newLocalEnd = newzdtEnd.withZoneSameInstant(newzid);

                String tTitle = rs.getString("Appointment.title");
                String tType = rs.getString("Appointment.description");
                Customer tCustomer = new Customer(rs.getString("Appointment.customerId"), rs.getString("Customer.customerName"));
                String tUser = rs.getString("Appointment.createdBy");   
                apptList.add(new Appointment(tAppointmentId, newLocalStart.format(timeDTF), newLocalEnd.format(timeDTF), tTitle, tType, tCustomer, tUser));
            }
        } 
        catch (SQLException sqe) {
            System.out.println("Check Your SQL Data");
            sqe.printStackTrace();
        } 
        catch (Exception e) {
            System.out.println("Something Besides The SQL Went Wrong. Check Database Connection.");
        }
    }
    
    //delete appointment
    private void deleteAppointment(Appointment appointment) {
        try{           
            PreparedStatement pst = DBConnection.getConn().prepareStatement("DELETE Appointment.* FROM Appointment WHERE Appointment.appointmentId = ?");
            pst.setString(1, appointment.getAppointmentId()); 
            pst.executeUpdate(); 
        } 
        catch(SQLException e){
            e.printStackTrace();
        }       
    }
}
