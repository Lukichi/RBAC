package rbac.Components;

import rbac.SystemValidation.ValidationUtils;

import java.util.*;

public class Role {
    private String id;
    private String name;
    private String description;
    private Set<Permission> permissions;

    public Role (String name, String description){
        this.id = "role_" + UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.permissions = new HashSet<>();
    }

    public Role (String name, String description, Set<Permission> permissions) {
        ValidationUtils.requireNonEmpty(name, "Role name");


        this.id = "role_" + UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.permissions = new HashSet<>(permissions);
    }

    public void addPermission(Permission permission){
        if (permission == null)
            throw new IllegalArgumentException("Permission cannot be null");

        permissions.add(permission);
    }

    public void removePermission(Permission permission){
        if (permission == null)
            throw new IllegalArgumentException("Permission cannot be null");

        permissions.remove(permission);
    }

    public boolean hasPermission(Permission permission){
        return permissions.contains(permission);
    }

    public boolean hasPermission(String permissionName, String resource){
        for (Permission permission : permissions){
            if (permission.name().equalsIgnoreCase(permissionName) && permission.resource().equalsIgnoreCase(resource))
                return true;
        }

        return false;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<Permission> getPermissions(){
        return  Collections.unmodifiableSet(permissions);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String result = "Role: " + name + " [ID: " + id + "]\n" + "Description: " + description + "\nPermissions (" + permissions.stream().count() + ")\n";
        for (Permission value : permissions)
            result += "    - " + value.format() + "\n";

        return  result;
    }

    public String format() {
        return toString();
    }
}
