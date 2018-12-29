package JMUtilities;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * initializes Logger class called in main method
 * file handler is set to create a log file in the base of the project folder
 * if file exists, it will append logs to that file unless file is being used... 
 * ...by another process or has reached the set size limit.
 * if file in use or at size limit, it will create a new file.
 * there is a file count limit of 10 then will overwrite first file.
 * 
 * @author jmacin1
 */
public class LoggerUtil {
 private final static Logger LOGGER = Logger.getLogger(LoggerUtil.class.getName());
 private static FileHandler handler = null;
 
 public static void init(){
    try {
    handler = new FileHandler("JDMScheduling-Userlog.%u.%g.txt", 1024 * 1024, 10, true);
    } 
    catch (SecurityException | IOException e) {
        e.printStackTrace();
        }
    Logger logger = Logger.getLogger("");
    handler.setFormatter(new SimpleFormatter());
    logger.addHandler(handler);
    logger.setLevel(Level.INFO);
 }
}
