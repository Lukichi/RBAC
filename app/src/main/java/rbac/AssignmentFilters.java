package rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AssignmentFilters {

    public static AssignmentFilter byUser(User user){
        return assignment -> assignment.user().equals(user);
    }

    public static AssignmentFilter byUsername(String username){
        return assignment -> assignment.user().username().equals(username);
    }

    public static AssignmentFilter byRole(Role role){
        return assignment -> assignment.role().equals(role);
    }

    public static AssignmentFilter byRoleName(String roleName){
        return assignment -> assignment.role().getName().toLowerCase().equals(roleName.toLowerCase());
    }

    public static AssignmentFilter activeOnly() {
        return assignment -> assignment.isActive();
    }

    public static AssignmentFilter inactiveOnly() {
        return assignment -> !assignment.isActive();
    }

    public static AssignmentFilter byType(String type){
        return assignment -> assignment.assignmentType().toUpperCase().equals(type.toUpperCase());
    }

    public static AssignmentFilter assignedBy(String username){
        return assignment -> assignment.metadata().assignedBy().toLowerCase().equals(username.toLowerCase());
    }

    public static AssignmentFilter assignedAfter(String date) {
        return assignment -> {
            String assignedAt = assignment.metadata().assignedAt();
            DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy MM dd HH:mm:ss");
            LocalDateTime filterDate = LocalDateTime.parse(date, DATE_FORMAT);
            LocalDateTime assignedDate = LocalDateTime.parse(assignedAt, DATE_FORMAT);

//            System.out.println("DATA TIME: " + assignedAt);
//            System.out.println("    filter TIME: " + date);

            return assignedDate.isAfter(filterDate);
        };
    }

    public static AssignmentFilter expiringBefore(String date) {
        return assignment -> {
            String assignedAt = assignment.metadata().assignedAt();
            DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy MM dd HH:mm:ss");
            LocalDateTime filterDate = LocalDateTime.parse(date, DATE_FORMAT);
            LocalDateTime assignedDate = LocalDateTime.parse(assignedAt, DATE_FORMAT);

//            System.out.println("DATA TIME: " + assignedAt);
//            System.out.println("    filter TIME: " + date);

            return assignedDate.isBefore(filterDate) && assignment.assignmentType().equals("TEMPORARY");
        };
    }

}
