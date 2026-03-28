package rbac.LogSystem;

import rbac.Components.AbstractRoleAssignment;
import rbac.Components.Role;
import rbac.Components.RoleAssignment;
import rbac.Components.User;
import rbac.Filters.AssignmentFilters;
import rbac.Managers.AssignmentManager;
import rbac.Managers.RoleManager;
import rbac.Managers.UserManager;
import rbac.Sorters.AssignmentSorters;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ReportGenerator {

    public static String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        List<User> userList = userManager.findAll();
        StringBuilder report = new StringBuilder();

        for (User value : userList){
            report.append("\n\n").append(value.username()).append(":\n");
            List<RoleAssignment> assignmentList = assignmentManager.findByFilter(AssignmentFilters.byUser(value)).stream().sorted(AssignmentSorters.byRoleName()).toList();
            for(RoleAssignment roleAssignment : assignmentList){
                AbstractRoleAssignment abstractRoleAssignment = (AbstractRoleAssignment) roleAssignment;
                Role role = abstractRoleAssignment.role();
                report.append(role.toString());
            }
            report.append("=".repeat(60)).append("\n");
        }

        return report.toString();
    }

    public static String generateUserReportParallel(UserManager userManager, AssignmentManager assignmentManager) {
        List<User> userList = userManager.findAll();

        return userList.parallelStream().map(user -> {
                    StringBuilder section = new StringBuilder();
                    section.append(user.username()).append(":\n");

                    List<RoleAssignment> assignmentList = assignmentManager.findAll(AssignmentFilters.byUser(user),  AssignmentSorters.byRoleName());
                    for(RoleAssignment roleAssignment : assignmentList){
                        AbstractRoleAssignment abstractRoleAssignment = (AbstractRoleAssignment) roleAssignment;
                        Role role = abstractRoleAssignment.role();
                        section.append(role.toString());
                    }

                    return section.toString();
                }).collect(Collectors.joining("\n\n", "", "\n" + "=".repeat(60) + "\n"));
    }

    public static String generateRoleReport(RoleManager roleManager, AssignmentManager  assignmentManager) {
        List<Role> roleList = roleManager.findAll();
        StringBuilder report = new StringBuilder();

        for (Role value : roleList){
            report.append(value.getName());
            List<RoleAssignment> assignmentList = assignmentManager.findByFilter(AssignmentFilters.byRole(value)).stream().sorted(AssignmentSorters.byUsername()).toList();
            report.append(String.format(" (%d count):\n", assignmentList.size()));
            for(RoleAssignment roleAssignment : assignmentList){
                report.append(String.format("   - %s\n", roleAssignment.user().format()));
            }
            report.append("\n").append("=".repeat(60)).append("\n\n");
        }

        return  report.toString();
    }

    public static String generateRoleReportParallel(RoleManager roleManager, AssignmentManager  assignmentManager) {
        List<Role> roleList = roleManager.findAll();

        return roleList.parallelStream().map(role -> {
            StringBuilder section = new StringBuilder();
            section.append(role.getName()).append("");

            List<RoleAssignment> assignmentList = assignmentManager.findAll(AssignmentFilters.byRole(role),  AssignmentSorters.byRoleName());
            section.append(String.format(" (%d count):\n", assignmentList.size()));
            for(RoleAssignment roleAssignment : assignmentList){
                section.append(String.format("   - %s\n", roleAssignment.user().format()));
            }

            return section.toString();
        }).collect(Collectors.joining("\n\n", "", "\n" + "=".repeat(60) + "\n"));
    }

    public static String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        List<User> userList = userManager.findAll();
        StringBuilder report = new StringBuilder();

        String headre = String.format("%-20s | %-10s | %-10s | %-10s | %-10s |\n", "", "user", "role", "assignment", "report");
        report.append(headre);
        report.append("-".repeat(74) + "\n");
        List<String> resources = List.of("CREATE", "READ", "UPDATE", "DELETE");
        List<String> pilars = List.of("user", "role", "assignment", "report");
        for (User user : userList) {
            report.append(String.format("%-20s |", user.username()));
            for(String pilar : pilars){
                String text = new String();
                for(int i = 0; i < resources.size(); i++) {
                    if (assignmentManager.userHasPermission(user, resources.get(i), pilar)) {
                        switch (i) {
                            case 0: {
                                text += "C";
                                break;
                            }
                            case 1: {
                                text += "R";
                                break;
                            }
                            case 2: {
                                text += "U";
                                break;
                            }
                            case 3: {
                                text += "D";
                                break;
                            }
                        }
                    }
                    else {
                        text += "-";
                    }
                }
                report.append(String.format(" %-10s |", text));
            }
            report.append("\n" + "-".repeat(74) + "\n");
        }

        report.append("C - create\nR - read\nU - update\nD - delete");

        return report.toString();
    }

    public static String generatePermissionMatrixParallel(UserManager userManager, AssignmentManager assignmentManager) {
        List<User> userList = userManager.findAll();
        List<String> resources = List.of("CREATE", "READ", "UPDATE", "DELETE");
        List<String> pilars = List.of("user", "role", "assignment", "report");

        String header = String.format("%-20s | %-10s | %-10s | %-10s | %-10s |\n", "", "user", "role", "assignment", "report") + "-".repeat(74) + "\n";

        String rows = userList.parallelStream()
                .map(user -> {
                    StringBuilder row = new StringBuilder();
                    row.append(String.format("%-20s |", user.username()));

                    for (String pillar : pilars) {
                        StringBuilder permissions = new StringBuilder();
                        for (int i = 0; i < resources.size(); i++) {
                            if (assignmentManager.userHasPermission(user, resources.get(i), pillar)) {
                                switch (i) {
                                    case 0:
                                        permissions.append("C");
                                        break;
                                    case 1:
                                        permissions.append("R");
                                        break;
                                    case 2:
                                        permissions.append("U");
                                        break;
                                    case 3:
                                        permissions.append("D");
                                        break;
                                    default:
                                        permissions.append("-");
                                }
                            } else {
                                permissions.append("-");
                            }
                        }
                        row.append(String.format(" %-10s |", permissions.toString()));
                    }

                    return row.toString();
                })
                .collect(Collectors.joining("\n" + "-".repeat(74) + "\n"));

        String footer = "\nC - create\nR - read\nU - update\nD - delete";

        return header + rows + footer;
    }

    public static void exportToFile(String report, String filename) {
        if (!filename.endsWith(".txt")) {
            filename += ".txt";
        }

        try (FileWriter writer = new FileWriter(filename)){
            writer.write(report);
        } catch (IOException e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }

        System.out.println("Данные успешно сохранены в файл " + filename);
    }
}
