package rbac;

import java.util.Locale;

public class RoleFilters {

    public static RoleFilter byName(String name){
        return role -> role.getName().toLowerCase().equals(name.toLowerCase());
    }

    public static RoleFilter byNameContains(String substring){
        return role -> role.getName().toLowerCase().contains(substring.toLowerCase());
    }

    public static RoleFilter hasPermission(Permission permission){
        System.out.println(permission.name());
        return role -> role.getPermissions().stream().anyMatch(value -> value.matches(permission.name().toUpperCase(), permission.resource().toLowerCase() ));
    }

    public static RoleFilter hasPermission(String permissionName, String resource){
        return role -> role.getPermissions().stream().anyMatch(value -> value.matches(permissionName.toUpperCase(), resource.toLowerCase()));
    }

    public static RoleFilter hasAtLeastNPermissions(int n){
        return role -> role.getPermissions().stream().count() >= n;
    }

}
