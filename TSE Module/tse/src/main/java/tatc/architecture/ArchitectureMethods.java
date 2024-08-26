package tatc.architecture;

import java.io.File;

/**
 * Interface that needs to be implemented by any class that generates an architecture JSON file
 */
public interface ArchitectureMethods {

    /**
     * Creates an architecture JSON file
     * @param counter the architecture number
     * @return the created JSON file object
     */
    File toJSON(int counter);
}
