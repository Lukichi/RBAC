package rbac.Filters;

import rbac.Components.RoleAssignment;

@FunctionalInterface
public interface AssignmentFilter {
    boolean test(RoleAssignment assignment);

    default AssignmentFilter and(AssignmentFilter other){
        return role -> this.test(role) && other.test(role);
    }

    default AssignmentFilter or(AssignmentFilter other){
        return role -> this.test(role) || other.test(role);
    }
}
