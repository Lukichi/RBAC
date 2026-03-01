package rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class TemporaryAssignment extends  AbstractRoleAssignment{

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy MM dd HH:mm:ss");

    private String expiresAt;
    private boolean autoRenew;

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
    }

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata, String expiresAt, boolean autoRenew) {

        super(user, role, metadata);

        if (expiresAt == null || expiresAt.isEmpty()) {
            throw new IllegalArgumentException("ExpiresAt cannot be null or empty");
        }

        if(!checkDateFormat(expiresAt))
            throw new IllegalArgumentException("ExpiresAt cannot be null or empty");

        this.expiresAt = expiresAt;
        this.autoRenew = autoRenew;
    }

    private boolean checkDateFormat(String date){
        try {
            LocalDateTime.parse(date, DATE_FORMAT);
            return true;
        } catch (DateTimeParseException e1) {
            return false;
        }
    }

    @Override
    public boolean isActive() {
        LocalDateTime nowDate = LocalDateTime.now();
        LocalDateTime thisDate = LocalDateTime.parse(this.expiresAt, DATE_FORMAT);

        return nowDate.isBefore(thisDate);
    }

    public boolean isActive(String date) {
        LocalDateTime nowDate = LocalDateTime.parse(date, DATE_FORMAT);
        LocalDateTime thisDate = LocalDateTime.parse(this.expiresAt, DATE_FORMAT);

        return nowDate.isBefore(thisDate);
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    public void extend(String newExpirationDate){
        if (newExpirationDate == null || newExpirationDate.isEmpty())
            throw new IllegalArgumentException("ExpiresAt cannot be null or empty");

        if (isActive(newExpirationDate))
            throw new IllegalArgumentException("New date must be after current date: " + this.expiresAt + " | " + newExpirationDate);

        this.expiresAt = newExpirationDate;
    }

    public boolean isExpired(){
        return !isActive();
    }

    private String getTimeRemaining(){
        LocalDateTime nowDate = LocalDateTime.now();
        LocalDateTime thisDate = LocalDateTime.parse(this.expiresAt, DATE_FORMAT);

        long days = Math.abs(ChronoUnit.DAYS.between(nowDate, thisDate));

        if (isActive())
            return "through " + days + " days";

        return days + " days ago";
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
