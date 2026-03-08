package rbac.CommandAndMenuSystem;

import rbac.Components.*;
import rbac.Filters.AssignmentFilters;
import rbac.Filters.RoleFilters;
import rbac.Filters.UserFilters;
import rbac.LogSystem.ReportGenerator;
import rbac.OtherFunctional.ConsoleUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.System.*;

public class CommandRegistry {

    public static final String RED = "\u001B[31m";
    public static final String WHITE = "\u001B[37m";
    public static final String RESET = "\u001B[0m";
    public static final String GREEN = "\u001B[32m";

    public static final String BOLD = "\u001B[1m";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void registerCommands(CommandParser parser) {
        parser.registerCommand("user-list", "Вывести всех пользователей", (scanner, system) -> {
            int type = ConsoleUtils.promptInt(scanner, "Вывести всех пользователей (1) или использовать параметры (0):", 0, 1);
            switch (type) {
                case 1: {
                    System.out.println(String.format(BOLD + RED + "%-20s | %-40s | %-30s", "username", "full name", "email" + RESET));
                    System.out.println("-".repeat(100));
                    List<User> userList = system.getUserManager().findAll();
                    for(User value : userList){
                        System.out.println(String.format("%-20s | %-40s | %-30s", value.username(), value.fullName(), value.email()));
                        System.out.println("-".repeat(100));
                    }
                    break;
                }
                case 0: {
                    parser.parseAndExecute("user-search", scanner, system);
                    break;
                }
                default:{
                    System.out.println("Ошибка выбора");
                    break;
                }
            }

        });

        parser.registerCommand("user-create", "Создать нового пользователя", (scanner, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Введите имя пользователя: ", true);
            String fullName = ConsoleUtils.promptString(scanner, "Введите ФИО пользователя: ", true);
            String email = ConsoleUtils.promptString(scanner, "Введите почту пользователя: ", true);

            User newUser = User.validate(username, fullName, email);
            int count1 = RBACSystem.getUserManager().count();
            RBACSystem.getUserManager().add(newUser);
            int count2 = RBACSystem.getUserManager().count();

            RBACSystem.getLogSystem().log("create", system.getCurrentUser(), "user", "User " +  system.getCurrentUser() + " create new user " + username);

            if (count1 < count2){
                System.out.println(GREEN + BOLD + "Пользователь успешно добавлен." + RESET);
            }
            else {
                System.out.println(RED + BOLD + "Ошибка добавления пользователя." + RESET);
            }
        });

        parser.registerCommand("user-view", "Выводит всю информацию о пользователе", (scanner, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Введите имя пользователя: ", true);

            Optional<User> optionalUser  = RBACSystem.getUserManager().findByUsername(username);
            User user = optionalUser.orElse(null);

            if (user == null) {
                System.out.println(RED + BOLD + "Пользователь " + username + " не найден" + RESET);
                return;
            }

            List<RoleAssignment> userRoles = RBACSystem.getAssignmentManager().findByUser(user);
            Set<Permission> userPermissions = RBACSystem.getAssignmentManager().getUserPermissions(user);
            System.out.println("Пользователь: " + user.format());
            if (userRoles.isEmpty()){
                System.out.println("Не имеет ролей");
            }
            else {
                System.out.println(RED + BOLD + "Роли: " + RESET);
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
                System.out.println(RED + BOLD + "Права: " + RESET);
                for (Permission per : userPermissions){
                    System.out.println("    " + per.format());
                }
            }
        });

        parser.registerCommand("user-update", "Обновляет данные пользователя", (scanner, system) -> {
            String username =ConsoleUtils.promptString(scanner, "Введите имя пользователя: ", true);
            String fullName = ConsoleUtils.promptString(scanner, "Введите новое ФИО: ", true);
            String email = ConsoleUtils.promptString(scanner, "Введите новую почту: ", true);

            Optional<User> optionalUser = RBACSystem.getUserManager().findByUsername(username);
            User user = optionalUser.orElse(null);

            if (user == null) {
                System.out.println("Пользователь " + username + " не найден.");
                return;
            }
            else {
                User.validate(username, fullName, email);
                RBACSystem.getUserManager().update(username, fullName, email);
                RBACSystem.getLogSystem().log("update", system.getCurrentUser(), "user", "User " +  system.getCurrentUser() + " update data user " + username);
            }
        });

        parser.registerCommand("user-delete", "Удаляет пользователя", (scanner, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Введите имя пользователя: ", true);

            Optional<User> optionalUser = RBACSystem.getUserManager().findByUsername(username);
            User user = optionalUser.orElse(null);

            if (user == null) {
                System.out.println("Пользователь " + username + " не найден.");
                return;
            }

            boolean flag = ConsoleUtils.promptYesNo(scanner, "Введите yes для подтверждения удаления: ");

            if (!flag){
                System.out.println(RED + BOLD + "Удаление отменено" + RESET);
                return;
            }

            List<RoleAssignment> assignmentList = RBACSystem.getAssignmentManager().findByFilter(AssignmentFilters.byUser(user));
            List<RoleAssignment> assignmentList2 = RBACSystem.getAssignmentManager().findByFilter(AssignmentFilters.assignedBy(user.username()));

            for (RoleAssignment value : assignmentList2){
                RBACSystem.getAssignmentManager().remove(value);
            }
            for (RoleAssignment value : assignmentList){
                if (assignmentList2.contains(value))
                    continue;

                RBACSystem.getAssignmentManager().remove(value);
            }

            RBACSystem.getUserManager().remove(user);
            out.println(GREEN + BOLD + "Пользователь успешно удален" + RESET);
            RBACSystem.getLogSystem().log("delete", system.getCurrentUser(), "user", "User " +  system.getCurrentUser() + " delete user " + username);
        });

        parser.registerCommand("user-search", "Поиск пользователей по фильтрам", (scanner, system) -> {
            String text = "Выберите тип фильтрации:\n" +
                    "   1) По username (содержит)\n" +
                    "   2) По email (содержит)\n" +
                    "   3) По домену email\n" +
                    "   4) По полному имени (содержит)\n";

            int key = ConsoleUtils.promptInt(scanner, text, 1, 4);
            switch (key){
                case 1:{
                    String username = ConsoleUtils.promptString(scanner, "Введите имя пользователя: ", true);

                    List<User> userList  = RBACSystem.getUserManager().findByFilter(UserFilters.byUsernameContains(username));

                    if (userList == null || userList.isEmpty()) {
                        System.out.println("Пользователь " + username + " не найден");
                        return;
                    }

                    System.out.println(String.format(RED + BOLD + "%-20s | %-40s | %-30s", "username", "full name", "email" + RESET));
                    System.out.println("-".repeat(100));

                    for(User value : userList){
                        System.out.println(String.format("%-20s | %-40s | %-30s", value.username(), value.fullName(), value.email()));
                        System.out.println("-".repeat(100));
                    }
                    break;
                }
                case 2:{
                    String email = ConsoleUtils.promptString(scanner, "Введите почту: ", true);

                    List<User> userList  = RBACSystem.getUserManager().findByFilter(UserFilters.byEmail(email));

                    if (userList == null || userList.isEmpty()) {
                        System.out.println("Пользователь с почтой " + email + " не найден");
                        return;
                    }

                    System.out.println(String.format(RED + BOLD + "%-20s | %-40s | %-30s", "username", "full name", "email" + RESET));
                    System.out.println("-".repeat(100));

                    for(User value : userList){
                        System.out.println(String.format("%-20s | %-40s | %-30s", value.username(), value.fullName(), value.email()));
                        System.out.println("-".repeat(100));
                    }
                    break;
                }
                case 3:{
                    String domen = ConsoleUtils.promptString(scanner, "Введите домен: ", true);

                    List<User> userList  = RBACSystem.getUserManager().findByFilter(UserFilters.byEmailDomain(domen));
                    if (userList == null || userList.isEmpty()) {
                        System.out.println("Пользователь с доменом " + domen + " не найден");
                        return;
                    }

                    System.out.println(String.format(RED + BOLD + "%-20s | %-40s | %-30s", "username", "full name", "email" + RESET));
                    System.out.println("-".repeat(100));

                    for(User value : userList){
                        System.out.println(String.format("%-20s | %-40s | %-30s", value.username(), value.fullName(), value.email()));
                        System.out.println("-".repeat(100));
                    }
                    break;
                }
                case 4:{
                    String fullName = ConsoleUtils.promptString(scanner, "Введите ФИО: ", true);

                    List<User> userList  = RBACSystem.getUserManager().findByFilter(UserFilters.byFullNameContains(fullName));
                    if (userList == null || userList.isEmpty()) {
                        System.out.println("Пользователь с именем " + fullName + " не найден");
                        return;
                    }

                    System.out.println(String.format(RED + BOLD + "%-20s | %-40s | %-30s", "username", "full name", "email" + RESET));
                    System.out.println("-".repeat(100));

                    for(User value : userList){
                        System.out.println(String.format("%-20s | %-40s | %-30s", value.username(), value.fullName(), value.email()));
                        System.out.println("-".repeat(100));
                    }
                    break;
                }
                default:{
                    System.out.println("По выбранному типу нет фильтра, повторите попытку.");
                    break;
                }
            }
        });

        parser.registerCommand("role-list", "Выводит список всех ролей", (scanner, system) -> {
            List<Role> roleList = RBACSystem.getRoleManager().findAll();

            for (Role role : roleList) {
                System.out.println(role.toString());
                System.out.println(RED + BOLD + "-".repeat(100) + RESET);
            }
        });

        parser.registerCommand("role-create", "Создание новой роли", (scanner, system) -> {
            String roleName = ConsoleUtils.promptString(scanner, "Введите имя роли: ", true);
            String roleDescription = ConsoleUtils.promptString(scanner, "Введите описание роли (опционально): ", false);

            Role newRole = new Role(roleName, roleDescription);
            RBACSystem.getRoleManager().add(newRole);
            System.out.println("Новая роль:" + newRole.toString());
            RBACSystem.getLogSystem().log("create", system.getCurrentUser(), "role", "User " +  system.getCurrentUser() + " create role " + roleName);
        });

        parser.registerCommand("role-view", "Просмотр роли", (scanner, system) -> {
            String roleName = ConsoleUtils.promptString(scanner, "Введите имя роли: ", true);

            Role role = RBACSystem.getRoleManager().findByName(roleName).orElse(null);
            if (role == null){
                System.out.println("Роль " + roleName + " не найдена");
                return;
            }
            System.out.println(role.toString());

            boolean type =  ConsoleUtils.promptYesNo(scanner, "Добавить права роли (yes / no): ");
            if (type){
                parser.parseAndExecute("role-add-permission", scanner, system);
            }
        });

        parser.registerCommand("role-update", "Обновить роль", (scanner, system) -> {
            String roleName = ConsoleUtils.promptString(scanner, "Введите имя роли: ", true);

            Optional<Role> optionalRole = RBACSystem.getRoleManager().findByName(roleName);
            Role role = optionalRole.orElse(null);
            if (role == null){
                System.out.println("Роль " + roleName + " не найдена");
                return;
            }

            String roleNameNew = ConsoleUtils.promptString(scanner, "Введите имя роли (оставить пустым, если не хотите изменять): ", false);
            String roleDescriptionNew = ConsoleUtils.promptString(scanner, "Введите имя роли (оставить пустым, если не хотите изменять): ", false);

            if (roleNameNew.trim().isEmpty())
                roleNameNew = roleName;
            if (roleDescriptionNew.trim().isEmpty())
                roleDescriptionNew = role.getDescription();

            Set<Permission> permissions = role.getPermissions();
            Role newRole = new Role(roleNameNew, roleDescriptionNew, permissions);
            RBACSystem.getRoleManager().remove(role);
            RBACSystem.getRoleManager().add(newRole);
            System.out.println("Новая роль:" + newRole.toString());
            RBACSystem.getLogSystem().log("update", system.getCurrentUser(), "role", "User " +  system.getCurrentUser() + " update role " + roleName);
        });

        parser.registerCommand("role-delete", "Удалить роль", (scanner, system) -> {
            String roleName = ConsoleUtils.promptString(scanner, "Введите имя роли: ", true);

            Optional<Role> optionalRole = RBACSystem.getRoleManager().findByName(roleName);
            Role role = optionalRole.orElse(null);
            if (role == null){
                System.out.println("Роль " + roleName + " не найдена");
                return;
            }

            List<RoleAssignment> roleList = RBACSystem.getAssignmentManager().findByRole(role);

            if (!roleList.isEmpty()){
                System.out.println("Роль " + roleName + " не может быть удалена, так как она назначена");
                return;
            }

            RBACSystem.getRoleManager().remove(role);
            System.out.println("Роль удалена");
            RBACSystem.getLogSystem().log("delete", system.getCurrentUser(), "role", "User " +  system.getCurrentUser() + " delete role " + roleName);
        });

        parser.registerCommand("role-add-permission", "Добавить права у роли", (scanner, system) -> {
            String roleName =ConsoleUtils.promptString(scanner, "Введите имя роли: ", true);

            Optional<Role> optionalRole = RBACSystem.getRoleManager().findByName(roleName);
            Role role = optionalRole.orElse(null);
            if (role == null){
                System.out.println("Роль " + roleName + " не найдена");
                return;
            }

            String permissionName = ConsoleUtils.promptString(scanner, "Введите имя права доступа: ", true);
            String permissionResource =ConsoleUtils.promptString(scanner, "Введите ресурс права доступа: ", true);
            String permissionDescription = ConsoleUtils.promptString(scanner, "Введите описание права доступа: ", false);

            Permission permission = new Permission(permissionName, permissionResource, permissionDescription);
            RBACSystem.getRoleManager().addPermissionToRole(roleName, permission);

            RBACSystem.getLogSystem().log("Add", system.getCurrentUser(), "permission", "User " +  system.getCurrentUser() + " add new permission for role " + roleName);

            System.out.println("Право доступа успешно добавлено");
        });

        parser.registerCommand("role-remove-permission", "Удалить права у роли", (scanner, system) -> {
            String roleName = ConsoleUtils.promptString(scanner, "Введите имя роли: ", true);

            Optional<Role> optionalRole = RBACSystem.getRoleManager().findByName(roleName);
            Role role = optionalRole.orElse(null);
            if (role == null){
                System.out.println("Роль " + roleName + " не найдена");
                return;
            }

            List<Permission> permissions = role.getPermissions().stream().toList();
            Permission value = ConsoleUtils.promptChoice(scanner, "Права доступа", permissions);
            RBACSystem.getRoleManager().removePermissionFromRole(roleName, value);
            System.out.println("Право роли удалено");
            RBACSystem.getLogSystem().log("delete", system.getCurrentUser(), "permission", "User " +  system.getCurrentUser() + " delete permission for role " + roleName);
        });

        parser.registerCommand("role-search", "Найти роль по фильтру", (scanner, system) -> {
            String text = "Выберите тип фильтрации:\n" +
                    "   1) По названию (содержит)\n" +
                    "   2) По наличию конкретного права\n" +
                    "   3) По минимальному количеству прав\n";

            int a = ConsoleUtils.promptInt(scanner, text, 1, 3);

            switch (a){
                case 1:{
                    String roleName = ConsoleUtils.promptString(scanner, "Введите название роли", true);
                    List<Role> roleList = RBACSystem.getRoleManager().findByFilter(RoleFilters.byNameContains(roleName));

                    if (roleList == null || roleList.isEmpty()) {
                        System.out.println("Роль " + roleName + " не найдена");
                        return;
                    }

                    for(Role value : roleList){
                        System.out.println(value.toString());
                        System.out.println("-".repeat(100));
                    }
                    break;
                }
                case 2:{
                    String permissionName = ConsoleUtils.promptString(scanner, "Введите имя права доступа", true);
                    String permissionResource = ConsoleUtils.promptString(scanner, "Введите ресурс права доступа", true);
                    String permissionDescription = ConsoleUtils.promptString(scanner, "Введите описание права доступа (опционально)", false);

                    Permission permission = new Permission(permissionName, permissionResource, permissionDescription);
                    List<Role> roleList = RBACSystem.getRoleManager().findByFilter(RoleFilters.hasPermission(permission));
                    if (roleList.isEmpty()){
                        System.out.println("Роли не найдены");
                        return;
                    }

                    for (Role value : roleList){
                        System.out.println(value.toString());
                    }
                    break;
                }
                case 3: {
                    int b = ConsoleUtils.promptInt(scanner, "Введите минимальное количество прав", 0, 999999999);

                    List<Role> roleList = RBACSystem.getRoleManager().findByFilter(RoleFilters.hasAtLeastNPermissions(b));
                    if (roleList.isEmpty()){
                        System.out.println("Роли не найдены");
                        return;
                    }

                    for (Role value : roleList){
                        System.out.println(value.toString());
                    }
                    break;
                }
                default:{
                    System.out.println("По выбранному типу нет фильтра, повторите попытку.");
                    break;
                }
            }
        });

        parser.registerCommand("assign-role", "Назначить роль пользователю", (scanner, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Введите имя пользователя", true);

            Optional<User> optionalUser  = RBACSystem.getUserManager().findByUsername(username);
            User user = optionalUser.orElse(null);

            if (user == null) {
                System.out.println("Пользователь с таким именем " + username + " не найден");
                return;
            }

            List<Role> roleList = RBACSystem.getRoleManager().findAll();
            Role role = ConsoleUtils.promptChoice(scanner, "Роли: ", roleList);

            String text = "Выберите тип назначения:\n" +
                    "   1) Постоянный\n" +
                    "   2) Временный\n";
            int typeAssigment = ConsoleUtils.promptInt(scanner, text, 1, 2);

            switch (typeAssigment){
                case 1: {
                    String reason = ConsoleUtils.promptString(scanner, "Введите причину назначения (опционально)", false);

                    String nowUser = system.getCurrentUser();
                    if (nowUser == null || nowUser.isEmpty()){
                        System.out.println("Не указан текущий пользовать, повторите попытку после указания");
                        return;
                    }

                    AssignmentMetadata metadata = AssignmentMetadata.now(nowUser, reason);
                    PermanentAssignment permanentAssignment = new PermanentAssignment(user, role, metadata);
                    RBACSystem.getAssignmentManager().add(permanentAssignment);
                    System.out.println("Роль назначена : " + permanentAssignment.summary());
                    RBACSystem.getLogSystem().log("appointed", system.getCurrentUser(), "assignment", "User " +  system.getCurrentUser()
                            + " appointed PERMANENT role" + role.getName() + " for user " + username);
                    break;
                }
                case 2: {
                    String reason = ConsoleUtils.promptString(scanner, "Введите причину назначения (опционально)", false);
                    String date = ConsoleUtils.promptString(scanner, "Введите дату окончания назначения (не раньше нынешней) в формате: yyyy MM dd HH:mm:ss", false);

                    LocalDateTime nowDate = LocalDateTime.parse(date, DATE_FORMAT);
                    String nowUser = system.getCurrentUser();
                    if (nowUser.isEmpty()){
                        System.out.println("Не указан текущий пользовать, повторите попытку после указания");
                        return;
                    }
                    AssignmentMetadata metadata = AssignmentMetadata.now(nowUser, reason);
                    TemporaryAssignment temporaryAssignment = new TemporaryAssignment(user, role, metadata);

                    if (!temporaryAssignment.isActive(date)){
                        temporaryAssignment.extend(date);
                        RBACSystem.getAssignmentManager().add(temporaryAssignment);
                        System.out.println("Роль назначена до даты: " + date);
                        System.out.println(temporaryAssignment.summary());
                    }
                    else {
                        RBACSystem.getAssignmentManager().add(temporaryAssignment);
                        System.out.println("Введенная дата уже прошла, поэтому роль назначена до текущей даты: \n" + temporaryAssignment.summary());
                    }
                    RBACSystem.getLogSystem().log("appointed", system.getCurrentUser(), "assignment", "User " +  system.getCurrentUser()
                            + " appointed TEMPORARY role" + role.getName() + " for user " + username);

                    break;
                }
                default: {
                    System.out.println("Неверный тип назначения, повторите попытку");
                    return;
                }
            }
        });

        parser.registerCommand("revoke-role", "Отозвать роль у пользователя", (scanner, system) -> {
            System.out.print("Введите имя пользователя: ");
            String username = scanner.nextLine();

            Optional<User> optionalUser  = RBACSystem.getUserManager().findByUsername(username);
            User user = optionalUser.orElse(null);
            if (user == null) {
                System.out.println("Пользователь с таким именем " + username + " не найден");
                return;
            }

            List<RoleAssignment> roleList = RBACSystem.getAssignmentManager().findByUser(user);
            RoleAssignment role = ConsoleUtils.promptChoice(scanner, "Роли пользователя: ", roleList);
            scanner.nextLine();

            AbstractRoleAssignment roleAssignment = (AbstractRoleAssignment) role;
            String roleType = roleAssignment.assignmentType();
            if (roleType == "PERMANENT"){
                String text = "Выберите тип аннулирования:\n" +
                        "   1) Отозвать \n" +
                        "   2) Пометить неактивным\n";
                int typeAssigment = ConsoleUtils.promptInt(scanner, text, 1, 2);

                switch (typeAssigment){
                    case 1: {
                        RBACSystem.getAssignmentManager().remove(roleAssignment);
                        System.out.println("Роль успешно отозвана");
                        RBACSystem.getLogSystem().log("recall", system.getCurrentUser(), "assignment", "User " +  system.getCurrentUser()
                                + " recall role for user " + username);
                        break;
                    }
                    case 2:{
                        RBACSystem.getAssignmentManager().revokeAssignment(roleAssignment.assignmentId());
                        System.out.println("Роль успешно аннулирована");
                        RBACSystem.getLogSystem().log("cancel", system.getCurrentUser(), "assignment", "User " +  system.getCurrentUser()
                                + " recall role for user " + username);
                        break;
                    }
                    default: {
                        System.out.println("Неверный тип аннулирования, повторите попытку");
                        return;
                    }
                }
            }
            else {
                RBACSystem.getAssignmentManager().remove(roleAssignment);
                System.out.println("Роль успешно отозвана");
            }
        });

        parser.registerCommand("assignment-list", "Список всех назначений", (scanner, system) -> {
            List<RoleAssignment> roleList = RBACSystem.getAssignmentManager().findAll();
            for (RoleAssignment roleAssignment : roleList){
                if (roleAssignment instanceof AbstractRoleAssignment) {
                    AbstractRoleAssignment role = (AbstractRoleAssignment) roleAssignment;
                    System.out.println(role.summary());
                    out.println(RED + BOLD + "-".repeat(100) + RESET);
                }
            }
        });

        parser.registerCommand("assignment-list-user", "Список назначений конкретного пользователя", (scanner, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Введите имя пользователя", true);

            Optional<User> optionalUser  = RBACSystem.getUserManager().findByUsername(username);
            User user = optionalUser.orElse(null);
            if (user == null) {
                System.out.println("Пользователь с таким именем " + username + " не найден");
                return;
            }

            List<RoleAssignment> roleList = RBACSystem.getAssignmentManager().findByUser(user);
            for (RoleAssignment roleAssignment : roleList){
                if (roleAssignment instanceof AbstractRoleAssignment) {
                    AbstractRoleAssignment role = (AbstractRoleAssignment) roleAssignment;
                    System.out.println(role.summary());
                }
            }
        });

        parser.registerCommand("assignment-list-role", "Список назначений конкретной роли", (scanner, system) -> {
            String roleName = ConsoleUtils.promptString(scanner, "Введите название роли", true);

            Optional<Role> optionalRole  = RBACSystem.getRoleManager().findByName(roleName);
            Role role = optionalRole.orElse(null);
            if (role == null) {
                System.out.println("Роль с таким названием " + roleName + " не найден");
                return;
            }

            List<RoleAssignment> roleList = RBACSystem.getAssignmentManager().findByRole(role);
            for (RoleAssignment roleAssignment : roleList){
                if (roleAssignment instanceof AbstractRoleAssignment) {
                    AbstractRoleAssignment roleAs = (AbstractRoleAssignment) roleAssignment;
                    System.out.println(roleAs.summary());
                    out.println(RED + BOLD + "-".repeat(100) + RESET);
                }
            }
        });

        parser.registerCommand("assignment-active", "Список только активные назначения", (scanner, system) -> {
            List<RoleAssignment> roleList = RBACSystem.getAssignmentManager().getActiveAssignments();
            for (RoleAssignment roleAssignment : roleList){
                if (roleAssignment instanceof AbstractRoleAssignment) {
                    AbstractRoleAssignment roleAs = (AbstractRoleAssignment) roleAssignment;
                    System.out.println(roleAs.summary());
                }
            }
        });

        parser.registerCommand("assignment-expired", "Список только истёкшие временные назначения", (scanner, system) -> {
            List<RoleAssignment> roleList = RBACSystem.getAssignmentManager().getExpiredAssignments();
            for (RoleAssignment roleAssignment : roleList){
                if (roleAssignment instanceof TemporaryAssignment) {
                    TemporaryAssignment roleAs = (TemporaryAssignment) roleAssignment;
                    System.out.println(roleAs.summary());
                }
            }
        });

        parser.registerCommand("assignment-extend", "Продлить временное назначение", (scanner, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Введите имя пользователя", true);

            Optional<User> optionalUser  = RBACSystem.getUserManager().findByUsername(username);
            User user = optionalUser.orElse(null);
            if (user == null) {
                System.out.println("Пользователь с таким именем " + username + " не найден");
                return;
            }

            String roleName = ConsoleUtils.promptString(scanner, "Введите название роли:", true);

            Optional<Role> optionalRole  = RBACSystem.getRoleManager().findByName(roleName);
            Role role = optionalRole.orElse(null);
            if (role == null) {
                System.out.println("Роль с таким названием " + roleName + " не найден");
                return;
            }

            List<TemporaryAssignment> tempRoleList = new ArrayList<>();
            List<RoleAssignment> roleList = RBACSystem.getAssignmentManager().findByUser(user);
            for (RoleAssignment roleAssignment : roleList){
                if (roleAssignment instanceof TemporaryAssignment) {
                    TemporaryAssignment temporaryAssignment = (TemporaryAssignment) roleAssignment;
                    tempRoleList.add(temporaryAssignment);
                }
            }

            List<TemporaryAssignment> resRoles = tempRoleList.stream().filter(assignment -> assignment.role().equals(role)).toList();
            if (resRoles.isEmpty()){
                System.out.println("У пользователя " + username + " нет временных назначений роли " + roleName);
                return;
            }
            TemporaryAssignment value = ConsoleUtils.promptChoice(scanner, "Временные назначения: ", resRoles);

            String date = ConsoleUtils.promptString(scanner, "Введите дату окончания назначения (не раньше нынешней) в формате: yyyy MM dd HH:mm:ss", true);

            LocalDateTime nowDate = LocalDateTime.parse(date, DATE_FORMAT);

            RBACSystem.getAssignmentManager().extendTemporaryAssignment(value.assignmentId(), date);
            System.out.println("Время обновлено");
            RBACSystem.getLogSystem().log("update", system.getCurrentUser(), "assignment", "User " +  system.getCurrentUser()
                    + " update time role " + roleName +" for " + username);
        });

        parser.registerCommand("assignment-search", "Поиск назначений по фильтрам", (scanner, system) -> {
            String text = "Меню фильтров:\n" +
                    "   1) По пользователю\n" +
                    "   2) По роли\n" +
                    "   3) По типу (постоянное/временное)\n" +
                    "   4) По статусу (активное/неактивное)\n" +
                    "   5) Назначённые после даты\n" +
                    "   6) Истекающие до даты";

            int type = ConsoleUtils.promptInt(scanner, text, 1, 4);
            switch (type){
                case 1: {
                    parser.parseAndExecute("assignment-list-user", scanner, system);
                    break;
                }
                case 2: {
                    parser.parseAndExecute("assignment-list-role", scanner, system);
                    break;
                }
                case 3: {
                    String textType = "Выберите тип:\n" +
                            "   1) Постоянные\n" +
                            "   2) Временные";
                    int typeAssigment = ConsoleUtils.promptInt(scanner, textType, 1, 2);
                    switch (typeAssigment){
                        case 1:{
                            List<RoleAssignment> roleList = RBACSystem.getAssignmentManager().findByFilter(AssignmentFilters.byType("PERMANENT"));
                            for (RoleAssignment value : roleList){
                                System.out.println(((AbstractRoleAssignment) value).summary());
                                out.println(RED + BOLD + "-".repeat(100) + RESET);
                            }
                            break;
                        }
                        case 2:{
                            List<RoleAssignment> roleList = RBACSystem.getAssignmentManager().findByFilter(AssignmentFilters.byType("TEMPORARY"));
                            for (RoleAssignment value : roleList){
                                System.out.println(((AbstractRoleAssignment) value).summary());
                                out.println(RED + BOLD + "-".repeat(100) + RESET);
                            }
                            break;
                        }
                        default:{
                            System.out.println("Выбран некорректный тип, повторите попытку");
                            return;
                        }
                    }
                    break;
                }
                case 4: {
                    String textType = "Выберите тип:\n" +
                            "   1) Активные\n" +
                            "   2) Неактивные";
                    int typeAssigment = ConsoleUtils.promptInt(scanner, textType, 1, 2);
                    scanner.nextLine();
                    switch (typeAssigment){
                        case 1:{
                            List<RoleAssignment> roleList = RBACSystem.getAssignmentManager().findByFilter(AssignmentFilters.activeOnly());
                            for (RoleAssignment value : roleList){
                                System.out.println(((AbstractRoleAssignment) value).summary());
                                out.println(RED + BOLD + "-".repeat(100) + RESET);
                            }
                            break;
                        }
                        case 2:{
                            List<RoleAssignment> roleList = RBACSystem.getAssignmentManager().findByFilter(AssignmentFilters.inactiveOnly());
                            for (RoleAssignment value : roleList){
                                System.out.println(((AbstractRoleAssignment) value).summary());
                                out.println(RED + BOLD + "-".repeat(100) + RESET);
                            }
                            break;
                        }
                        default:{
                            System.out.println("Выбран некорректный тип, повторите попытку");
                            return;
                        }
                    }
                    break;
                }
                case 5: {
                    String date = ConsoleUtils.promptString(scanner, "Введите дату назначения в формате: yyyy MM dd HH:mm:ss", true);
                    LocalDateTime nowDate = LocalDateTime.parse(date, DATE_FORMAT);

                    List<RoleAssignment> roleList = RBACSystem.getAssignmentManager().findByFilter(AssignmentFilters.assignedAfter(date));
                    for (RoleAssignment roleAssignment : roleList){
                        if (roleAssignment instanceof AbstractRoleAssignment) {
                            AbstractRoleAssignment assignment = (AbstractRoleAssignment) roleAssignment;
                            System.out.println(assignment.summary());
                            out.println(RED + BOLD + "-".repeat(100) + RESET);
                        }
                    }
                    break;
                }
                case 6: {
                    String date = ConsoleUtils.promptString(scanner, "Введите дату в формате: yyyy MM dd HH:mm:ss", true);
                    LocalDateTime nowDate = LocalDateTime.parse(date, DATE_FORMAT);

                    List<RoleAssignment> roleList = RBACSystem.getAssignmentManager().findByFilter(AssignmentFilters.expiringBefore(date));
                    for (RoleAssignment roleAssignment : roleList){
                        if (roleAssignment instanceof AbstractRoleAssignment) {
                            AbstractRoleAssignment assignment = (AbstractRoleAssignment) roleAssignment;
                            System.out.println(assignment.summary());
                            out.println(RED + BOLD + "-".repeat(100) + RESET);
                        }
                    }
                    break;
                }
                default:{
                    System.out.println("Выбран некорректный фильтр, повторите попытку");
                    return;
                }
            }
        });

        parser.registerCommand("permissions-user", "Все права конкретного пользователя", (scanner, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Введите имя пользователя", true);

            Optional<User> optionalUser  = RBACSystem.getUserManager().findByUsername(username);
            User user = optionalUser.orElse(null);
            if (user == null) {
                System.out.println("Пользователь с таким именем " + username + " не найден");
                return;
            }

            List<Role> roleList = RBACSystem.getRoleManager().findAll();
            Set<Permission> permissions = new HashSet<>();
            for(Role role : roleList){
                permissions.addAll(role.getPermissions());
            }
            Map<String, List<Permission>> permissionsByResource = permissions.stream()
                    .collect(Collectors.groupingBy(Permission::resource));

            permissionsByResource.forEach((resource, perms) -> {
                System.out.println("\nResource: " + resource);
                System.out.println("==================================");
                for (Permission value : perms){
                    System.out.println(value.format());
                }
            });

        });

        parser.registerCommand("permissions-check", "Проверить, есть ли у пользователя конкретное право", (scanner, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Введите имя пользователя", true);

            Optional<User> optionalUser  = RBACSystem.getUserManager().findByUsername(username);
            User user = optionalUser.orElse(null);
            if (user == null) {
                System.out.println("Пользователь с таким именем " + username + " не найден");
                return;
            }

            String perName = ConsoleUtils.promptString(scanner, "Введите имя права доступа", true);
            String res = ConsoleUtils.promptString(scanner, "Введите ресурс права доступа", true);

            boolean has = RBACSystem.getAssignmentManager().userHasPermission(user, perName, res);
            if (has){
                List<RoleAssignment> roleList = RBACSystem.getAssignmentManager().findByUser(user);
                for (RoleAssignment role : roleList){
                    if (role instanceof AbstractRoleAssignment) {
                        AbstractRoleAssignment assignment = (AbstractRoleAssignment) role;
                        Role thisRole = assignment.role();
                        if (thisRole.hasPermission(perName, res)){
                            System.out.println("Роль " + thisRole.getName() + " имеет права " + perName.toUpperCase() + " к ресурсу " + res.toLowerCase(Locale.ROOT));
                        }
                    }
                }

            }
        });

        parser.registerCommand("help", "Cправка по командам", (scanner, system) -> {
            parser.printHelp();
        });

        parser.registerCommand("stats", "Cтатистика системы", (scanner, system) -> {
            String stats = RBACSystem.generateStatistics();
            System.out.println(stats);

            List<RoleAssignment> activeList = RBACSystem.getAssignmentManager().getActiveAssignments();
            int coutnActive = activeList.size();
            List<RoleAssignment>expiredList = RBACSystem.getAssignmentManager().getExpiredAssignments();
            int coutnExpired = expiredList.size();
            System.out.println("Количество активных назначений: " + coutnActive);
            System.out.println("Количество неактивных назначений: " + coutnExpired);

            int countAllRoles = 0;
            List<User> userList = RBACSystem.getUserManager().findAll();
            for(User value : userList){
                List<RoleAssignment> roleAssignmentList = RBACSystem.getAssignmentManager().findByUser(value);
                for (RoleAssignment roleAssignment : roleAssignmentList){
                    countAllRoles += 1;
                }
            }

            int countUsers = RBACSystem.getUserManager().count();
            System.out.println("Среднее число ролей пользователей: " + (float) countAllRoles / countUsers);

            List<RoleAssignment> roleAssignmentList = RBACSystem.getAssignmentManager().findAll();
            List<AbstractRoleAssignment> abstractRoleAssignments = new ArrayList<>();
            for (RoleAssignment value : roleAssignmentList){
                abstractRoleAssignments.add((AbstractRoleAssignment) value);
            }

            List<String> rolesName = abstractRoleAssignments.stream()
                    .map(a -> a.role().getName())
                    .toList();
            Map<String, Integer> roleCounts = new HashMap<>();
            for (String roleName : rolesName) {
                roleCounts.merge(roleName, 1, Integer::sum);
            }

            List<Map.Entry<String, Integer>> topRoles = roleCounts.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .limit(3)
                    .toList();

            System.out.println("Топ 3");
            for (int i = 0; i < topRoles.size(); i++) {
                Map.Entry<String, Integer> entry = topRoles.get(i);
                Role role = RBACSystem.getRoleManager().findByName(entry.getKey()).orElse(null);
                System.out.println((i + 1) + ")  (" + entry.getValue() + " count) " + role.toString());
            }
        });

        parser.registerCommand("clear", "Очистить экран", (scanner, system) -> {
            for(int i=0 ;i<100 ;i++){
                System.out.println(" ");
            }
        });

        parser.registerCommand("exit", "Выход", (scanner, system) -> {
            System.out.println("Для подтверждения выхода напишите: yes");
            boolean exit = ConsoleUtils.promptYesNo(scanner, "Для подтверждения выхода напишите yes, иначе no:");

            if (exit){
                boolean save =  ConsoleUtils.promptYesNo(scanner, "Сохранить данные? yes / no:");
                if (save){
                    parser.parseAndExecute("save", scanner, system);
                }
                exit(0);
            }
            else {
                System.out.println("Выход отменён");
            }
        });

        parser.registerCommand("save", "Сохранить данные в файл (JSON)", (scanner, system) -> {
            String filename = ConsoleUtils.promptString(scanner, "Введите имя файла для сохранения", true);
            if (!filename.endsWith(".json")) {
                filename += ".json";
            }

            try (FileWriter writer = new FileWriter(filename)) {
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

                System.out.println("Данные сохранены в файл: " + filename);

            } catch (IOException e) {
                System.out.println("Ошибка сохранения: " + e.getMessage());
            }
        });

        parser.registerCommand("load", "Загрузка из JSON файла", (scanner, system) -> {
            System.out.print("Введите имя файла для загрузки: ");
            String filename = ConsoleUtils.promptString(scanner, "Введите имя файла для загрузки", true);
            filename += ".json";

            try {
                String content = new String(Files.readAllBytes(Paths.get(filename)));

                String usersSection = content.split("\"users\": \\[")[1].split("],")[0];
                String[] userBlocks = usersSection.split("},\\s*\\{");
                for (String block : userBlocks) {
                    block = block.replace("[", "").replace("]", "").replace("{", "").replace("}", "");
                    String username = "", fullName = "", email = "";

                    if (block.trim().isEmpty())
                        continue;


                    String[] pairs = block.split(",");
                    for (String pair : pairs) {
                        String[] kv = pair.split(":", 2);
                        if (kv.length < 2) continue;
                        String key = kv[0].replace("\"", "").trim();
                        String value = kv[1].replace("\"", "").trim();

                        switch (key) {
                            case "username": username = value; break;
                            case "fullName": fullName = value; break;
                            case "email": email = value; break;
                        }
                    }

                    if (username.isEmpty()){
                        System.out.println("Ошибка парсинга, отсутствует поле username у пользователя");
                        exit(2);
                    }
                    if (fullName.isEmpty()){
                        System.out.println("Ошибка парсинга, отсутствует поле username у пользователя");
                        exit(2);
                    }
                    if (email.isEmpty()){
                        System.out.println("Ошибка парсинга, отсутствует поле username у пользователя");
                        exit(2);
                    }

                    User newUser = new User(username, fullName, email);
                    RBACSystem.getUserManager().add(newUser);
                    System.out.println("Загружен пользователь: " + newUser.format());
                    RBACSystem.getLogSystem().log("CREATE", system.getCurrentUser(), "user", "User " +  system.getCurrentUser()
                            + " add new user " + username);
                }

                Pattern rolePattern = Pattern.compile("\"roles\":\\s*\\[(.*?)\"assignments\":\\s*\\[",  Pattern.DOTALL);
                Matcher roleMatcher = rolePattern.matcher(content);

                if (roleMatcher.find()) {
                    String rolesContent = roleMatcher.group(1).trim();

                    Pattern pattern = Pattern.compile("\\{(?:[^{}]|\\{(?:[^{}]|\\{[^{}]*\\})*\\})*\\}", Pattern.DOTALL );

                    Matcher matcher = pattern.matcher(rolesContent);
                    List<String> roleBlocks = new ArrayList<>();

                    while (matcher.find()) {
                        String roleBlock = matcher.group().trim();
                        if (roleBlock.endsWith(",")) {
                            roleBlock = roleBlock.substring(0, roleBlock.length() - 1);
                        }
                        roleBlocks.add(roleBlock);
                    }

                    for (int i = 0; i < roleBlocks.size(); i++) {
                        String roleBlock = roleBlocks.get(i);
                        String roleName = "", roleDis = "";
                        Set<Permission> permissions = new HashSet<>();

                        Pattern roleNamePattern = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
                        Matcher roleNameMatcher = roleNamePattern.matcher(roleBlock);
                        if (roleNameMatcher.find()) {
                            roleName = roleNameMatcher.group(1);
                        }

                        Pattern roleDescPattern = Pattern.compile("\"description\"\\s*:\\s*\"([^\"]+)\"");
                        Matcher roleDescMatcher = roleDescPattern.matcher(roleBlock);
                        if (roleDescMatcher.find()) {
                            roleDis = roleDescMatcher.group(1);
                        }

                        Pattern rolePermPattern = Pattern.compile("\"permissions\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL);
                        Matcher rolePermMatcher = rolePermPattern.matcher(roleBlock);
                        if (rolePermMatcher.find()) {
                            String permissionsBlock = rolePermMatcher.group(1);

                            Pattern permObjPattern = Pattern.compile("\\{([^}]+)\\}", Pattern.DOTALL);
                            Matcher matcherPermisson = permObjPattern.matcher(permissionsBlock);
                            String perName = "", perRes = "", desPer = "";
                            while (matcherPermisson.find()) {
                                String permStr = matcherPermisson.group(1);

                                Pattern namePatternPer = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
                                Matcher nameMatcherPer = namePatternPer.matcher(permStr);
                                if (nameMatcherPer.find()) {
                                    perName = nameMatcherPer.group(1);
                                }

                                Pattern resourcePattern = Pattern.compile("\"resource\"\\s*:\\s*\"([^\"]+)\"");
                                Matcher resourceMatcher = resourcePattern.matcher(permStr);
                                if (resourceMatcher.find()) {
                                    perRes = resourceMatcher.group(1);
                                }

                                Pattern descPatternPer = Pattern.compile("\"description\"\\s*:\\s*\"([^\"]+)\"");
                                Matcher descMatcherPer = descPatternPer.matcher(permStr);
                                if (descMatcherPer.find()) {
                                    desPer = descMatcherPer.group(1);
                                }
                                Permission perm = new Permission(perName, perRes, desPer);

                                permissions.add(perm);
                            }
                        }
                        Role newRole = new Role(roleName, roleDis, permissions);
                        RBACSystem.getRoleManager().add(newRole);
                        System.out.println("Добавлена роль: " + newRole.toString());
                        RBACSystem.getLogSystem().log("CREATE", system.getCurrentUser(), "role", "User " +  system.getCurrentUser()
                                + " add new role " + roleName);
                    }
                }

                Pattern assignmentsPattern =  Pattern.compile( "\"assignments\":\\s*\\[(.*?)\\]\\s*\\}\\s*$",  Pattern.DOTALL);
                Matcher assignmentsMatcher = assignmentsPattern.matcher(content);

                if (assignmentsMatcher.find()) {
                    String assignmentsContent = assignmentsMatcher.group(1).trim();

                    Pattern assignmentsPatternElements =  Pattern.compile(
                            "\\{(?:[^{}]|\\{[^{}]*\\})*\\}",
                            Pattern.DOTALL
                    );
                    Matcher matcherPermissonElements = assignmentsPatternElements.matcher(assignmentsContent);
                    int count = 0;
                    while (matcherPermissonElements.find()) {
                        count++;
                        String assignment = matcherPermissonElements.group().trim();

                        String type = "";
                        Pattern typePattern = Pattern.compile("\"type\"\\s*:\\s*\"([^\"]+)\"");
                        Matcher typeMather = typePattern.matcher(assignment);
                        if (typeMather.find()) {
                            type = typeMather.group(1);
                        }

                        String username = "";
                        Pattern usernamePattern = Pattern.compile("\"user\"\\s*:\\s*\"([^\"]+)\"");
                        Matcher usernameMather = usernamePattern.matcher(assignment);
                        if (usernameMather.find()) {
                            username = usernameMather.group(1);
                        }
                        User user = RBACSystem.getUserManager().findByUsername(username).orElse(null);
                        if (user == null){
                            System.out.println("Ошибка при парсинге: пользователь " + username + " не существует. Создание права доступа отменено");
                            exit(2);
                        }

                        String roleName = "";
                        Pattern roleNamePattern = Pattern.compile("\"role\"\\s*:\\s*\"([^\"]+)\"");
                        Matcher roleNameMather = roleNamePattern.matcher(assignment);
                        if (roleNameMather.find()) {
                            roleName = roleNameMather.group(1);
                        }
                        Role role = RBACSystem.getRoleManager().findByName(roleName).orElse(null);
                        if (role == null){
                            System.out.println("Ошибка при парсинге: роль " + roleName + " не существует. Создание права доступа отменено");
                            exit(2);
                        }

                        String assignedBy = "";
                        Pattern assignedByPattern = Pattern.compile("\"type\"\\s*:\\s*\"([^\"]+)\"");
                        Matcher assignedByMather = assignedByPattern.matcher(assignment);
                        if (assignedByMather.find()) {
                            assignedBy = assignedByMather.group(1);
                        }

                        String reason = "";
                        Pattern reasonPattern = Pattern.compile("\"type\"\\s*:\\s*\"([^\"]+)\"");
                        Matcher reasonMather = reasonPattern.matcher(assignment);
                        if (reasonMather.find()) {
                            reason = reasonMather.group(1);
                        }

                        AssignmentMetadata metadata = AssignmentMetadata.now(system.getCurrentUser(), reason);
                        if (type.equals("PERMANENT")){
                            PermanentAssignment permanentAssignment = new PermanentAssignment(user,role, metadata);
                            RBACSystem.getAssignmentManager().add(permanentAssignment);
                            System.out.println("Назначение добавлено: " + permanentAssignment.summary());
                            RBACSystem.getLogSystem().log("appointed", system.getCurrentUser(), "assignment", "User " +  system.getCurrentUser()
                                    + " appointed PERMANENT role" + roleName + " for user " + username);
                        }
                        else if(type.equals("TEMPORARY")){
                            String expiresAt = "";
                            Pattern expiresAtPattern = Pattern.compile("\"expiresAt\"\\s*:\\s*\"([^\"]+)\"");
                            Matcher expiresAtMather = expiresAtPattern.matcher(assignment);
                            if (expiresAtMather.find()) {
                                expiresAt = expiresAtMather.group(1);
                            }

                            String autoRenew = "";
                            Pattern autoRenewPattern = Pattern.compile("\"autoRenew\"\\s*:\\s*\"([^\"]+)\"");
                            Matcher autoRenewMather = autoRenewPattern.matcher(assignment);
                            if (autoRenewMather.find()) {
                                autoRenew = autoRenewMather.group(1);
                            }

                            boolean flag = false;
                            if (autoRenew.equals("true")){
                                flag = true;
                            }

                            TemporaryAssignment temporaryAssignment = new TemporaryAssignment(user, role, metadata, expiresAt, false);
                            RBACSystem.getAssignmentManager().add(temporaryAssignment);
                            System.out.println("Назначение добавлено: " + temporaryAssignment.summary());
                            RBACSystem.getLogSystem().log("appointed", system.getCurrentUser(), "assignment", "User " +  system.getCurrentUser()
                                    + " appointed TEMPORARY role" + roleName + " for user " + username);
                        }
                        else {
                            System.out.println("Неизвестный тип назначения " + type + ". Создание назвачения отменено");
                            exit(2);
                        }

                    }

                    System.out.println("Данные загружены успешно.");
                }

            } catch (IOException e) {
                System.out.println("Ошибка загрузки: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Ошибка парсинга JSON: " + e.getMessage());
            }
        });

        parser.registerCommand("audit-log", "Вывод логов и сохранение", (scanner, system) -> {
            System.out.println("Логи:");
            RBACSystem.getLogSystem().printLog();

            System.out.println("\n\nДля сохранения логов напишите yes:");
            boolean flag = ConsoleUtils.promptYesNo(scanner, "\n\nСохранить логи? Yes / No");
            if (flag) {
                    System.out.println("\n\nВведите имя файла для сохранения:");
                    String filename = scanner.nextLine();
                    RBACSystem.getLogSystem().saveToFile(filename);
                    if (!filename.endsWith(".json")) {
                        filename += ".json";
                    }
                    System.out.println("Логи сохранены в файл " + filename);
            }
            else {
                System.out.println("Сохранение логов отменено.");
            }
        });

        parser.registerCommand("report-users", "Вывести / сохранить (в txt) отчёт по пользователям", (scanner, system) -> {
            System.out.println("Для вывода отчета напишите 1, для сохранения в файл 0:");
            int type = ConsoleUtils.promptInt(scanner, "Для вывода отчета напишите 1, для сохранения в файл 0", 0, 1);

            switch (type) {
                case 1: {
                    String text = ReportGenerator.generateUserReport(RBACSystem.getUserManager(), RBACSystem.getAssignmentManager());
                    System.out.println(text);
                    break;
                }
                case 0: {
                    String filename = ConsoleUtils.promptString(scanner, "Введите имя файла: ", true);
                    String text = ReportGenerator.generateUserReport(RBACSystem.getUserManager(), RBACSystem.getAssignmentManager());
                    ReportGenerator.exportToFile(text, filename);
                    break;
                }
                default: {
                    System.out.println("Неверный ввод.");
                    break;
                }
            }
        });

        parser.registerCommand("report-roles", "Вывести / сохранить (в txt) отчёт по ролям", (scanner, system) -> {
            System.out.println("Для вывода отчета напишите 1, для сохранения в файл 0:");
            int type =ConsoleUtils.promptInt(scanner, "Для вывода отчета напишите 1, для сохранения в файл 0", 0, 1);

            switch (type) {
                case 1: {
                    String text = ReportGenerator.generateRoleReport(RBACSystem.getRoleManager(), RBACSystem.getAssignmentManager());
                    System.out.println(text);
                    break;
                }
                case 0: {
                    String filename = ConsoleUtils.promptString(scanner, "Введите имя файла: ", true);
                    String text = ReportGenerator.generateRoleReport(RBACSystem.getRoleManager(), RBACSystem.getAssignmentManager());
                    ReportGenerator.exportToFile(text, filename);
                    break;
                }
                default: {
                    System.out.println("Неверный ввод.");
                    break;
                }
            }
        });

        parser.registerCommand("report-matrix", "Вывести / сохранить (в txt) отчёт по правам", (scanner, system) -> {
            System.out.println("Для вывода отчета напишите 1, для сохранения в файл 0:");
            int type = ConsoleUtils.promptInt(scanner, "Для вывода отчета напишите 1, для сохранения в файл 0", 0, 1);

            switch (type) {
                case 1: {
                    String text = ReportGenerator.generatePermissionMatrix(RBACSystem.getUserManager(), RBACSystem.getAssignmentManager());
                    System.out.println(text);
                    break;
                }
                case 0: {
                    String filename = ConsoleUtils.promptString(scanner, "Введите имя файла: ", true);
                    String text = ReportGenerator.generatePermissionMatrix(RBACSystem.getUserManager(), RBACSystem.getAssignmentManager());
                    ReportGenerator.exportToFile(text, filename);
                    break;
                }
                default: {
                    System.out.println("Неверный ввод.");
                    break;
                }
            }
        });


    }
}
