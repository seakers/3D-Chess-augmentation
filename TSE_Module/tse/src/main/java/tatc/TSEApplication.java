package tatc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot application entry point for the TSE (Tradespace Search Executive) system.
 * This class serves as the main application class for the Spring Boot framework,
 * enabling auto-configuration and component scanning for the TSE application.
 * 
 * @author TSE Development Team
 */
@SpringBootApplication
public class TSEApplication {
    
    /**
     * Main method that starts the Spring Boot application.
     * 
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(TSEApplication.class, args);
    }
} 