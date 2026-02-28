package rbac.CommandAndMenuSystem;

import rbac.*;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

public class CommandRegistry {

    private static final Pattern UN_PATTER = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern FN_PATTER = Pattern.compile("^[a-zA-Z\\s]+$");
    private static final Pattern EM_PATTER = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");

    public static void registerCommands(CommandParser parser) {
        parser.registerCommand("user-list", "Вывести всех пользователей", (scanner, system) -> {
            List<User> userList = system.getUserManager().findAll();
            for(User value : userList){
                System.out.println(value.format());
            }
        });

        parser.registerCommand("user-create", "Создать нового пользователя", (scanner, system) -> {
            System.out.print("Введите никнейм: ");
            String username = scanner.nextLine();
            System.out.print("Введите ФИО: ");
            String fullName = scanner.nextLine();
            System.out.print("Введите почту: ");
            String email = scanner.nextLine();

            User newUser = User.validate(username, fullName, email);
            int count1 = RBACSystem.getUserManager().count();
            RBACSystem.getUserManager().add(newUser);
            int count2 = RBACSystem.getUserManager().count();

            if (count1 < count2){
                System.out.println("Пользователь успешно добавлен.");
            }
            else {
                System.out.println("Ошибка добавления пользователя.");
            }
        });

        parser.registerCommand("user-view", "Выводит всю информацию о пользователе", (scanner, system) -> {
            System.out.print("Введите никнейм: ");
            String username = scanner.nextLine();

            Optional<User> optionalUser  = RBACSystem.getUserManager().findByUsername(username);
            User user = optionalUser.orElse(null);

            if (user == null) {
                System.out.println("Пользователь " + username + " не найден");
            }

            List<RoleAssignment> userRoles = RBACSystem.getAssignmentManager().findByUser(user);
            Set<Permission> userPermissions = RBACSystem.getAssignmentManager().getUserPermissions(user);
            System.out.println("Пользователь: " + user.format());
            if (userRoles.isEmpty()){
                System.out.println("Не имеет ролей");
            }
            else {
                System.out.println("Роли: ");
                for (RoleAssignment assignment : userRoles){
                    if (assignment instanceof AbstractRoleAssignment) {
                        AbstractRoleAssignment role = (AbstractRoleAssignment) assignment;
                        System.out.println(role.summary());
                    }
                }
            }

            if (userPermissions.isEmpty()){
                System.out.println("Не имеет прав");
            }
            else {
                System.out.println("Права: ");
                for (Permission per : userPermissions){
                    System.out.println("    " + per.format());
                }
            }
        });

        parser.registerCommand("user-update", "Обновляет данные пользователя", (scanner, system) -> {
            System.out.print("Введите имя пользователя: ");
            String username = scanner.nextLine();
            System.out.print("Введите новое ФИО: ");
            String fullName = scanner.nextLine();
            System.out.print("Введите новую почту: ");
            String email = scanner.nextLine();

            Optional<User> optionalUser = RBACSystem.getUserManager().findByUsername(username);
            User user = optionalUser.orElse(null);

            if (user == null) {
                System.out.println("Пользователь " + username + " не найден.");
            }
            else {
                User.validate(username, fullName, email);
                RBACSystem.getUserManager().update(username, fullName, email);
            }
        });

        parser.registerCommand("user-delete", "Удаляет пользователя", (scanner, system) -> {
            System.out.print("Введите имя пользователя: ");
            String username = scanner.nextLine();

            Optional<User> optionalUser = RBACSystem.getUserManager().findByUsername(username);
            User user = optionalUser.orElse(null);

            if (user == null) {
                System.out.println("Пользователь " + username + " не найден.");
            }
            else {
                RBACSystem.getUserManager().remove(user);
            }
        });

        parser.registerCommand("user-search", "Поиск пользователей по фильтрам", (scanner, system) -> {
            System.out.println("Выберите тип фильтрации:\n" +
                    "   1) По username (содержит)\n" +
                    "   2) По email (содержит)\n" +
                    "   3) По домену email\n" +
                    "   4) По полному имени (содержит)\n");

            int key = scanner.nextInt();
            scanner.nextLine();
            switch (key){
                case 1:{
                    System.out.println("Вверите username");
                    String username = scanner.nextLine();

                    Optional<User> optionalUser  = RBACSystem.getUserManager().findByUsername(username);
                    User user = optionalUser.orElse(null);

                    if (user == null) {
                        System.out.println("Пользователь " + username + " не найден");
                    }

                    List<RoleAssignment> userRoles = RBACSystem.getAssignmentManager().findByUser(user);
                    Set<Permission> userPermissions = RBACSystem.getAssignmentManager().getUserPermissions(user);
                    System.out.println("Пользователь: " + user.format());
                    if (userRoles.isEmpty()){
                        System.out.println("Не имеет ролей");
                    }
                    else {
                        System.out.println("Роли: ");
                        for (RoleAssignment assignment : userRoles){
                            if (assignment instanceof AbstractRoleAssignment) {
                                AbstractRoleAssignment role = (AbstractRoleAssignment) assignment;
                                System.out.println(role.summary());
                            }
                        }
                    }

                    if (userPermissions.isEmpty()){
                        System.out.println("Не имеет прав");
                    }
                    else {
                        System.out.println("Права: ");
                        for (Permission per : userPermissions){
                            System.out.println("    " + per.format());
                        }
                    }
                    break;
                }
                case 2:{
                    System.out.println("Вверите email");
                    String email = scanner.nextLine();

                    Optional<User> optionalUser  = RBACSystem.getUserManager().findByEmail(email);
                    User user = optionalUser.orElse(null);

                    if (user == null) {
                        System.out.println("Пользователь с такой почтой " + email + " не найден");
                    }

                    List<RoleAssignment> userRoles = RBACSystem.getAssignmentManager().findByUser(user);
                    Set<Permission> userPermissions = RBACSystem.getAssignmentManager().getUserPermissions(user);
                    System.out.println("Пользователь: " + user.format());
                    if (userRoles.isEmpty()){
                        System.out.println("Не имеет ролей");
                    }
                    else {
                        System.out.println("Роли: ");
                        for (RoleAssignment assignment : userRoles){
                            if (assignment instanceof AbstractRoleAssignment) {
                                AbstractRoleAssignment role = (AbstractRoleAssignment) assignment;
                                System.out.println(role.summary());
                            }
                        }
                    }

                    if (userPermissions.isEmpty()){
                        System.out.println("Не имеет прав");
                    }
                    else {
                        System.out.println("Права: ");
                        for (Permission per : userPermissions){
                            System.out.println("    " + per.format());
                        }
                    }
                    break;
                }
                case 3:{
                    System.out.println("Вверите домен");
                    String domen = scanner.nextLine();

                    List<User> userList  = RBACSystem.getUserManager().findByFilter(UserFilters.byEmailDomain(domen));
                    if (userList.isEmpty()){
                        System.out.println("Пользователи с доменом " + domen + " не найдены");
                        return;
                    }

                    for (User value : userList){
                        List<RoleAssignment> userRoles = RBACSystem.getAssignmentManager().findByUser(value);
                        Set<Permission> userPermissions = RBACSystem.getAssignmentManager().getUserPermissions(value);
                        System.out.println("Пользователь: " + value.format());
                        if (userRoles.isEmpty()){
                            System.out.println("Не имеет ролей");
                        }
                        else {
                            System.out.println("Роли: ");
                            for (RoleAssignment assignment : userRoles){
                                if (assignment instanceof AbstractRoleAssignment) {
                                    AbstractRoleAssignment role = (AbstractRoleAssignment) assignment;
                                    System.out.println(role.summary());
                                }
                            }
                        }

                        if (userPermissions.isEmpty()){
                            System.out.println("Не имеет прав");
                        }
                        else {
                            System.out.println("Права: ");
                            for (Permission per : userPermissions){
                                System.out.println("    " + per.format());
                            }
                        }
                    }
                    break;
                }
                case 4:{
                    System.out.println("Вверите fullName");
                    String fullName = scanner.nextLine();

                    List<User> userList  = RBACSystem.getUserManager().findByFilter(UserFilters.byFullNameContains(fullName));
                    if (userList.isEmpty()){
                        System.out.println("Пользователи с полным именем " + fullName + " не найдены");
                        return;
                    }

                    for (User value : userList){
                        List<RoleAssignment> userRoles = RBACSystem.getAssignmentManager().findByUser(value);
                        Set<Permission> userPermissions = RBACSystem.getAssignmentManager().getUserPermissions(value);
                        System.out.println("Пользователь: " + value.format());
                        if (userRoles.isEmpty()){
                            System.out.println("Не имеет ролей");
                        }
                        else {
                            System.out.println("Роли: ");
                            for (RoleAssignment assignment : userRoles){
                                if (assignment instanceof AbstractRoleAssignment) {
                                    AbstractRoleAssignment role = (AbstractRoleAssignment) assignment;
                                    System.out.println(role.summary());
                                }
                            }
                        }

                        if (userPermissions.isEmpty()){
                            System.out.println("Не имеет прав");
                        }
                        else {
                            System.out.println("Права: ");
                            for (Permission per : userPermissions){
                                System.out.println("    " + per.format());
                            }
                        }
                    }
                    break;
                }
                default:{
                    System.out.println("По выбранному типу нет фильтра, повторите попытку.");
                    break;
                }
            }
        });
    }
}
