package rbac.Managers;

import rbac.Components.Repository;
import rbac.Filters.RoleFilter;
import rbac.Components.Permission;
import rbac.Components.Role;
import rbac.Sorters.RoleSorters;

import java.util.*;
import java.util.regex.Pattern;

public class RoleManager implements Repository<Role> {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Z]+$");
    private static final Pattern RES_PATTERN = Pattern.compile("^[a-z]+$");

    private Map<String, Role> rolesData = new HashMap<>();
    private Map<String, Role> rolesDataName = new HashMap<>();

    @Override
    public void add(Role item) {
        if (item == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        String key = item.getId();
        if (rolesData.containsKey(key)) {
            throw new IllegalArgumentException("Role with name '" + key + "' already create");
        }

        rolesData.put(key, item);
        rolesDataName.put(item.getName(), item);
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
        return res != null;
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
        return rolesData.values().stream().toList().stream().sorted(RoleSorters.byName()).toList();
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
        return rolesData.values().stream().filter(filter::test).toList();
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

        return rolesData.containsKey(name);
    }

    public void addPermissionToRole(String roleName, Permission permission){
        Role data = findByName(roleName).orElse(null);

        if (data == null)
            throw new IllegalArgumentException("Role with name '" + roleName + "' not create");

        if (permission == null)
            return;

        data.addPermission(permission);

        rolesData.put(data.getId(), data);
        rolesDataName.put(roleName, data);
    }

    public void removePermissionFromRole(String roleName, Permission permission){
        Role data = rolesData.get(roleName);

        if (data == null)
            throw new IllegalArgumentException("Role with name '" + roleName + "' not create");

        if (permission == null)
            return;

        data.removePermission(permission);

        rolesData.put(data.getId(), data);
        rolesDataName.put(roleName, data);
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

        List<Role> data = null;
        data = rolesData.values().stream().filter(role -> role.getPermissions().stream().anyMatch(value -> value.matches(permissionName.toUpperCase(), resource.toLowerCase()))).toList().stream().sorted(RoleSorters.byName()).toList();
        return data;
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
