
package JDMSchedApp;

import JMUtilities.DBConnection;
import java.io.IOException;
import java.sql.Connection;
import java.util.Locale;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import static javafx.application.Application.launch;
import jdm.view.LoginScreenController;
import jdm.view.CustomerScreenController;
import jdm.view.AppointmentScreenController;
import jdm.view.MenuController;
import jdm.view.AppointmentEditScreenController;
import jdm.view.ReportsController;
import jdm.model.Appointment;
import jdm.model.User;
import JMUtilities.LoggerUtil;

/**
 * @author jmacin1
 */
public class JDMScheduling extends Application {
    
    private Stage primaryStage;
    private BorderPane menu;
    private AnchorPane loginScreen;
    private AnchorPane customerScreen;
    private AnchorPane appointmentScreen;
    private AnchorPane custReportScreen;
    private TabPane tabPane;
    Locale locale = Locale.getDefault();
    private static Connection connection;

    @Override public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("JDM Scheduling App");
        showLoginScreen();
    }
   
    /**
     * remove comment tags from main method below to set
     * French lang login and errors.
     * calls init() for db and logger
     * runs args then calls closeConn() to close db connection
     * @param args 
     */
    public static void main(String[] args) {
        //Locale.setDefault(new Locale("fr", "FR"));
        //System.out.println(Locale.getDefault()); 
        DBConnection.init();
        connection = DBConnection.getConn();
        LoggerUtil.init();
        launch(args);
        DBConnection.closeConn();
    }
    
    /**initializes the main menu.
     * @param currentUser
     */
    public void showMenu(User currentUser) {
        try {
            //loads main menu fxml file
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(JDMScheduling.class.getResource("/jdm/view/Menu.fxml"));
            menu = (BorderPane) loader.load();

            //show the scene containing the main menu frame
            Scene scene = new Scene(menu);
            primaryStage.setScene(scene);
            
            //give the controller access to the main app
            MenuController controller = loader.getController();
            controller.setMenu(this, currentUser);
            
            primaryStage.show();
        } 
        catch (IOException e) {
            e.getCause().printStackTrace();
        }
    }
    
    //brings up login screen
    public void showLoginScreen() {
        try {
            //load login screen
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(JDMScheduling.class.getResource("/jdm/view/LoginScreen.fxml"));
            AnchorPane loginScreen = (AnchorPane) loader.load();
            
            //gives controller access to the main app
            LoginScreenController controller = loader.getController();
            controller.setLogin(this);            
            
            //show the scene containing the main menu layout
            Scene scene = new Scene(loginScreen);
            primaryStage.setScene(scene);
            primaryStage.show();
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * calls appointment screen and passes in currentUser
     * @param currentUser 
     */
    public void showAppointmentScreen(User currentUser) {
        try {
            //load person overview
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(JDMScheduling.class.getResource("/jdm/view/AppointmentScreen.fxml"));
            AnchorPane appointmentScreen = (AnchorPane) loader.load();

            //set person overview into the center of root layout
            menu.setCenter(appointmentScreen);

            //give the controller access to the main app
            AppointmentScreenController controller = loader.getController();
            controller.setAppointmentScreen(this, currentUser);
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * calls new appointment screen, passes currentUser,
     * returns false so subsequent save is done as new appointment
     * @param currentUser
     * @return 
     */
    public boolean showNewApptScreen(User currentUser) {
    try {
        //load the fxml file and create a new stage for the popup dialog.
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(JDMScheduling.class.getResource("/jdm/view/AppointmentEditScreen.fxml"));
        AnchorPane newApptScreen = (AnchorPane) loader.load();

        //create the dialog Stage.
        Stage dialogStage = new Stage();
        dialogStage.setTitle("New Appointment");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        Scene scene = new Scene(newApptScreen);
        dialogStage.setScene(scene);

        //set the person into the controller.
        AppointmentEditScreenController controller = loader.getController();
        controller.setDialogStage(dialogStage, currentUser);

        //show the dialog and wait until the user closes it
        dialogStage.showAndWait();
        return controller.isOkClicked();
        } 
    catch (IOException e) {
        e.printStackTrace();
        return false;
        }
    }
    
    /**
     * call customer screen and pass currentUser
     * @param currentUser 
     */
    public void showCustomerScreen(User currentUser) {
        try {
            //load person overview
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(JDMScheduling.class.getResource("/jdm/view/CustomerScreen.fxml"));
            AnchorPane customerScreen = (AnchorPane) loader.load();

            //set person overview into the center of main menu layout
            menu.setCenter(customerScreen);

            //give the controller access to the main app
            CustomerScreenController controller = loader.getController();
            controller.setCustomerScreen(this, currentUser);
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public boolean showEditApptScreen(Appointment appointment, User currentUser) {
    try {
        //load the fxml file and create a new stage for the popup dialog
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(JDMScheduling.class.getResource("/jdm/view/AppointmentEditScreen.fxml"));
        AnchorPane editApptScreen = (AnchorPane) loader.load();

        //create the dialog Stage
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Edit Appointment");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        Scene scene = new Scene(editApptScreen);
        dialogStage.setScene(scene);

        //set the person into the controller
        AppointmentEditScreenController controller = loader.getController();
        controller.setDialogStage(dialogStage, currentUser);
        controller.setAppointment(appointment);

        //show the dialog and wait until the user closes it
        dialogStage.showAndWait();
        return controller.isOkClicked();
        } 
    catch (IOException e) {
        e.printStackTrace();
        return false;
        }
    }
    
    public void showReports(User currentUser) {
        try {
            //load person overview report
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(JDMScheduling.class.getResource("/jdm/view/Reports.fxml"));
            TabPane tabPane = (TabPane) loader.load();

            //set person overview into the center of root layout
            menu.setCenter(tabPane);

            // Give the controller access to the main app
            ReportsController controller = loader.getController();
            controller.setReports(this, currentUser);
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
