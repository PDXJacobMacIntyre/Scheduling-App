package jdm.view;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import JMUtilities.DBConnection;
import JDMSchedApp.JDMScheduling;
import jdm.model.Appointment;
import jdm.model.AppointmentReport;
import jdm.model.Customer;
import jdm.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * FXML controller class
 * @author jmacin1 
 */
public class ReportsController {
    @FXML private TabPane tabPane;

    @FXML private Tab schedTab;

    @FXML private TableView<Appointment> schedTableView;

    @FXML private TableColumn<Appointment, ZonedDateTime> startSchedColumn;

    @FXML private TableColumn<Appointment, LocalDateTime> endSchedColumn;
    
    @FXML private TableColumn<Appointment, String> titleSchedColumn;

    @FXML private TableColumn<Appointment, String> typeSchedColumn;

    @FXML private TableColumn<Appointment, Customer> customerSchedColumn;

    @FXML private Tab apptTab;

    @FXML private TableView<AppointmentReport> apptTableView;

    @FXML private TableColumn<AppointmentReport, String> monthColumn;

    @FXML private TableColumn<AppointmentReport, String> typeColumn;

    @FXML private TableColumn<AppointmentReport, String> typeAmount;

    @FXML private Tab custTab;
    
    @FXML private BarChart barChart;
    
    @FXML private CategoryAxis xAxis;

    @FXML private NumberAxis yAxis;
    
    private JDMScheduling mainApp;
    private ObservableList<AppointmentReport> apptList;
    private ObservableList<Appointment> schedule;
    private ObservableList<PieChart.Data> pieChartData;
    private final DateTimeFormatter timeDTF = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
    private final ZoneId newzid = ZoneId.systemDefault();
    private User currentUser;
    
    public ReportsController() {
    }
    
    public void setReports(JDMScheduling mainApp, User currentUser) {
        this.mainApp = mainApp;
        this.currentUser = currentUser;
        
        //methods called to populate data on each tab        
        populateApptTypeList();
        populateCustBarChart();
        populateSchedule();      
        
        startSchedColumn.setCellValueFactory(new PropertyValueFactory<>("start"));
        endSchedColumn.setCellValueFactory(new PropertyValueFactory<>("end"));
        titleSchedColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeSchedColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        customerSchedColumn.setCellValueFactory(new PropertyValueFactory<>("customer"));
        
        monthColumn.setCellValueFactory(new PropertyValueFactory<>("Month"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("Type"));
        typeAmount.setCellValueFactory(new PropertyValueFactory<>("Amount"));
    }
   
    private void populateApptTypeList() {
        apptList = FXCollections.observableArrayList();
        
        try {
            PreparedStatement statement = DBConnection.getConn().prepareStatement(
             "SELECT MONTHNAME(`start`) AS \"Month\", description AS \"Type\", COUNT(*) as \"Amount\" "
             + "FROM Appointment "
             + "GROUP BY MONTHNAME(`start`), description");
             ResultSet rs = statement.executeQuery();
           
            while (rs.next()) {
                String month = rs.getString("Month");
                String type = rs.getString("Type");
                String amount = rs.getString("Amount");
                apptList.add(new AppointmentReport(month, type, amount));
            } 
        } 
        catch (SQLException sqe) {
            System.out.println("Check The SQL");
            sqe.printStackTrace();
        } 
        catch (Exception e) {
            System.out.println("Something Besides The SQL Went Wrong.");
        }
        apptTableView.getItems().setAll(apptList);
    }
    
    private void populateCustBarChart() {
        ObservableList<XYChart.Data<String, Integer>> data = FXCollections.observableArrayList();
        XYChart.Series<String, Integer> series = new XYChart.Series<>();

            try { PreparedStatement pst = DBConnection.getConn().prepareStatement(
                  "SELECT City.city, COUNT(city) "
                + "FROM Customer, Address, City "
                + "WHERE Customer.addressId = Address.addressId "
                + "AND Address.cityId = City.cityId "
                + "GROUP BY city"); 
                ResultSet rs = pst.executeQuery();

                while (rs.next()) {
                    String city = rs.getString("city");
                    Integer count = rs.getInt("COUNT(city)");
                    data.add(new Data<>(city, count));
                }
            } 
            catch (SQLException sqe) {
                System.out.println("Check The SQL");
                sqe.printStackTrace();
            } 
            catch (Exception e) {
                System.out.println("Something Besides The SQL Went Wrong.");
                e.printStackTrace();
            }             
        series.getData().addAll(data);
        barChart.getData().add(series);
    }
    
    private void populateSchedule() {
        schedule = FXCollections.observableArrayList();
        
        try {
            PreparedStatement pst = DBConnection.getConn().prepareStatement(
                    "SELECT Appointment.appointmentId, Appointment.customerId, Appointment.title, Appointment.description, "
                    + "Appointment.`start`, Appointment.`end`, Customer.customerId, Customer.customerName, Appointment.createdBy "
                    + "FROM Appointment, Customer "
                    + "WHERE Appointment.customerId = Customer.customerId AND Appointment.`start` >= CURRENT_DATE AND Appointment.createdBy = ?"
                    + "ORDER BY `start`");
            pst.setString(1, currentUser.getUsername());
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
                schedule.add(new Appointment(tAppointmentId, newLocalStart.format(timeDTF), newLocalEnd.format(timeDTF), tTitle, tType, tCustomer, tUser));
            }
        } 
        catch (SQLException sqe) {
            System.out.println("Check your SQL");
            sqe.printStackTrace();
        } 
        catch (Exception e) {
            System.out.println("Something besides the SQL went wrong.");
        }
        schedTableView.getItems().setAll(schedule);
    }
}
