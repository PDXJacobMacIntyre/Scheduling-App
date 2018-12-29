package jdm.view;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import JMUtilities.DBConnection;
import JDMSchedApp.JDMScheduling;
import jdm.model.City;
import jdm.model.Customer;
import jdm.model.User;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * FXML controller class
 * @author jmacin1
 */

public class CustomerScreenController {

    @FXML private TableView<Customer> customerTable;

    @FXML private TableColumn<Customer, String> customerNameColumn;

    @FXML private TableColumn<Customer, String> phoneColumn;
    
    @FXML private TextField customerIdField;

    @FXML private TextField nameField;

    @FXML private TextField addressField;

    @FXML private ComboBox<City> cityComboBox;

    @FXML private TextField address2Field;

    @FXML private TextField postalCodeField;

    @FXML private TextField phoneField;

    @FXML private TextField countryField;
    
    @FXML private ButtonBar newEditDeleteButtonBar;
    
    @FXML private ButtonBar saveCancelButtonBar;
    
    private JDMScheduling mainApp;
    private boolean editClicked = false;
    private Stage dialogStage;
    private User currentUser;
    
    public CustomerScreenController() {
    }
    
    //new customer handler
    @FXML void handleNewCustomer(ActionEvent event) {
        editClicked = false;
        enableCustomerFields();
        saveCancelButtonBar.setDisable(false);
        customerTable.setDisable(true);
        clearCustomerDetails();
        customerIdField.setText("Auto-Generated");
        newEditDeleteButtonBar.setDisable(true);        
    }
    
