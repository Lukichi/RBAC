package rbac.Components;

import rbac.OtherFunctional.DateUtils;
import rbac.SystemValidation.ValidationUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class TemporaryAssignment extends  AbstractRoleAssignment{

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String expiresAt;
    private boolean autoRenew;
    private boolean active;

    public String getExpiresAt(){
        return expiresAt;
    }
    public Boolean getAutoRenew(){
        return autoRenew;
    }

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata) {
        super(user, role, metadata);
        this.expiresAt = LocalDateTime.now().format(DATE_FORMAT);
        this.autoRenew = false;
        this.active = true;
    }

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata, String expiresAt, boolean autoRenew) {

        super(user, role, metadata);

        ValidationUtils.requireNonEmpty(expiresAt, "ExpiresAt");

        if(!checkDateFormat(expiresAt))
            throw new IllegalArgumentException("ExpiresAt cannot be null or empty");

        this.expiresAt = expiresAt;
        this.autoRenew = autoRenew;
        this.active = true;
    }

    private boolean checkDateFormat(String date){
        if (ValidationUtils.isValidDate(date))
            return true;
        else
            return false;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public boolean isActive(String date) {
        return DateUtils.isAfter(date, expiresAt);
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    public void extend(String newExpirationDate){
        ValidationUtils.requireNonEmpty(newExpirationDate, "ExpiresAt");

        if (!isActive(newExpirationDate)) {
            throw new IllegalArgumentException("New date must be after current date: " + this.expiresAt + " < " + newExpirationDate);
        }

        this.expiresAt = newExpirationDate;
        this.active = true;
    }

    public boolean isExpired(){
        return !isActive();
    }

    private String getTimeRemaining(){
        return DateUtils.formatRelativeTime(expiresAt);
    }

    @Override
    public String summary() {
        String status = isActive() ?  "ACTIVE" : "INACTIVE";
        status = autoRenew ? "ACTIVE (autoRenew = true)" : status;
        return  String.format("[%s] %s assigned to %s by %s at %s\nReason: %s\nStatus: %s\nExpires: %s (%s)",
                assignmentType(), role().getName(), user().username(), metadata().assignedBy(), metadata().assignedAt(), metadata().reason(), status,
                expiresAt, getTimeRemaining());
    }
}
