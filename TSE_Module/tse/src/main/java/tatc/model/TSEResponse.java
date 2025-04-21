package tatc.model;

public class TSEResponse {
    private String workflowId;
    private boolean success;
    private String errorMessage;
    private String resultDirectory;

    private TSEResponse(String workflowId, boolean success, String errorMessage, String resultDirectory) {
        this.workflowId = workflowId;
        this.success = success;
        this.errorMessage = errorMessage;
        this.resultDirectory = resultDirectory;
    }

    public static TSEResponse success(String workflowId, String resultDirectory) {
        return new TSEResponse(workflowId, true, null, resultDirectory);
    }

    public static TSEResponse failure(String workflowId, String errorMessage) {
        return new TSEResponse(workflowId, false, errorMessage, null);
    }

    // Getters
    public String getWorkflowId() {
        return workflowId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getResultDirectory() {
        return resultDirectory;
    }
} 