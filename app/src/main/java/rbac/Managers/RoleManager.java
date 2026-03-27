package rbac.Managers;

import rbac.Components.Repository;
import rbac.Components.RoleAssignment;
import rbac.Filters.RoleFilter;
import rbac.Components.Permission;
import rbac.Components.Role;
import rbac.Sorters.RoleSorters;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class RoleManager implements Repository<Role> {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Z]+$");
    private static final Pattern RES_PATTERN = Pattern.compile("^[a-z]+$");

    private Map<String, Role> rolesData = new ConcurrentHashMap<>();
    private Map<String, Role> rolesDataName = new ConcurrentHashMap<>();

    @Override
    public void add(Role item) {
        if (item == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        String key = item.getId();
        if (rolesData.containsKey(key)) {
            throw new IllegalArgumentException("Role with name '" + key + "' already create");
        }

        Role previous = rolesData.putIfAbsent(key, item);
        if (previous != null) {
            throw new IllegalArgumentException("Role with name '" + key + "' already create");
        }


        Role previous2 = rolesDataName.putIfAbsent(item.getName(), item);
        if (previous2 != null) {
            rolesData.remove(key);
            throw new IllegalArgumentException("Role with name '" + key + "' already create");
        }
    }

    @Override
    public boolean remove(Role item) {
        if (item == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        String key = item.getId();
        String key2 = item.getName();
        Role res = rolesData.remove(key);
        Role res2 = rolesDataName.remove(key2);

        return res != null || res2 != null;
    }

    @Override
    public Optional<Role> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(rolesData.get(id));
    }

    @Override
    public List<Role> findAll() {
        return rolesData.values().stream().sorted(RoleSorters.byName()).toList();
    }

    @Override
    public int count() {
        return rolesData.size();
    }

    @Override
    public void clear() {
        rolesData.clear();
    }

    public Optional<Role> findByName(String name){
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(rolesDataName.get(name));
    }

    public List<Role> findByFilter(RoleFilter filter){
        List<Role> helpList = new ArrayList<>( rolesData.values());
        return helpList.stream().filter(filter::test).toList();
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter){
        List<Role> afterFilter, afterSort;
        if (filter != null)
            afterFilter = rolesData.values().stream().filter(filter::test).toList();
        else
            afterFilter = rolesData.values().stream().toList();

        if (sorter != null)
            afterSort = afterFilter.stream().sorted(sorter).toList();
        else
            afterSort  = afterFilter;

        return afterSort;
    }

    public boolean exists(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        return rolesDataName.containsKey(name);
    }

    public void addPermissionToRole(String roleName, Permission permission){
        if (permission == null)
            return;

        Role updated = rolesDataName.compute(roleName, (id, role) -> {
            if (role == null) {
                throw new IllegalArgumentException("Role with name '" + roleName + "' not found");
            }
            role.addPermission(permission);
            return role;
        });

        rolesData.put(roleName, updated);
    }

    public void removePermissionFromRole(String roleName, Permission permission){
        if (permission == null)
            return;

        Role updated = rolesDataName.compute(roleName, (id, role) -> {
            if (role == null) {
                throw new IllegalArgumentException("Role with name '" + roleName + "' not found");
            }
            role.removePermission(permission);
            return role;
        });

        rolesData.put(roleName, updated);
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource){
        if (permissionName == null || permissionName.trim().isEmpty() ||
                resource == null || resource.trim().isEmpty()) {
            return Collections.emptyList();
        }

        if (!NAME_PATTERN.matcher(permissionName.toUpperCase()).matches())
            throw new IllegalArgumentException("Invalid name format. Name must contain only letters and not be empty");
        if (!RES_PATTERN.matcher(resource.toLowerCase()).matches())
            throw new IllegalArgumentException("Invalid resource format. resource must contain only letters");

        List<Role> helpList = new ArrayList<>( rolesData.values());
        return helpList.stream().filter(role -> role.getPermissions().stream().anyMatch(value -> value.matches(permissionName.toUpperCase(), resource.toLowerCase()))).toList().stream().sorted(RoleSorters.byName()).toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleManager that = (RoleManager) o;
        return Objects.equals(rolesData, that.rolesData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rolesData);
    }

}
