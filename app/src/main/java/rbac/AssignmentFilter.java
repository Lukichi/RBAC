package rbac;

@FunctionalInterface
interface AssignmentFilter {
    boolean test(RoleAssignment assignment);

    default AssignmentFilter and(AssignmentFilter other){
        return role -> this.test(role) && other.test(role);
    }

    default AssignmentFilter or(AssignmentFilter other){
        return role -> this.test(role) || other.test(role);
    }
}
