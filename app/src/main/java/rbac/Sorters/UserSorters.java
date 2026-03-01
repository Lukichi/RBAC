package rbac.Sorters;

import rbac.Components.User;

import java.util.Comparator;

public class UserSorters {
    public static Comparator<User> byUsername(){
        return Comparator.comparing(User::username, String.CASE_INSENSITIVE_ORDER);
    }

    public static Comparator<User> byFullName(){
        return Comparator.comparing(User::fullName, String.CASE_INSENSITIVE_ORDER);
    }

    public static Comparator<User> byEmail(){
        return Comparator.comparing(User::email, String.CASE_INSENSITIVE_ORDER);
    }

}
