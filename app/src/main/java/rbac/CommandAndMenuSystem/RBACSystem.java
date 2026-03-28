package rbac.CommandAndMenuSystem;

import rbac.Components.*;
import rbac.Filters.AssignmentFilters;
import rbac.LogSystem.ReportGenerator;
import rbac.Managers.AssignmentManager;
import rbac.Managers.RoleManager;
import rbac.Managers.UserManager;
import rbac.LogSystem.AuditLog;
import rbac.OtherFunctional.DateUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.regex.Pattern;

public class RBACSystem {

    private static final Pattern UN_PATTER = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    private static UserManager userManager = new UserManager();
    private static RoleManager roleManager = new RoleManager();
    private static AssignmentManager assignmentManager = new AssignmentManager();
    private static String currentUser;
    private static AuditLog logSystem = new AuditLog();

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void startExpiredAssignmentsCleaner(long time) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<RoleAssignment> expiredAssignments = assignmentManager.findByFilterParallel(AssignmentFilters.expiringBefore(DateUtils.getCurrentDateTime()));

                if (expiredAssignments.isEmpty()) {
                    logStatistics();
                    return;
                }

                for (RoleAssignment assignment : expiredAssignments) {
                    /*AbstractRoleAssignment value = (AbstractRoleAssignment) assignment;
                    System.out.println(value.summary());*/
                    assignmentManager.deactivateAssignment(assignment.assignmentId());
                }

                logSystem.log("SCHEDULER", "system", "assignments", "Deactivated " + expiredAssignments.size() + " temporary assignments");
                logStatistics();

            } catch (Exception e) {
                logSystem.log("ERROR", "system", "scheduler", "Error in assignments cleaner: " + e.getMessage());
            }
        }, 0, time, TimeUnit.SECONDS);
    }

    private void logStatistics() {
        int totalUsers = userManager.count();
        int totalRoles = roleManager.count();
        int totalAssignments = assignmentManager.count();
        int activeAssignments = assignmentManager.getActiveAssignments().size();
        int expiredAssignments = assignmentManager.getExpiredAssignments().size();

        String stats = String.format( "STATISTICS: Users=%d, Roles=%d, Assignments=%d (Active=%d, InActive=%d)", totalUsers, totalRoles, totalAssignments, activeAssignments, expiredAssignments);

        logSystem.log("STATISTIC", "system", "statistics", stats);
    }

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
    public String getCurrentUser() {
        return currentUser;
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

    public CompletableFuture<String> generateUserReportAsync() {
        return CompletableFuture.supplyAsync(() -> {
            return ReportGenerator.generateUserReport(userManager, assignmentManager);
        }, executor);
    }

    public CompletableFuture<String> generateRoleReportAsync() {
        return CompletableFuture.supplyAsync(() -> {
            return ReportGenerator.generateRoleReport(roleManager, assignmentManager);
        }, executor);
    }

    public CompletableFuture<String> generatePermissionMatrixAsync() {
        return CompletableFuture.supplyAsync(() -> {
            return ReportGenerator.generatePermissionMatrix(userManager, assignmentManager);
        }, executor);
    }

    public CompletableFuture<Void> saveReportAsync(String report, String filename) {
        return CompletableFuture.runAsync(() -> {
            ReportGenerator.exportToFile(report, filename);
        }, executor);
    }

    public CompletableFuture<Void> saveAllData(String filename) {
        return CompletableFuture.runAsync(() -> {
            String correctName = filename;
            if (!correctName.endsWith(".json")) {
                correctName += ".json";
            }

            try (FileWriter writer = new FileWriter(correctName)) {
                writer.write("{\n");
                List<User> userList = RBACSystem.getUserManager().findAll();
                writer.write("  \"users\": [\n");
                for (int i = 0; i < userList.size(); i++) {
                    User user = userList.get(i);

                    writer.write("    {\n");
                    writer.write("      \"username\": \"" + user.username() + "\",\n");
                    writer.write("      \"fullName\": \"" + user.fullName() + "\",\n");
                    writer.write("      \"email\": \"" + user.email() + "\"\n");
                    writer.write("    }");

                    if (i < userList.size() - 1) {
                        writer.write(",");
                    }
                    writer.write("\n");
                }
                writer.write("  ],\n");

                writer.write("  \"roles\": [\n");
                List<Role> roleList = RBACSystem.getRoleManager().findAll();
                for (int i = 0; i < roleList.size(); i++) {
                    Role role = roleList.get(i);

                    writer.write("    {\n");
                    writer.write("      \"name\": \"" + role.getName() + "\",\n");
                    writer.write("      \"description\": \"" + role.getDescription() + "\",\n");
                    writer.write("      \"permissions\": [\n");

                    Set<Permission> permissions = role.getPermissions();
                    int h = 0;
                    for(Permission permission : permissions){
                        writer.write("          {\n");
                        writer.write("            \"name\": \"" + permission.name() + "\",\n");
                        writer.write("            \"resource\": \"" + permission.resource() + "\",\n");
                        writer.write("            \"description\": \"" + permission.description() + "\"\n");
                        writer.write("          }");

                        if (h < permissions.size() - 1) {
                            writer.write(",");
                        }
                        h++;
                        writer.write("\n");
                    }

                    writer.write("      ]\n");
                    writer.write("    }");

                    if (i < roleList.size() - 1) {
                        writer.write(",");
                    }
                    writer.write("\n");
                }
                writer.write("  ],\n");

                writer.write("  \"assignments\": [\n");
                List<RoleAssignment> assignmentList = RBACSystem.getAssignmentManager().findAll();
                for (int i = 0; i < assignmentList.size(); i++) {
                    AbstractRoleAssignment role = (AbstractRoleAssignment) assignmentList.get(i);

                    writer.write("    {\n");
                    writer.write("      \"type\": \"" + role.assignmentType() + "\",\n");

                    if (role.assignmentType() == "PERMANENT"){
                        writer.write("      \"user\": \"" + role.user().username() + "\",\n");
                        writer.write("      \"role\": \"" + role.role().getName() + "\",\n");
                        writer.write("      \"metadata\": \n");
                        writer.write("      {\n");
                        writer.write("        \"assignedBy\": \"" + role.metadata().assignedAt() + "\",\n");
                        writer.write("        \"reason\": \"" + role.metadata().reason() + "\"\n");
                        writer.write("      }\n");
                    }
                    else {
                        TemporaryAssignment temporaryAssignment = (TemporaryAssignment) assignmentList.get(i);
                        writer.write("      \"user\": \"" + role.user().username() + "\",\n");
                        writer.write("      \"role\": \"" + role.role().getName() + "\",\n");
                        writer.write("      \"metadata\": \n");
                        writer.write("      {\n");
                        writer.write("        \"assignedBy\": \"" + role.metadata().assignedAt() + "\",\n");
                        writer.write("        \"reason\": \"" + role.metadata().reason() + "\"\n");
                        writer.write("      },\n");
                        writer.write("      \"expiresAt\": \"" + temporaryAssignment.getExpiresAt() + "\",\n");
                        String text = temporaryAssignment.getAutoRenew()? "true" : "false";
                        writer.write("      \"autoRenew\": \"" + text + "\"\n");
                    }

                    writer.write("    }");
                    if (i < assignmentList.size() - 1) {
                        writer.write(",");
                    }
                    writer.write("\n");
                }
                writer.write("  ]\n");

                writer.write("}\n");

            } catch (IOException e) {
                System.out.println("Ошибка сохранения: " + e.getMessage());
            }
        }, executor);
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }


}
