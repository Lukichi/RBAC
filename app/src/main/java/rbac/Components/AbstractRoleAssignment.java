package rbac.Components;

import java.util.*;

public abstract class AbstractRoleAssignment implements RoleAssignment {

    private String assignmentId;
    private User user;
    private Role role;
    private AssignmentMetadata metadata;

    public AbstractRoleAssignment (User user, Role role, AssignmentMetadata metadata) {
        if (user == null)
            throw new IllegalArgumentException("User cannot be null");
        if (role == null)
            throw new IllegalArgumentException("Role cannot be null");
        if (metadata == null)
            throw new IllegalArgumentException("Metadata cannot be null");

        this.assignmentId = UUID.randomUUID().toString();
        this.user = user;
        this.role = role;
        this.metadata = metadata;
    }

    @Override
    public String assignmentId() {
        return assignmentId;
    }

    @Override
    public User user() {
        return user;
    }

    @Override
    public Role role() {
        return role;
    }

    @Override
    public AssignmentMetadata metadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRoleAssignment that = (AbstractRoleAssignment) o;
        return Objects.equals(assignmentId, that.assignmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentId);
    }

    @Override
    public abstract boolean isActive();

    @Override
    public abstract String assignmentType();

    public String summary() {
        String status = isActive() ?  "ACTIVE" : "INACTIVE";
        return  String.format("[%s] %s assigned to %s by %s at %s\nReason: %s\nStatus: %s\n",
                assignmentType(), role().getName(), user().username(), metadata().assignedBy(), metadata().assignedAt(), metadata().reason(), status);
    }
}
