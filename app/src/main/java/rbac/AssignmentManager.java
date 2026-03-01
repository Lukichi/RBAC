package rbac;

import java.util.*;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {

    private Map<String, RoleAssignment> assigmentsData = new HashMap<>();

    @Override
    public void add(RoleAssignment item) {
        if (item == null) {
            throw new IllegalArgumentException("RoleAssignment cannot be null");
        }

        String key = item.assignmentId();
        if (assigmentsData.containsKey(key)) {
            throw new IllegalArgumentException("RoleAssignment with name '" + key + "' already create");
        }

        assigmentsData.put(key, item);
    }

    @Override
    public boolean remove(RoleAssignment item) {
        if (item == null) {
            throw new IllegalArgumentException("RoleAssignment cannot be null");
        }

        String key = item.assignmentId();
        RoleAssignment res = assigmentsData.remove(key);
        return res != null;
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(assigmentsData.get(id));
    }

    @Override
    public List<RoleAssignment> findAll() {
        return assigmentsData.values().stream().toList();
    }

    @Override
    public int count() {
        return assigmentsData.size();
    }

    @Override
    public void clear() {
        assigmentsData.clear();
    }

    public List<RoleAssignment> findByUser(User user){
        if (user == null)
            throw new IllegalArgumentException("User cannot be null");

        return assigmentsData.values().stream().filter(assignment -> assignment.user().equals(user)).toList();
    }

    public List<RoleAssignment> findByRole(Role role){
        if (role == null)
            throw new IllegalArgumentException("Role cannot be null");

        return assigmentsData.values().stream().filter(assignment -> assignment.role().equals(role)).toList();
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter){
        return  assigmentsData.values().stream().filter(filter::test).toList();
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter){
        List<RoleAssignment> afterFilter, afterSort;
        if (filter != null)
            afterFilter = assigmentsData.values().stream().filter(filter::test).toList();
        else
            afterFilter = assigmentsData.values().stream().toList();

        if (sorter != null)
            afterSort = afterFilter.stream().sorted(sorter).toList();
        else
            afterSort  = afterFilter;

        return afterSort;
    }

    public List<RoleAssignment> getActiveAssignments(){
        return assigmentsData.values().stream().filter(assignment -> assignment.isActive()).toList().stream().sorted(AssignmentSorters.byUsername()).toList();
    }

    public List<RoleAssignment> getExpiredAssignments(){
        return assigmentsData.values().stream().filter(assignment -> !assignment.isActive()).toList().stream().sorted(AssignmentSorters.byUsername()).toList();
    }

    public boolean userHasRole(User user, Role role){
        if (user == null)
            throw new IllegalArgumentException("User cannot be null");

        List<RoleAssignment> result = assigmentsData.values().stream().filter(assignment -> assignment.user().equals(user)).toList();
        result = result.stream().filter(assignment -> assignment.role().equals(role)).toList();

        return !result.isEmpty();
    }

    public boolean userHasPermission(User user, String permissionName, String resource){
        if (user == null )
            throw new IllegalArgumentException("User cannot be null");
        if (permissionName == null)
            throw new IllegalArgumentException("Permissions name cannot be null");
        if (resource == null)
            throw new IllegalArgumentException("Resource cannot be null");

        List<RoleAssignment> result = assigmentsData.values().stream().filter(assignment -> assignment.user().equals(user)).toList();

        boolean flag = false;
        for (RoleAssignment assignment : result){
            flag = assignment.role().hasPermission(permissionName.toUpperCase(), resource.toLowerCase());
            if (flag)
                break;
        }

        return flag;
    }

    public Set<Permission> getUserPermissions(User user){
        if (user == null) {
            return Collections.emptySet();
        }

        return assigmentsData.values().stream().filter(assignment -> assignment.user().equals(user))
                .flatMap(assignment -> assignment.role().getPermissions().stream()).collect(Collectors.toSet());
    }

    public void revokeAssignment(String assignmentId){
        RoleAssignment assignment = assigmentsData.get(assignmentId);
        if (assignment == null)
            throw new IllegalArgumentException("Assignment with id '" + assignmentId + "' not found");

        if (!(assignment instanceof PermanentAssignment))
            throw new IllegalArgumentException("Only permanent assignments can be revoked");
        PermanentAssignment permAssignment = (PermanentAssignment) assignment;
        permAssignment.revoke();

        assigmentsData.put(assignmentId, permAssignment);
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate){
        RoleAssignment assignment = assigmentsData.get(assignmentId);
        if (assignment == null)
            throw new IllegalArgumentException("Assignment with id '" + assignmentId + "' not found");

        if (!(assignment instanceof TemporaryAssignment))
            throw new IllegalArgumentException("Only temporary assignments can be extended");
        TemporaryAssignment tempAssignment = (TemporaryAssignment) assignment;
        tempAssignment.extend(newExpirationDate);

        assigmentsData.put(assignmentId, tempAssignment);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignmentManager that = (AssignmentManager) o;
        return Objects.equals(assigmentsData, that.assigmentsData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assigmentsData);
    }

}
