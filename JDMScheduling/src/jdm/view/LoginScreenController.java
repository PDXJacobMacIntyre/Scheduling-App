package jdm.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.sql.*;
import JMUtilities.DBConnection;
import JDMSchedApp.JDMScheduling;
import jdm.model.Appointment;
import jdm.model.Customer;
import jdm.model.User;
import JMUtilities.LoggerUtil;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.text.Text;

/**
 * FXML controller class
 * @author jmacin1
 */

public class LoginScreenController {
    @FXML private Label errorMessage;

    @FXML private TextField usernameField;

    @FXML private PasswordField passwordField;

    @FXML private Text usernameText;

    @FXML private Text passwordText;

    @FXML private Text titleText;

    @FXML private Button signinText;

    @FXML private Button cancelText;
    
    //reference to the main application.
    private JDMScheduling mainApp;
    ResourceBundle rb = ResourceBundle.getBundle("login", Locale.getDefault());
    private final ZoneId newzid = ZoneId.systemDefault();
    private final DateTimeFormatter timeDTF = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
    User user = new User();
    ObservableList<Appointment> reminderList;
    private final static Logger LOGGER = Logger.getLogger(LoggerUtil.class.getName());

    /**
     * the constructor
     * constructor is called before the initialize() method.
     */
    public LoginScreenController() {
    }
    
    //collect signin input and check fields
    @FXML void handleSignInAction(ActionEvent event) {
        String userN = usernameField.getText();
        String pass = passwordField.getText();
        
        if(userN.length()==0 || pass.length()==0) 
            errorMessage.setText(rb.getString("empty"));
        else{
            User validUser = validateLogin(userN,pass); 
            if (validUser == null) {
                errorMessage.setText(rb.getString("incorrect"));
                return;
            }
            populateReminderList();
            reminder();
            mainApp.showMenu(validUser);
            mainApp.showAppointmentScreen(validUser);
            //logs successful logins
            LOGGER.log(Level.INFO, "{0} logged in", validUser.getUsername());
        }
    }
    
    /**
     * searches for matching username and password in database
     * @param username
     * @param password
     * @return user if match found
     */
    User validateLogin(String username,String password) {
        try{           
            PreparedStatement pst = DBConnection.getConn().prepareStatement("SELECT * FROM User WHERE userName=? AND password=?");
            pst.setString(1, username); 
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();                        
            if(rs.next()){
                user.setUsername(rs.getString("userName"));
                user.setPassword(rs.getString("password"));
                user.setUserID(rs.getInt("userId"));
            } 
            else {
                return null;    
            }             
        } 
        catch(SQLException e){
            e.printStackTrace();
        }       
        return user;
}
    
    @FXML void handleCancelAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Cancel");
            alert.setHeaderText("You Are About To Close The Program, Are You Certain?");
            alert.showAndWait()
            .filter(response -> response == ButtonType.OK)
            .ifPresent((ButtonType response) -> {
                Platform.exit();
                System.exit(0);
                }
            );
    }
    
    /**
     * initializes loginScreen
     * @param mainApp 
     */
    public void setLogin(JDMScheduling mainApp) {
	this.mainApp = mainApp;
        reminderList = FXCollections.observableArrayList();
        
        titleText.setText(rb.getString("title"));
        usernameText.setText(rb.getString("username"));
        passwordText.setText(rb.getString("password"));
        signinText.setText(rb.getString("signin"));
        cancelText.setText(rb.getString("cancel"));
    }
    
    /**
     * filters reminder list and launches alert if filteredData is not empty
     */
    private void reminder() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nowPlus15Min = now.plusMinutes(15);
        
        FilteredList<Appointment> filteredData = new FilteredList<>(reminderList);
        filteredData.setPredicate(row -> {
            LocalDateTime rowDate = LocalDateTime.parse(row.getStart(), timeDTF);
            return rowDate.isAfter(now.minusMinutes(1)) && rowDate.isBefore(nowPlus15Min);
            }
        );
        if (filteredData.isEmpty()) {
            System.out.println("No Reminders");
        } 
        else {
            String type = filteredData.get(0).getDescription();
            String customer =  filteredData.get(0).getCustomer().getCustomerName();
            String start = filteredData.get(0).getStart();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Upcoming Appointment Reminder");
            alert.setHeaderText("Reminder: You Have The Following Appointment Set For The Next 15 Minutes.");
            alert.setContentText("Your Upcoming " + type + " Appointment With " + customer +
                " Is Currently Set For " + start + ".");
            alert.showAndWait();
        }
    }
    
    private void populateReminderList() {
        System.out.println(user.getUsername());
        try{
            PreparedStatement pst = DBConnection.getConn().prepareStatement(
            "SELECT Appointment.appointmentId, Appointment.customerId, Appointment.title, Appointment.description, "
                + "Appointment.`start`, Appointment.`end`, Customer.customerId, Customer.customerName, Appointment.createdBy "
                + "FROM Appointment, Customer "
                + "WHERE Appointment.customerId = Customer.customerId AND Appointment.createdBy = ? "
                + "ORDER BY `start`");
            pst.setString(1, user.getUsername());
            ResultSet rs = pst.executeQuery();
           
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
                      
                reminderList.add(new Appointment(tAppointmentId, newLocalStart.format(timeDTF), newLocalEnd.format(timeDTF), tTitle, tType, tCustomer, tUser));   
            }   
        } 
        catch (SQLException sqe) {
            System.out.println("Check your SQL");
            sqe.printStackTrace();
        } 
        catch (Exception e) {
            System.out.println("Something besides the SQL went wrong.");
            e.printStackTrace();
        }
    }
}
