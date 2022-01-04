package me.itsmcb.vexelcore.api.modules;

public enum ModuleLoadStatus {

    UNKNOWN("Unknown"),
    SUCCESS("Success"),
    NOT_FOUND("Not Found"),
    UNSUPPORTED_PLATFORM("Unsupported Platform"),
    DEPENDENCY_MISSING("Missing Dependency");

    private String status;

    ModuleLoadStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
