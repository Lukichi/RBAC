package rbac.Components;

import rbac.OtherFunctional.DateUtils;
import rbac.SystemValidation.ValidationUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AssignmentMetadata{
        ValidationUtils.requireNonEmpty(ValidationUtils.normalizeString(assignedBy), "AssignedBy");
        ValidationUtils.requireNonEmpty(ValidationUtils.normalizeString(assignedAt), "AssignedAt");

    }

    public static AssignmentMetadata now(String assignedBy, String reason){
        ValidationUtils.requireNonEmpty(ValidationUtils.normalizeString(assignedBy), "AssignedBy");

        String time = DateUtils.getCurrentDateTime();
        return new AssignmentMetadata(assignedBy, time, reason);
    }

    public String format(){
        return "Author: " + assignedBy + " | Date: " + assignedAt + " | Reason: " + reason;
    }
}
