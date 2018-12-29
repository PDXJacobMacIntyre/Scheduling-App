package JMUtilities;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @author jmacin1 / Jacob MacIntyre
 */
public class DBConnection {
    private static Connection connDB;
    public DBConnection(){}
    
    /**
     * opens db
     * db connection info:
     * Server name: 52.206.157.109
     * Database name: U04Esb
     * Username: U04Esb
     * Password: 53688216525
     */
    public static void init(){
        System.out.println("Connecting to the database");
        try{
            Class.forName("com.mysql.jdbc.Driver");
            connDB = DriverManager.getConnection("jdbc:mysql://52.206.157.109:3306/U04cuX", "U04cuX", "53688204536");
        }
        catch (ClassNotFoundException ce){
            System.out.println("Unable To Locate Correct Class. Add The MySQL Library To The Run Configuration.");
            ce.printStackTrace();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    //returns connection
    public static Connection getConn(){
        return connDB;
    }
    
    //closes connections
    public static void closeConn(){
        try{
            connDB.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally{
            System.out.println("Connection Closed.");
        }
    }
}
