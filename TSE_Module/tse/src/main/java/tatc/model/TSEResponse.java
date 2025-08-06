package tatc.model;

/**
 * Response model for TSE (Tradespace Search Executive) operations.
 * This class represents the response structure returned by TSE operations,
 * including success/failure status, error messages, and result information.
 * 
 * @author TSE Development Team
 */
public class TSEResponse {
    private final String workflowId;
    private final boolean success;
    private final String errorMessage;
    private final String resultDirectory;

    /**
     * Private constructor to enforce factory method usage.
     * 
     * @param workflowId The unique identifier for the workflow
     * @param success Whether the operation was successful
     * @param errorMessage Error message if operation failed (null if successful)
     * @param resultDirectory Directory containing results (null if failed)
     */
    private TSEResponse(String workflowId, boolean success, String errorMessage, String resultDirectory) {
        this.workflowId = workflowId;
        this.success = success;
        this.errorMessage = errorMessage;
        this.resultDirectory = resultDirectory;
    }

    /**
     * Creates a successful TSE response.
     * 
     * @param workflowId The unique identifier for the workflow
     * @param resultDirectory Directory containing the operation results
     * @return A TSEResponse representing a successful operation
     */
    public static TSEResponse success(String workflowId, String resultDirectory) {
        return new TSEResponse(workflowId, true, null, resultDirectory);
    }

    /**
     * Creates a failed TSE response.
     * 
     * @param workflowId The unique identifier for the workflow
     * @param errorMessage Description of the error that occurred
     * @return A TSEResponse representing a failed operation
     */
    public static TSEResponse failure(String workflowId, String errorMessage) {
        return new TSEResponse(workflowId, false, errorMessage, null);
    }

    /**
     * Gets the workflow identifier.
     * 
     * @return The unique workflow identifier
     */
    public String getWorkflowId() {
        return workflowId;
    }

    /**
     * Checks if the operation was successful.
     * 
     * @return true if the operation succeeded, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Gets the error message if the operation failed.
     * 
     * @return The error message, or null if the operation was successful
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Gets the result directory path if the operation was successful.
     * 
     * @return The result directory path, or null if the operation failed
     */
    public String getResultDirectory() {
        return resultDirectory;
    }
} 