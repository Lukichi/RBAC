package rbac.OtherFunctional;

import rbac.Components.Permission;
import rbac.Components.Role;
import rbac.Components.RoleAssignment;
import rbac.Components.User;

import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class ConsoleUtils {

    public static String promptString(Scanner scanner, String message, boolean required) {
        System.out.println(message);

        String result = new String();
        if (required) {
            result = scanner.nextLine();
            while (result.trim().isEmpty()) {
                System.out.println("Значение не может быть пустым: ");
                result = scanner.nextLine();
            }
        }
        else {
            result = scanner.nextLine();
        }

        return result;
    }

    public static int promptInt(Scanner scanner, String message, int min, int max) {
        System.out.println(message);

        int result = scanner.nextInt();
        scanner.nextLine();
        while (result < min || result > max) {
            System.out.println("Введите значение в диапазоне [" + min + "; " + max + "]: ");
            result = scanner.nextInt();
            scanner.nextLine();
        }

        return result;
    }

    public static boolean promptYesNo(Scanner scanner, String message) {
        System.out.println(message);

        String result = scanner.nextLine();
        while (!result.equalsIgnoreCase("yes") && !result.equalsIgnoreCase("no")) {
            System.out.println("Некорректный выбор, введите yes / no:");
            result = scanner.nextLine();
        }

        return result.equalsIgnoreCase("yes");
    }

    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        System.out.println(message);

        for (int i = 0; i < options.size(); i++) {
            T option = options.get(i);
            String optionStr = formatValue(option, i + 1);
            System.out.println(optionStr);
        }

        int num = promptInt(scanner, "Выберите элемент: ", 1, options.size());

        return options.get(num - 1);
    }

    private static <T> String formatValue(T option, int index) {
        StringBuilder sb = new StringBuilder();

        if (option instanceof User user) {
            sb.append(String.format("%-3d) %-20s | %-30s | %s", index, user.username(), user.fullName(), user.email()));

        } else if (option instanceof Role role) {
            Set<Permission> rolePermissions = role.getPermissions();
            int i = 0;
            for (Permission value : rolePermissions) {
                if (i == 0) {
                    sb.append(String.format("%-3d) %-20s | %-30s | %s",  index, role.getName(), role.getDescription(), value.format()));
                }
                else {
                    sb.append(String.format("\n%-3s  %-20s | %-30s | %s", "", "", "", value.format()));
                }
                i++;
            }

        } else if (option instanceof Permission perm) {
            sb.append(String.format("%-3d) %-20s | %-30s | %s", index, perm.name(), perm.resource(), perm.description()));

        } else if (option instanceof RoleAssignment assig) {
            String status = assig.isActive() ?  "ACTIVE" : "INACTIVE";
            sb.append(String.format("%-3d) %-8s | %-10s | %-20s | %-20s | %-20s | %-8s | %s",
                    assig.assignmentType(), assig.role().getName(), assig.user().username(), assig.metadata().assignedBy(),
                    assig.metadata().assignedAt(), status, assig.metadata().reason()));

        } else {
            sb.append(option.toString());
        }
        sb.append("\n" + "-".repeat(100));

        return sb.toString();
    }
}