    //edit customer handler
    @FXML void handleEditCustomer(ActionEvent event) {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        
        if (selectedCustomer != null) {
            editClicked = true;
            enableCustomerFields();
            saveCancelButtonBar.setDisable(false);
            customerTable.setDisable(true);
            newEditDeleteButtonBar.setDisable(true);
        } 
        else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Customer Selected");
            alert.setContentText("Please Select a Customer In The Table");
            alert.showAndWait();
        }
    }

    //delete customer handler
    @FXML void handleDeleteCustomer(ActionEvent event) {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        
        if (selectedCustomer != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Are You Certain You Want To Delete " + selectedCustomer.getCustomerName() + "?");
            alert.showAndWait()
            .filter(response -> response == ButtonType.OK)
            .ifPresent(response -> {
                deleteCustomer(selectedCustomer);
                mainApp.showCustomerScreen(currentUser);
            }
            );
        } 
        else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Customer Selected For Deletion");
            alert.setContentText("Please Select a Customer In The Table To Delete");
            alert.showAndWait();
        }
    }
   
    //save customer handler
    @FXML void handleSaveCustomer(ActionEvent event) {
        if (validateCustomer()){
            saveCancelButtonBar.setDisable(true);
            customerTable.setDisable(false);
            if (editClicked == true) {
                //edits Customer record
                updateCustomer();
            } 
            else if (editClicked == false){
                //inserts new customer record
                saveCustomer();
            }
        mainApp.showCustomerScreen(currentUser);
        } 
    }
    
    @FXML void handleCancelCustomer(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Cancel");
        alert.setHeaderText("Are You Certain You Want To Cancel?");
        alert.showAndWait()
        .filter(response -> response == ButtonType.OK)
        .ifPresent(response -> {
            saveCancelButtonBar.setDisable(true);
            customerTable.setDisable(false);
            clearCustomerDetails();
            newEditDeleteButtonBar.setDisable(false);
            editClicked = false;
            }
        );
    }
    
    /**
     * initializes Customer screen
     * @param mainApp
     * @param currentUser 
     */
    public void setCustomerScreen(JDMScheduling mainApp, User currentUser) {
	this.mainApp = mainApp;
        this.currentUser = currentUser;
        
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        disableCustomerFields();
        populateCityList();
        
        cityComboBox.setConverter(new StringConverter<City>() {

        @Override public String toString(City object) {
        return object.getCity();
        }     

        @Override public City fromString(String string) {
        return cityComboBox.getItems().stream().filter(ap -> 
            ap.getCity().equals(string)).findFirst().orElse(null);
        }
        });
        
        cityComboBox.valueProperty().addListener((obs, oldval, newval) -> {
            if(newval != null)
                showCountry(newval.toString());
        });
        
        customerTable.getItems().setAll(populateCustomerList());          
        customerTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue)->showCustomerDetails(newValue));
    }
         
    /**
     * sets fields from selectedCustomer
     * @param selectedCustomer 
     */
    @FXML private void showCustomerDetails(Customer selectedCustomer) {
        customerIdField.setText(selectedCustomer.getCustomerId());
        nameField.setText(selectedCustomer.getCustomerName());
        addressField.setText(selectedCustomer.getAddress());
        address2Field.setText(selectedCustomer.getAddress2());
        cityComboBox.setValue(selectedCustomer.getCity());
        countryField.setText(selectedCustomer.getCountry());
        postalCodeField.setText(selectedCustomer.getPostalCode());
        phoneField.setText(selectedCustomer.getPhone());
    }
    
    //disables editing to not allow entry prior to clicking New or Edit
    private void disableCustomerFields() {
        nameField.setEditable(false);
        addressField.setEditable(false);
        address2Field.setEditable(false);
        postalCodeField.setEditable(false);
        phoneField.setEditable(false);
    }
    
    //enables editing after New or Edit clicked
    private void enableCustomerFields() {
        nameField.setEditable(true);
        addressField.setEditable(true);
        address2Field.setEditable(true);
        postalCodeField.setEditable(true);
        phoneField.setEditable(true);
    }
    
    //clears details listed in fields
    @FXML private void clearCustomerDetails() {
        customerIdField.clear();
        nameField.clear();
        addressField.clear();
        address2Field.clear();
        countryField.clear();
        postalCodeField.clear();
        phoneField.clear();
    }
    
    /**
     * Populates customerList for CustomerTable
     * @return customerList
     */
    protected List<Customer> populateCustomerList() {
        String tCustomerId;
        String tCustomerName;
        String tAddress;
        String tAddress2;
        City tCity;
        String tCountry;
        String tPostalCode;
        String tPhone;
        
        ObservableList<Customer> customerList = FXCollections.observableArrayList();
        try(
             
        PreparedStatement statement = DBConnection.getConn().prepareStatement(
        "SELECT Customer.customerId, Customer.customerName, Address.address, Address.address2, Address.postalCode, City.cityId, City.city, Country.country, Address.phone " +
        "FROM Customer, Address, City, Country " +
        "WHERE Customer.addressId = Address.addressId AND Address.cityId = City.cityId AND City.countryId = Country.countryId " +
        "ORDER BY Customer.customerName");
            ResultSet rs = statement.executeQuery();){
    
            while (rs.next()) {
                tCustomerId = rs.getString("Customer.customerId");
                tCustomerName = rs.getString("Customer.customerName");
                tAddress = rs.getString("Address.address");
                tAddress2 = rs.getString("Address.address2");
                tCity = new City(rs.getInt("City.cityId"), rs.getString("City.city"));
                tCountry = rs.getString("Country.country");
                tPostalCode = rs.getString("Address.postalCode");
                tPhone = rs.getString("Address.phone");
                customerList.add(new Customer(tCustomerId, tCustomerName, tAddress, tAddress2, tCity, tCountry, tPostalCode, tPhone));
            }
        } 
        catch (SQLException sqe) {
            System.out.println("Check Your SQL");
            sqe.printStackTrace();
        } 
        catch (Exception e) {
            System.out.println("Something Besides The SQL Went Wrong.");
        }
        return customerList;
    }
    
    /**
     * populates cities list and sets to cityComboBox
     */
    protected void populateCityList() {
        ObservableList<City> cities = FXCollections.observableArrayList();
        
        try(
                PreparedStatement statement = DBConnection.getConn().prepareStatement("SELECT cityId, city FROM City LIMIT 100;");
                ResultSet rs = statement.executeQuery();){
            while (rs.next()) {
                cities.add(new City(rs.getInt("City.cityId"),rs.getString("City.city")));
            }
        } 
        catch (SQLException sqe) {
            System.out.println("Check Your SQL");
            sqe.printStackTrace();
        } 
        catch (Exception e) {
            System.out.println("Something Besides The SQL Went Wrong.");
        }
    
    //System.out.println(cities);
    cityComboBox.setItems(cities);
    }
    
    /**
     * sets country based on citySelection
     * cities and countries hard coded in to database with no way for user to create their own
     * assuming this business only has customers in cities where offices are located
     * @param citySelection 
     */
    @FXML private void showCountry(String citySelection) {
        if (citySelection.equals("Surrey")) {
            countryField.setText("Canada");
        } 
        else if (citySelection.equals("Philadelphia") || citySelection.equals("Portland") || citySelection.equals("Bowling Green")) {
            countryField.setText("United States of America");
        } 
        else if (citySelection.equals ("Mexico City")) {
            countryField.setText("Mexico");
        }
    }
    
    /**
     * inserts new customer record
     */
    private void saveCustomer() {
            try {
                PreparedStatement ps = DBConnection.getConn().prepareStatement("INSERT INTO Address (address, address2, cityId, postalCode, phone, createDate, createdBy, lastUpdate, lastUpdateBy) "
                        + "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)",Statement.RETURN_GENERATED_KEYS);

                ps.setString(1, addressField.getText());
                ps.setString(2, address2Field.getText());
                ps.setInt(3, cityComboBox.getValue().getCityId());
                ps.setString(4, postalCodeField.getText());
                ps.setString(5, phoneField.getText());
                ps.setString(6, currentUser.getUsername());
                ps.setString(7, currentUser.getUsername());
                boolean res = ps.execute();
                int newAddressId = -1;
                ResultSet rs = ps.getGeneratedKeys();
                
                if(rs.next()){
                    newAddressId = rs.getInt(1);
                    //System.out.println("Generated AddressId: "+ newAddressId);
                }
    
                PreparedStatement psc = DBConnection.getConn().prepareStatement("INSERT INTO Customer "
                + "(customerName, addressId, active, createDate, createdBy, lastUpdate, lastUpdateBy)"
                + "VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)");
            
                psc.setString(1, nameField.getText());
                psc.setInt(2, newAddressId);
                psc.setInt(3, 1);
                psc.setString(4, currentUser.getUsername());
                psc.setString(5, currentUser.getUsername());
                int result = psc.executeUpdate();
            } 
            catch (SQLException ex) {
                ex.printStackTrace();
            }
    }
    
    /**
     * Deletes selected customer from table
     * @param customer 
     */
    private void deleteCustomer(Customer customer) {
        try{           
            PreparedStatement pst = DBConnection.getConn().prepareStatement("DELETE Customer.*, Address.* FROM Customer, Address WHERE Customer.customerId = ? AND Customer.addressId = Address.addressId");
            pst.setString(1, customer.getCustomerId()); 
            pst.executeUpdate();
        } 
        catch(SQLException e){
            e.printStackTrace();
        }       
    }

    /**
     * updates customer records
     */
    private void updateCustomer() {
        try {
            PreparedStatement ps = DBConnection.getConn().prepareStatement("UPDATE Address, Customer, City, Country "
             + "SET address = ?, address2 = ?, Address.cityId = ?, postalCode = ?, phone = ?, Address.lastUpdate = CURRENT_TIMESTAMP, Address.lastUpdateBy = ? "
             + "WHERE Customer.customerId = ? AND Customer.addressId = Address.addressId AND Address.cityId = City.cityId AND City.countryId = Country.countryId");

                ps.setString(1, addressField.getText());
                ps.setString(2, address2Field.getText());
                ps.setInt(3, cityComboBox.getValue().getCityId());
                ps.setString(4, postalCodeField.getText());
                ps.setString(5, phoneField.getText());
                ps.setString(6, currentUser.getUsername());
                ps.setString(7, customerIdField.getText());
                int result = ps.executeUpdate();
                            
                PreparedStatement psc = DBConnection.getConn().prepareStatement("UPDATE Customer, Address, City "
                 + "SET customerName = ?, Customer.lastUpdate = CURRENT_TIMESTAMP, Customer.lastUpdateBy = ? "
                 + "WHERE Customer.customerId = ? AND Customer.addressId = Address.addressId AND Address.cityId = City.cityId");
            
                psc.setString(1, nameField.getText());
                psc.setString(2, currentUser.getUsername());
                psc.setString(3, customerIdField.getText());
                int results = psc.executeUpdate();
        } 
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * validates customer details to prevent invalid data
     * @return true if no errors
     */
    private boolean validateCustomer() {
        String name = nameField.getText();
        String address = addressField.getText();
        City city = cityComboBox.getValue();
        String country = countryField.getText();
        String zip = postalCodeField.getText();
        String phone = phoneField.getText();
        
        String errorMessage = "";
        //first checks to see if inputs are null
        if (name == null || name.length() == 0) {
            errorMessage += "Enter Customer's Name.\n"; 
        }
        if (address == null || address.length() == 0) {
            errorMessage += "Enter An Address.\n";  
        } 
        if (city == null) {
            errorMessage += "Select A City.\n"; 
        } 
        if (country == null || country.length() == 0) {
            errorMessage += "No Valid Country. Country Set By City.\n"; 
        }         
        if (zip == null || zip.length() == 0) {
            errorMessage += "Enter The Postal Code.\n"; 
        } else if (zip.length() > 10 || zip.length() < 5){
            errorMessage += "Enter A valid Postal Code.\n";
        }
        if (phone == null || phone.length() == 0) {
            errorMessage += "Enter A Phone Number (including Area Code)."; 
        } else if (phone.length() < 10 || phone.length() > 15 ){
            errorMessage += "Enter A Valid Phone Number (including Area Code).\n";
        }        
        if (errorMessage.length() == 0) {
            return true;
        } 
        else {
            // Show the error message.
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Correct Invalid Customer fields");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }
}
