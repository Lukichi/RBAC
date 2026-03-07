package rbac.CommandAndMenuSystem;

import rbac.Components.*;
import rbac.Managers.AssignmentManager;
import rbac.Managers.RoleManager;
import rbac.Managers.UserManager;
import rbac.LogSystem.AuditLog;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class RBACSystem {

    private static final Pattern UN_PATTER = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    private static UserManager userManager = new UserManager();
    private static RoleManager roleManager = new RoleManager();
    private static AssignmentManager assignmentManager = new AssignmentManager();
    private static String currentUser;
    private static AuditLog logSystem = new AuditLog();

    public static UserManager getUserManager() {
        return userManager;
    }

    public static RoleManager getRoleManager() {
        return roleManager;
    }

    public static AuditLog getLogSystem(){return logSystem;}

    public static AssignmentManager getAssignmentManager() {
        return assignmentManager;
    }

    public void setCurrentUser(String username){
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username not be empty");
        }
        if (!UN_PATTER.matcher((username)).matches())
            throw new IllegalArgumentException("Invalid format username. Username must not contain special characters");

        Optional<User> optionalUser = RBACSystem.getUserManager().findByUsername(username);
        User user = optionalUser.orElse(null);
        if (user == null){
            throw new IllegalArgumentException("User witch username *" + username + "* not found");
        }
        currentUser = username;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void initialize() {
        User userAdmin = new User("admin", "Chief Admin", "admin@mail.ru");
        userManager.add(userAdmin);
        logSystem.log("create", "system", "user", "Initial user admin");

        Set<Permission> permissionsAdmin = new HashSet<>();
        permissionsAdmin.add(new Permission("READ", "report", "Read report"));
        permissionsAdmin.add(new Permission("WRITE", "report", "Write report"));
        permissionsAdmin.add(new Permission("DELETE", "report", "Delete report"));
        permissionsAdmin.add(new Permission("READ", "users", "Read users"));
        permissionsAdmin.add(new Permission("Delete", "users", "Delete users"));

        Set<Permission> permissionsManager = new HashSet<>();
        permissionsManager.add(new Permission("READ", "report", "Read report"));
        permissionsManager.add(new Permission("WRITE", "report", "Write report"));

        Set<Permission> permissionsViwer = new HashSet<>();
        permissionsViwer.add(new Permission("READ", "users", "Read users"));

        Role roleAdmin = new Role("admin", "Chief admin", permissionsAdmin);
        logSystem.log("create", "system", "role", "Initial role admin");
        Role roleManage = new Role("manager", "Manage reports", permissionsManager);
        logSystem.log("create", "system", "role", "Initial role manager");
        Role roleViewer = new Role("viewer", "View users", permissionsViwer);
        logSystem.log("create", "system", "role", "Initial role viewer");
        roleManager.add(roleAdmin);
        roleManager.add(roleManage);
        roleManager.add(roleViewer);

        AssignmentMetadata metadataAdmin = AssignmentMetadata.now("admin", "Initial data");
        PermanentAssignment assignmentAdmin = new PermanentAssignment(userAdmin, roleAdmin, metadataAdmin);
        assignmentManager.add(assignmentAdmin);
        logSystem.log("create", "system", "assignment", "Initial assignment admin role for admin");
    }

    public static String generateStatistics(){
        int countUsers = userManager.count();
        int countRoles = userManager.count();
        int countAssigments = userManager.count();

        return "Count users: " + countUsers + "\nCount roles: " + countRoles +"\nCount assignments: " + countAssigments;
    }
}
