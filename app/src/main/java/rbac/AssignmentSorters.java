package rbac;

import java.util.Comparator;

public class AssignmentSorters {

    public static Comparator<RoleAssignment> byUsername(){
        return Comparator.comparing(assignment -> assignment.user().username().toLowerCase());
    }

    public static Comparator<RoleAssignment> byRoleName(){
        return Comparator.comparing(assignment -> assignment.role().getName().toLowerCase());
    }

    public static Comparator<RoleAssignment> byAssignmentDate(){
        return Comparator.comparing(assignment -> assignment.metadata().assignedAt());
    }

}
