package com.ayshriv.salescrm.common.resources;

public class Constants {

    // Status Types
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";
    public static final String ERROR = "ERROR";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String CREATED = "CREATED";

    // Message Templates with |ENTITY| placeholder
    public static final String LIST_SUCCESS = "|ENTITY| list fetched successfully.";
    public static final String DETAIL_SUCCESS = "|ENTITY| details fetched successfully.";
    public static final String SAVE_SUCCESS = "|ENTITY| created successfully.";
    public static final String UPDATE_SUCCESS = "|ENTITY| updated successfully.";
    public static final String DELETE_SUCCESS = "|ENTITY| deleted successfully.";

    public static final String LIST_FAILURE = "Failed to fetch |ENTITY| list.";
    public static final String DETAIL_FAILURE = "Failed to fetch |ENTITY| details.";
    public static final String SAVE_FAILURE = "Failed to create |ENTITY|.";
    public static final String UPDATE_FAILURE = "Failed to update |ENTITY|.";
    public static final String DELETE_FAILURE = "Failed to delete |ENTITY|.";

    // Common Error / Validation Messages
    public static final String EXECUTION_ERROR = "An execution error occurred: ";
    public static final String PARAMETER_MISSING = "Required parameter is missing: ";
    public static final String INVALID_TOKEN = "Invalid or expired token.";

    private Constants() {
        // Utility class
    }
}
