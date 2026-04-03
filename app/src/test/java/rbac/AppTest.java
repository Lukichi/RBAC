package rbac;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import rbac.CommandAndMenuSystem.RBACSystem;
import rbac.Components.*;
import rbac.Filters.*;
import rbac.LogSystem.ReportGenerator;
import rbac.Managers.AssignmentManager;
import rbac.Managers.RoleManager;
import rbac.Managers.UserManager;
import rbac.OtherFunctional.DateUtils;
import rbac.OtherFunctional.FormatUtils;
import rbac.Sorters.AssignmentSorters;
import rbac.Sorters.RoleSorters;
import rbac.Sorters.UserSorters;
import rbac.LogSystem.AuditEntry;
import rbac.LogSystem.AuditLog;
import rbac.SystemValidation.ValidationUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;


class AppTest {
    private List<User> users;
    private List<Permission> permissions;
    private List<Role> roles;
    private  List<AbstractRoleAssignment> assignmentList;

    @BeforeEach
    void getData(){
        users = List.of(
                new User("admin", "Главный Админ", "admin@company.com"),
                new User("admin2", "Второй Админ", "john@gmail.com"),
                new User("maria", "Марина Семеновна", "maria@company.com"),
                new User("guest", "Гость", "guest@mail.ru"),
                new User("SERGEY", "Сергей Сергеевич", "super@mail.ru")
        );
        permissions = List.of(
                new Permission("READ", "testers", "Read users1"),
                new Permission("ReaD", "report", "Read users2"),
                new Permission("CREATE", "report", "other text"),
                new Permission("READ", "user", "text read")
        );

        roles = List.of(
                new Role("admin", "Administrator", Set.of(permissions.get(0), permissions.get(1), permissions.get(2), permissions.get(3))),
                new Role("admin-reporter", "Reporter",  Set.of(permissions.get(1), permissions.get(2))),
                new Role("user", "User", Set.of(permissions.get(3))),
                new Role("guest", "Guest", Set.of())
        );

        AssignmentMetadata metData = AssignmentMetadata.now("ADMIN", "Important reason");
        AssignmentMetadata metData2 = AssignmentMetadata.now("admin-report", "Important reason");
        DateTimeFormatter dataFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TemporaryAssignment temp1 = new TemporaryAssignment(users.get(2), roles.get(2), new AssignmentMetadata("ADMIN", LocalDateTime.now().plusHours(3).format(dataFormat), "Important reason"));
        temp1.extend(LocalDateTime.now().plusHours(3).format(dataFormat));
        TemporaryAssignment temp2 = new TemporaryAssignment(users.get(3), roles.get(2), new AssignmentMetadata("admin-report", LocalDateTime.now().plusHours(1).format(dataFormat), "Important reason"));
        temp2.extend(LocalDateTime.now().plusHours(1).format(dataFormat));

        TemporaryAssignment noActiveTempAssign = new TemporaryAssignment(users.get(1), roles.get(1), metData);
        noActiveTempAssign.deactivate();
        TemporaryAssignment noActiveTempAssign2 = new TemporaryAssignment(users.get(4), roles.get(3), metData2);
        noActiveTempAssign2.deactivate();

        assignmentList = List.of(
                new PermanentAssignment(users.get(0), roles.get(0), metData),   // admin
                noActiveTempAssign,   // admin-report
                temp1,   // user
                temp2,  // user
                noActiveTempAssign2  // guest
        );

//        System.out.println("\nДанные для теста" );
//        for(AbstractRoleAssignment value : assignmentList){
//            System.out.println(value.summary());
//        }
    }

    @Nested
    class userFilterTest{
        @Test
        void testUserFilterUsername() {
            System.out.println("Начало теста фильтрации ников");
            UserFilter filter = UserFilters.byUsernameContains("admin");

            List<User> filterData = users.stream().filter(filter::test).toList();
            List<User> result = List.of(
                    new User("admin", "Главный Админ", "admin@company.com"),
                    new User("admin2", "Второй Админ", "john@gmail.com")
            );
            assertEquals(result, filterData, "Содержание в имени подстроки");

//            System.out.println("Требуемый результат");
//            for(User value : result){
//                System.out.println(value);
//            }
//            System.out.println("\nДанные после фильтрации");
//            for(User value : filterData){
//                System.out.println(value);
//            }

            filter = UserFilters.byUsername("SERGEY");
            filterData = users.stream().filter(filter::test).toList();
            result = List.of(users.get(4));
            assertEquals(result, filterData, "Совпадение имени");

            filter = UserFilters.byEmail("guest@mail.ru");
            filterData = users.stream().filter(filter::test).toList();
            result = List.of(users.get(3));
            assertEquals(result, filterData, "Совпадение почты");

            filter = UserFilters.byEmailDomain("@company.com");
            filterData = users.stream().filter(filter::test).toList();
            result = List.of(users.get(0), users.get(2));
            assertEquals(result, filterData, "Совпадение домена поты");

            filter = UserFilters.byFullNameContains("Админ");
            filterData = users.stream().filter(filter::test).toList();
            result = List.of(users.get(0), users.get(1));
            assertEquals(result, filterData, "Наличие подстроки в имени");
        }

        @Test
        void testUserFilterMoreFilters() {
            System.out.println("Начало теста использования нескольких фильтров");
            UserFilter filter1 = UserFilters.byUsernameContains("admin");
            UserFilter filter2 = UserFilters.byEmailDomain("@gmail.com");
            UserFilter filterAnd = filter1.and(filter2);

            UserFilter filter3 = UserFilters.byFullNameContains("СЕ");
            UserFilter filter4 = UserFilters.byEmailDomain("@mail.ru");
            UserFilter filterOr = filter3.or(filter4);

            List<User> filterDataAnd = users.stream().filter(filterAnd::test).toList();
            List<User> filterDataOr = users.stream().filter(filterOr::test).toList();

            List<User> resultAnd = List.of(
                    new User("admin2", "Второй Админ", "john@gmail.com")
            );
            List<User> resultOr = List.of(
                    new User("maria", "Марина Семеновна", "maria@company.com"),
                    new User("guest", "Гость", "guest@mail.ru"),
                    new User("SERGEY", "Сергей Сергеевич", "super@mail.ru")
            );

            System.out.println("Требуемый результат И");
            for(User value : resultAnd){
                System.out.println(value);
            }
            System.out.println("\nДанные после фильтрации И");
            for(User value : filterDataAnd){
                System.out.println(value);
            }
            assertEquals(resultAnd.size(), filterDataAnd.size(), "Сравнение И: размеры должны совпадать");
            assertEquals(resultAnd, filterDataAnd, "Сравнение И: элементы должны совпадать");

            System.out.println("Требуемый результат ИЛИ");
            for(User value : resultOr){
                System.out.println(value);
            }
            System.out.println("\nДанные после фильтрации ИЛИ");
            for(User value : filterDataOr){
                System.out.println(value);
            }
            assertEquals(resultOr.size(), filterDataOr.size(), "Сравнение ИЛИ: размеры должны совпадать");
            assertEquals(resultOr, filterDataOr, "Сравнение ИЛИ: элементы должны совпадать");

            System.out.println("Тест прошёл");
        }
    }


    @Nested
    class roleFilterTest{
        @Test
        void testRoleFilter() {
            System.out.println("Начало теста использования фильтров для ролей");

            RoleFilter filter = RoleFilters.byName("ADMIN");
            List<Role> filterResult = roles.stream().filter(filter::test).toList();
            List<Role> result = List.of(roles.get(0));
            assertEquals(result, filterResult, "Совпадение имени");

//            System.out.println("Требуемый результат фильтрации по названию роли");
//            for(Role value : result){
//                System.out.println(value);
//            }
//            System.out.println("\nДанные после фильтрации");
//            for(Role value : filterResult){
//                System.out.println(value);
//            }

            filter = RoleFilters.byNameContains("ADMIN");
            filterResult = roles.stream().filter(filter::test).toList();
            result = List.of(roles.get(0), roles.get(1));
            assertEquals(result, filterResult, "Содержание подстроки в имени");

            filter = RoleFilters.hasPermission(permissions.get(3));
            filterResult = roles.stream().filter(filter::test).toList();
            result = List.of(roles.get(0), roles.get(2));
            assertEquals(result, filterResult, "Наличия права доступа по элементу");

            filter = RoleFilters.hasPermission(permissions.get(3).name(), permissions.get(3).resource());
            filterResult = roles.stream().filter(filter::test).toList();
            result = List.of(roles.get(0), roles.get(2));
            assertEquals(result, filterResult, "Наличия права доступа по названию и ресурсу");

            filter = RoleFilters.hasAtLeastNPermissions(1);
            filterResult = roles.stream().filter(filter::test).toList();
            result = List.of(roles.get(0), roles.get(1), roles.get(2));
            assertEquals(result, filterResult, "Наличие минимального числа прав");
        }

        @Test
        void testRoleFilterMoreFilters() {
            RoleFilter filter1 = RoleFilters.hasPermission("read", "USER");
            RoleFilter filter2 = RoleFilters.hasAtLeastNPermissions(2);
            RoleFilter filter = filter1.and(filter2);
            List<Role> filterResult = roles.stream().filter(filter::test).toList();

            List<Role> result = List.of(roles.get(0));

            System.out.println("Требуемый результат фильтрации по нескольким признакам" );
            for(Role value : result){
                System.out.println(value);
            }
            System.out.println("\nДанные после фильтрации");
            for(Role value : filterResult){
                System.out.println(value);
            }

            assertEquals(result.size(), filterResult.size(), "Количество записей должно совпадать");
            assertEquals(result, filterResult, "Записи должны совпадать");
            System.out.println("Тест фильтрации по нескольким признакам пройден");
        }
    }

    @Nested
    class assigmentFilterTest{

        private  List<AbstractRoleAssignment> assignmentAssigList;

        @BeforeEach
        void initData() {
            AssignmentMetadata metData = AssignmentMetadata.now("ADMIN", "Important reason");
            AssignmentMetadata metData2 = AssignmentMetadata.now("admin-report", "Important reason");
            DateTimeFormatter dataFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            TemporaryAssignment temp1 = new TemporaryAssignment(users.get(2), roles.get(2), new AssignmentMetadata("ADMIN", LocalDateTime.now().plusHours(3).format(dataFormat), "Important reason"));
            temp1.extend(LocalDateTime.now().plusHours(3).format(dataFormat));
            TemporaryAssignment temp2 = new TemporaryAssignment(users.get(3), roles.get(2), new AssignmentMetadata("admin-report", LocalDateTime.now().plusHours(1).format(dataFormat), "Important reason"));
            temp2.extend(LocalDateTime.now().plusHours(1).format(dataFormat));
            assignmentAssigList = List.of(
                    new PermanentAssignment(users.get(0), roles.get(0), metData),   // admin
                    new TemporaryAssignment(users.get(1), roles.get(1), metData),   // admin-report
                    temp1,   // user
                    temp2,  // user
                    new TemporaryAssignment(users.get(4), roles.get(3), metData2),   // guest
                    new TemporaryAssignment(users.get(0), roles.get(3), metData2)
            );
        }

        @Test
        void testAssigmentUser(){
            AssignmentFilter filter1 = AssignmentFilters.byUser(users.get(0));
            AssignmentFilter filter2 = AssignmentFilters.byUser(users.get(1));
            AssignmentFilter filter = filter1.or(filter2);

            List<AbstractRoleAssignment> resultFilter = assignmentList.stream().filter(filter::test).toList();
            List<AbstractRoleAssignment> result = List.of(assignmentList.get(0), assignmentList.get(1));
            assertEquals(result, resultFilter, "Назначен конкретному пользователю по элементу");

            filter =AssignmentFilters.byUsername(users.get(0).username());
            resultFilter = assignmentList.stream().filter(filter::test).toList();
            result = List.of(assignmentList.get(0));
            assertEquals(result, resultFilter, "Назначен конкретному пользователю по имени");

        }

        @Test
        void testAssigmentRole(){
            AssignmentFilter filter1 = AssignmentFilters.byRole(roles.get(0));
            AssignmentFilter filter2 = AssignmentFilters.byRoleName("user");
            AssignmentFilter filter = filter1.or(filter2);

            List<AbstractRoleAssignment> resultFilter = assignmentList.stream().filter(filter::test).toList();
            List<AbstractRoleAssignment> result = List.of(assignmentList.get(0), assignmentList.get(2), assignmentList.get(3));
            assertEquals(result, resultFilter, "Назначена конкретная роль");
        }

        @Test
        void testAssigmentActive(){
            AssignmentFilter filter1 = AssignmentFilters.activeOnly();
            AssignmentFilter filter2 = AssignmentFilters.inactiveOnly();

            List<AbstractRoleAssignment> resultFilter = assignmentList.stream().filter(filter1::test).toList();
            List<AbstractRoleAssignment> result = List.of(assignmentList.get(0), assignmentList.get(2), assignmentList.get(3));
            assertEquals(result, resultFilter, "Активные роли");

            List<AbstractRoleAssignment> resultFilter2 = assignmentList.stream().filter(filter2::test).toList();
            List<AbstractRoleAssignment> result2 = List.of(assignmentList.get(1), assignmentList.get(4));
            assertEquals(result2, resultFilter2, "Не активные роли");
        }

        @Test
        void testAssigmentByType(){
            AssignmentFilter filter1 = AssignmentFilters.byType("PERMANENT");
            AssignmentFilter filter2 = AssignmentFilters.byType("TEMpoRARY");

            List<AbstractRoleAssignment> resultFilter = assignmentList.stream().filter(filter1::test).toList();
            List<AbstractRoleAssignment> result = List.of(assignmentList.get(0));
            assertEquals(result, resultFilter, "Тип назначения PERMANENT");

            List<AbstractRoleAssignment> resultFilter2 = assignmentList.stream().filter(filter2::test).toList();
            List<AbstractRoleAssignment> result2 = List.of(assignmentList.get(1),assignmentList.get(2), assignmentList.get(3), assignmentList.get(4));
            assertEquals(result2, resultFilter2, "Тип назначения TEMPORARY");
        }

        @Test
        void testAssigmentWhoAssigmenting(){
            AssignmentFilter filter = AssignmentFilters.assignedBy("adMin-Report");

            List<AbstractRoleAssignment> resultFilter = assignmentList.stream().filter(filter::test).toList();
            List<AbstractRoleAssignment> result = List.of(assignmentList.get(3), assignmentList.get(4));
            assertEquals(result, resultFilter, "Назначил пользователь");
        }

        @Test
        void testAssigmentAfterAssigmenting(){
            DateTimeFormatter dataFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            AssignmentFilter filter = AssignmentFilters.assignedAfter(LocalDateTime.now().plusHours(2).format(dataFormat));

            List<AbstractRoleAssignment> resultFilter = assignmentList.stream().filter(filter::test).toList();
            List<AbstractRoleAssignment> result = List.of(assignmentList.get(2));
            assertEquals(result, resultFilter, "Назначена после даты");
        }

        @Test
        void testAssigmentBeforeAssigmenting(){
            DateTimeFormatter dataFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            AssignmentFilter filter = AssignmentFilters.expiringBefore(LocalDateTime.now().plusHours(2).format(dataFormat));

            List<AbstractRoleAssignment> resultFilter = assignmentList.stream().filter(filter::test).toList();
            List<AbstractRoleAssignment> result = List.of(assignmentList.get(1), assignmentList.get(3), assignmentList.get(4));
            assertEquals(result, resultFilter, "Временные назначения, истекающие до даты");
        }
    }

    @Nested
    class sortTests{

        @Test
        void sortUserByUsername(){
            List<User> sortedUsers = users.stream().sorted(UserSorters.byUsername()).collect(Collectors.toList());

            List<User> result = List.of(users.get(0), users.get(1), users.get(3), users.get(2), users.get(4));

            System.out.println("\nТребуемый результат сортировки по нику" );
            for(User value : result){
                System.out.println(value);
            }
            System.out.println("\nДанные после сортировки по нику");
            for(User value : sortedUsers){
                System.out.println(value);
            }

            assertEquals(result, sortedUsers, "Записи должны совпадать");
        }

        @Test
        void sortUserByFullname(){
            List<User> sortedUsers = users.stream().sorted(UserSorters.byFullName()).collect(Collectors.toList());

            List<User> result = List.of(users.get(1), users.get(0), users.get(3), users.get(2), users.get(4));

            System.out.println("\nТребуемый результат сортировки по нику" );
            for(User value : result){
                System.out.println(value);
            }
            System.out.println("\nДанные после сортировки по нику");
            for(User value : sortedUsers){
                System.out.println(value);
            }

            assertEquals(result, sortedUsers, "Записи должны совпадать");
        }

        @Test
        void sortUserByEmail(){
            List<User> sortedUsers = users.stream().sorted(UserSorters.byEmail()).collect(Collectors.toList());

            List<User> result = List.of(users.get(0), users.get(3), users.get(1), users.get(2), users.get(4));

            System.out.println("\nТребуемый результат сортировки по нику" );
            for(User value : result){
                System.out.println(value);
            }
            System.out.println("\nДанные после сортировки по нику");
            for(User value : sortedUsers){
                System.out.println(value);
            }

            assertEquals(result, sortedUsers, "Записи должны совпадать");
        }

        @Test
        void sortRoleByName(){
            List<Role> sortedUsers = roles.stream().sorted(RoleSorters.byName()).collect(Collectors.toList());

            List<Role> result = List.of(roles.get(0), roles.get(1), roles.get(3), roles.get(2));

            System.out.println("\nТребуемый результат сортировки ролей по названию" );
            for(Role value : result){
                System.out.println(value);
            }
            System.out.println("\nДанные после сортировки ролей по названию");
            for(Role value : sortedUsers){
                System.out.println(value);
            }

            assertEquals(result, sortedUsers, "Записи должны совпадать");
        }

        @Test
        void sortRoleByCount(){
            List<Role> sortedUsers = roles.stream().sorted(RoleSorters.byPermissionCount()).collect(Collectors.toList());

            List<Role> result = List.of(roles.get(3), roles.get(2), roles.get(1), roles.get(0));

            System.out.println("\nТребуемый результат сортировки ролей по названию (увеличение)" );
            for(Role value : result){
                System.out.println(value);
            }
            System.out.println("\nДанные после сортировки ролей по названию");
            for(Role value : sortedUsers){
                System.out.println(value);
            }

            assertEquals(result, sortedUsers, "Записи должны совпадать");
        }

        @Test
        void sortAssigmentByUsername(){
            List<AbstractRoleAssignment> sortedUsers = assignmentList.stream().sorted(AssignmentSorters.byUsername()).collect(Collectors.toList());

            List<AbstractRoleAssignment> result = List.of(assignmentList.get(0), assignmentList.get(1), assignmentList.get(3), assignmentList.get(2), assignmentList.get(4));

            System.out.println("\nТребуемый результат сортировки прав доступа по имени пользователя" );
            for(AbstractRoleAssignment value : result){
                System.out.println(value.summary());
            }
            System.out.println("\nДанные после сортировки прав доступа по имени пользователя");
            for(AbstractRoleAssignment value : sortedUsers){
                System.out.println(value.summary());
            }

            assertEquals(result, sortedUsers, "Записи должны совпадать");
        }

        @Test
        void sortAssigmentByRoleName(){
            List<AbstractRoleAssignment> sortedUsers = assignmentList.stream().sorted(AssignmentSorters.byRoleName()).collect(Collectors.toList());

            List<AbstractRoleAssignment> result = List.of(assignmentList.get(0), assignmentList.get(1), assignmentList.get(4), assignmentList.get(2), assignmentList.get(3));

            System.out.println("\nТребуемый результат сортировки прав доступа по имени пользователя" );
            for(AbstractRoleAssignment value : result){
                System.out.println(value.summary());
            }
            System.out.println("\nДанные после сортировки прав доступа по имени пользователя");
            for(AbstractRoleAssignment value : sortedUsers){
                System.out.println(value.summary());
            }

            assertEquals(result, sortedUsers, "Записи должны совпадать");
        }

        @Test
        void sortAssigmentByDate(){
            List<AbstractRoleAssignment> sortedUsers = assignmentList.stream().sorted(AssignmentSorters.byAssignmentDate()).collect(Collectors.toList());

            List<AbstractRoleAssignment> result = List.of(assignmentList.get(0), assignmentList.get(1), assignmentList.get(4), assignmentList.get(3), assignmentList.get(2));

            System.out.println("\nТребуемый результат сортировки прав доступа по имени пользователя" );
            for(AbstractRoleAssignment value : result){
                System.out.println(value.summary());
            }
            System.out.println("\nДанные после сортировки прав доступа по имени пользователя");
            for(AbstractRoleAssignment value : sortedUsers){
                System.out.println(value.summary());
            }

            assertEquals(result, sortedUsers, "Записи должны совпадать");
        }
    }

    @Nested
    class testManagers{

        @Test
        void testUserManager(){
            UserManager usersManager = new UserManager();
            usersManager.add(users.get(0));
            assertThrows(IllegalArgumentException.class, () -> {
                usersManager.add(users.get(0));
            });
            usersManager.add(users.get(1));
            usersManager.add(users.get(2));
            usersManager.add(users.get(3));
            usersManager.add(users.get(4));

            assertEquals(5, usersManager.count(), "Размеры должны совпадать");

            Optional<User> resultFindOne = Optional.of(users.get(0));
            Optional<User> resultFindOneManager = usersManager.findByUsername("admin");
            assertEquals(resultFindOne, resultFindOneManager);

            boolean resultExistsManager = usersManager.exists("guest");
            assertEquals(true, resultExistsManager);

            usersManager.update("SERGEY", "", "super-sergey@mail.ru");
            List<User> resultUpdateData = List.of(
                    new User("admin2", "Второй Админ", "john@gmail.com"),
                    new User("admin", "Главный Админ", "admin@company.com"),
                    new User("guest", "Гость", "guest@mail.ru"),
                    new User("maria", "Марина Семеновна", "maria@company.com"),
                    new User("SERGEY", "Сергей Сергеевич", "super-sergey@mail.ru")
            );
            List<User> resultUpdateManager = usersManager.findAll(null, UserSorters.byFullName());

            UserManager userManager2 = new UserManager();
            for (User value : resultUpdateData)
                userManager2.add(value);
            assertEquals(true, usersManager.equals(userManager2));

            usersManager.remove(resultUpdateData.get(0));
            assertEquals(resultUpdateData, resultUpdateManager);

            usersManager.remove(resultUpdateData.get(0));
            assertEquals(4, usersManager.count(), "Размеры должны совпадать");


        }

        @Test
        void testRoleManager(){
            RoleManager roleManager = new RoleManager();
            roleManager.add(roles.get(0));
            roleManager.add(roles.get(1));
            roleManager.add(roles.get(2));
            roleManager.add(roles.get(3));

            roleManager.addPermissionToRole("guest", permissions.get(0));
            Optional<Role> resultFindOne = Optional.of(roles.get(3));
            Optional<Role> resultFindOneManager = roleManager.findByName("guest");
            assertEquals(resultFindOne, resultFindOneManager);

            List<Role> roleList = roleManager.findRolesWithPermission("read", "REport");  // порядок обратный (обратный по отношению к добавлению ролей)
            List<Role> result = List.of(roles.get(0), roles.get(1));

            for (Role value : result)
                System.out.println(value.toString());

            assertEquals(result.size(), roleList.size(), "Размеры должны совпадать");
            assertEquals(result, roleList);
        }

        @Test
        void testAssigmentsManager() throws InterruptedException {
            AssignmentManager assignmentManager = new AssignmentManager();
            assignmentManager.add(assignmentList.get(0));
            assignmentManager.add(assignmentList.get(1));
            assignmentManager.add(assignmentList.get(2));
            assignmentManager.add(assignmentList.get(3));
            assignmentManager.add(assignmentList.get(4));

            List<RoleAssignment> activeManager = assignmentManager.getActiveAssignments(); // сортируем по нику, иначе порядок рандомный
            List<RoleAssignment> activeResult = List.of(assignmentList.get(0), assignmentList.get(3), assignmentList.get(2));
//            assertEquals(activeResult.size(), activeManager.size(), "Размеры должны совпадать");
            for(RoleAssignment value : activeManager) {
                AbstractRoleAssignment help = (AbstractRoleAssignment) value;
                System.out.println(help.summary());
            }
            System.out.println("===============");
            for(RoleAssignment value : activeResult) {
                AbstractRoleAssignment help = (AbstractRoleAssignment) value;
                System.out.println(help.summary());
            }
            assertEquals(activeResult, activeManager);

            // на примере temp1, должна быть правда
            boolean hasRoleManager = assignmentManager.userHasRole(users.get(2), roles.get(2));
            assertEquals(true, hasRoleManager);

            // на примере temp1, должна быть правда
            boolean hasPermissoinsManager = assignmentManager.userHasPermission(users.get(2), permissions.get(3).name(), permissions.get(3).resource());
            assertEquals(true, hasPermissoinsManager);

            Set<Permission> getPirmisionsManager = assignmentManager.getUserPermissions(users.get(2));
            Set<Permission> getPirmisionsResult = new HashSet<>();
            getPirmisionsResult.add(permissions.get(3));
            assertEquals(getPirmisionsResult, getPirmisionsManager);
        }
    }

    @Nested
    class testCommandAndSystem{
        @Test
        void testSystem() {
            RBACSystem system = new RBACSystem();
            system.initialize();
            system.setCurrentUser("admin");
            String nameUser = system.getCurrentUser();
            assertEquals("admin", nameUser);

            assertThrows(IllegalArgumentException.class, () -> {
                system.setCurrentUser("admin2@1");
            });

            System.out.println(RBACSystem.generateStatistics());

            String resultStatictic = "Count users: " + 1 + "\nCount roles: " + 1 +"\nCount assignments: " + 1;
            assertEquals(resultStatictic, RBACSystem.generateStatistics());
        }
    }

    @Nested
    class validTests {

        @Test
        void testValidUsername(){
            assertEquals(true, ValidationUtils.isValidUsername("admin"));
            assertEquals(true, ValidationUtils.isValidUsername("test2"));
            assertEquals(false, ValidationUtils.isValidUsername("error@"));
            assertEquals(false, ValidationUtils.isValidUsername(""));
        }

        @Test
        void testValidEmail(){
            assertEquals(true, ValidationUtils.isValidEmail("admin@mail.ru"));
            assertEquals(true, ValidationUtils.isValidEmail("test$2@gmail.com"));
            assertEquals(false, ValidationUtils.isValidEmail(""));
            assertEquals(false, ValidationUtils.isValidEmail("text@text@.ru"));
        }

        @Test
        void testValidDate(){
            assertEquals(true, ValidationUtils.isValidDate("2026-03-06 11:11:00"));
            assertEquals(false, ValidationUtils.isValidDate(""));
            assertEquals(false, ValidationUtils.isValidDate("11.11 09/01/26"));
            assertEquals(false, ValidationUtils.isValidDate("2026 03 06 11:11"));
        }

        @Test
        void testNormalize(){
            assertEquals("text", ValidationUtils.normalizeString("        text        "));
            assertEquals("", ValidationUtils.normalizeString(""));
            assertEquals("test", ValidationUtils.normalizeString("TEST"));
            assertEquals("test", ValidationUtils.normalizeString("\ntest\n"));
        }

        @Test
        void testThrows(){
            assertThrows(IllegalArgumentException.class, () -> {ValidationUtils.requireNonEmpty("", "test");});
            assertThrows(IllegalArgumentException.class, () -> {ValidationUtils.requireNonEmpty("\n\n", "test");});
            assertThrows(IllegalArgumentException.class, () -> {ValidationUtils.requireNonEmpty("           ", "test");});
            assertDoesNotThrow(() -> {ValidationUtils.requireNonEmpty("text", "test");});
        }
    }

    @Nested
    class testLog {

        record testLogReord (String action, String performer, String target, String details){};

        AuditLog logs = new AuditLog();

        @BeforeEach
        void initLog() {
            logs.log("Create", "admin", "user", "Create test user");
            logs.log("Create", "admin-reporter", "REPORT", "");
            logs.log("UPDATE", "admin", "user", null);
        }

        @Test
        void testAdd() {
            assertDoesNotThrow(() -> {logs.log("Create", "admin", "user", "Create test user");});
            assertDoesNotThrow(() -> {logs.log("Create", "admin-reporter", "REPORT", "");});
            assertDoesNotThrow(() -> {logs.log("UPDATE", "admin", "user", null);});
            assertThrows(IllegalArgumentException.class, () -> {logs.log("  ", "admin", "user", null);});
            assertThrows(IllegalArgumentException.class, () -> {logs.log("create", null, "user", null);});
        }

        @Test
        void testGetAllLog() {
            List<AuditEntry> allLogs = new ArrayList<>();
            allLogs.add(new AuditEntry("11","CREATE", "admin", "user", "Create test user"));
            allLogs.add(new AuditEntry("11","CREATE", "admin-reporter", "report", ""));
            allLogs.add(new AuditEntry("11","UPDATE", "admin", "user", null));

            List<AuditEntry> logList = logs.getAll();

            List<testLogReord> reseltRecords = new ArrayList<>();
            List<testLogReord> logsRecord = new ArrayList<>();
            for (AuditEntry value : allLogs) {
                reseltRecords.add(new testLogReord(value.action(), value.performer(), value.target(), value.details()));
            }
            for (AuditEntry value : logList) {
                logsRecord.add(new testLogReord(value.action(), value.performer(), value.target(), value.details()));
            }

            assertEquals(reseltRecords, logsRecord);
        }

        @Test
        void testGetPerformLog() {
            List<AuditEntry> allLogs = new ArrayList<>();
            allLogs.add(new AuditEntry("11","CREATE", "admin", "user", "Create test user"));
            allLogs.add(new AuditEntry("11","CREATE", "admin-reporter", "report", ""));
            allLogs.add(new AuditEntry("11","UPDATE", "admin", "user", null));

            List<AuditEntry> logList = logs.getByPerformer("admin");

            List<testLogReord> reseltRecords = new ArrayList<>();
            reseltRecords.add(new testLogReord(allLogs.get(0).action(), allLogs.get(0).performer(), allLogs.get(0).target(), allLogs.get(0).details()));
            reseltRecords.add(new testLogReord(allLogs.get(2).action(), allLogs.get(2).performer(), allLogs.get(2).target(), allLogs.get(2).details()));

            List<testLogReord> logsRecord = new ArrayList<>();
            for (AuditEntry value : logList) {
                logsRecord.add(new testLogReord(value.action(), value.performer(), value.target(), value.details()));
            }

            assertEquals(reseltRecords, logsRecord);

            logList = logs.getByPerformer("ADMIN-reporter");
            logsRecord.clear();
            for (AuditEntry value : logList) {
                logsRecord.add(new testLogReord(value.action(), value.performer(), value.target(), value.details()));
            }
            reseltRecords.clear();
            reseltRecords.add(new testLogReord(allLogs.get(1).action(), allLogs.get(1).performer(), allLogs.get(1).target(), allLogs.get(1).details()));

            assertEquals(reseltRecords, logsRecord);

            logs.printLog();
        }
    }

    @Nested
    class testReportGenerator{

        UserManager usersManager = new UserManager();
        RoleManager roleManager = new RoleManager();
        AssignmentManager assignmentManager = new AssignmentManager();

        private  List<AbstractRoleAssignment> assignmentRepList;

        @BeforeEach
        void initData(){
            AssignmentMetadata metData = AssignmentMetadata.now("ADMIN", "Important reason");
            AssignmentMetadata metData2 = AssignmentMetadata.now("admin-report", "Important reason");
            DateTimeFormatter dataFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            TemporaryAssignment temp1 = new TemporaryAssignment(users.get(2), roles.get(2), new AssignmentMetadata("ADMIN", LocalDateTime.now().plusHours(3).format(dataFormat), "Important reason"));
            temp1.extend(LocalDateTime.now().plusHours(3).format(dataFormat));
            TemporaryAssignment temp2 = new TemporaryAssignment(users.get(3), roles.get(2), new AssignmentMetadata("admin-report", LocalDateTime.now().plusHours(1).format(dataFormat), "Important reason"));
            temp2.extend(LocalDateTime.now().plusHours(1).format(dataFormat));
            assignmentRepList = List.of(
                    new PermanentAssignment(users.get(0), roles.get(0), metData),   // admin
                    new TemporaryAssignment(users.get(1), roles.get(1), metData),   // admin-report
                    temp1,   // user
                    temp2,  // user
                    new TemporaryAssignment(users.get(4), roles.get(3), metData2),   // guest
                    new TemporaryAssignment(users.get(0), roles.get(3), metData2)
            );

            usersManager.add(users.get(0));
            usersManager.add(users.get(1));
            usersManager.add(users.get(2));
            usersManager.add(users.get(3));
            usersManager.add(users.get(4));

//            for (User user : users){
//                System.out.println(user.format());
//            }
//            System.out.println("\n\n" + "=".repeat(50) + "\n\n");

            assignmentManager.add(assignmentRepList.get(0));
            assignmentManager.add(assignmentRepList.get(1));
            assignmentManager.add(assignmentRepList.get(2));
            assignmentManager.add(assignmentRepList.get(3));
            assignmentManager.add(assignmentRepList.get(4));
            assignmentManager.add(assignmentRepList.get(5));
//
//            for (AbstractRoleAssignment value : assignmentRepList){
//                System.out.println(value.summary());
//            }

            roleManager.add(roles.get(0));
            roleManager.add(roles.get(1));
            roleManager.add(roles.get(2));
            roleManager.add(roles.get(3));
        }

//        permissions = List.of(
//                new Permission("READ", "testers", "Read users1"),
//                new Permission("ReaD", "report", "Read users2"),
//                new Permission("Write", "report", "other text"),
//                new Permission("READ", "user", "text read")
//        );
//
//        roles = List.of(
//                new Role("admin", "Administrator", Set.of(permissions.get(0), permissions.get(1), permissions.get(2), permissions.get(3))),
//                new Role("admin-reporter", "Reporter",  Set.of(permissions.get(1), permissions.get(2))),
//                new Role("user", "User", Set.of(permissions.get(3))),
//                new Role("guest", "Guest", Set.of())
//                );

//        users = List.of(
//                new User("admin", "Главный Админ", "admin@company.com"),
//                new User("admin2", "Второй Админ", "john@gmail.com"),
//                new User("maria", "Марина Семеновна", "maria@company.com"),
//                new User("guest", "Гость", "guest@mail.ru"),
//                new User("SERGEY", "Сергей Сергеевич", "super@mail.ru")
//        );

        @Test
        void testUserReport(){
            ReportGenerator reportGenerator = new ReportGenerator();
            String resultGenerator = reportGenerator.generateUserReport(usersManager, assignmentManager);

            StringBuilder reportAdmin = new StringBuilder();
            reportAdmin.append("\n\n").append("admin:\n").append(roles.get(0).toString()).append(roles.get(3).toString());

            StringBuilder reportAdmin2 = new StringBuilder();
            reportAdmin2.append("\n\n").append("admin2:\n").append(roles.get(1).toString());

            StringBuilder reportGuest = new StringBuilder();
            reportGuest.append("\n\n").append("guest:\n").append(roles.get(2).toString());

            StringBuilder reportMaria = new StringBuilder();
            reportMaria.append("\n\n").append("maria:\n").append(roles.get(2).toString());

            StringBuilder reportSegey = new StringBuilder();
            reportSegey.append("\n\n").append("SERGEY:\n").append(roles.get(3).toString());

            StringBuilder resultReport = new StringBuilder();
            resultReport.append(reportAdmin.toString()).append("=".repeat(60)).append("\n");
            resultReport.append(reportAdmin2.toString()).append("=".repeat(60)).append("\n");
            resultReport.append(reportGuest.toString()).append("=".repeat(60)).append("\n");
            resultReport.append(reportMaria.toString()).append("=".repeat(60)).append("\n");
            resultReport.append(reportSegey.toString()).append("=".repeat(60)).append("\n");

            assertEquals(resultReport.toString(), resultGenerator);
        }

        @Test
        void testRoleReport() {
            ReportGenerator reportGenerator = new ReportGenerator();
            String resultGenerator = reportGenerator.generateRoleReport(roleManager, assignmentManager);

            StringBuilder reportAR = new StringBuilder();

            StringBuilder reportAdmin = new StringBuilder();
            reportAR.append(String.format("%s (%d count):\n", roles.get(1).getName(), 1));
            reportAR.append(String.format("   - %s\n", users.get(1).format()));

            reportAdmin.append(String.format("%s (%d count):\n", roles.get(0).getName(), 1));
            reportAdmin.append(String.format("   - %s\n", users.get(0).format()));

            StringBuilder reportGuest = new StringBuilder();
            reportGuest.append(String.format("%s (%d count):\n", roles.get(3).getName(), 2));
            reportGuest.append(String.format("   - %s\n", users.get(0).format()));
            reportGuest.append(String.format("   - %s\n", users.get(4).format()));

            StringBuilder reportUser = new StringBuilder();
            reportUser.append(String.format("%s (%d count):\n", roles.get(2).getName(), 2));
            reportUser.append(String.format("   - %s\n", users.get(3).format()));
            reportUser.append(String.format("   - %s\n", users.get(2).format()));

            StringBuilder resultReport = new StringBuilder();
            resultReport.append(reportAdmin.toString()).append("\n").append("=".repeat(60)).append("\n\n");
            resultReport.append(reportAR.toString()).append("\n").append("=".repeat(60)).append("\n\n");
            resultReport.append(reportGuest.toString()).append("\n").append("=".repeat(60)).append("\n\n");
            resultReport.append(reportUser.toString()).append("\n").append("=".repeat(60)).append("\n\n");

            assertEquals(resultReport.toString(), resultGenerator);
        }

        @Test
        void testPerMat(){
            ReportGenerator reportGenerator = new ReportGenerator();
            String resultGenerator = reportGenerator.generatePermissionMatrix(usersManager, assignmentManager);

            StringBuilder reportAdmin = new StringBuilder();
            reportAdmin.append(String.format("%-20s | %-10s | %-10s | %-10s | %-10s |\n", "", "user", "role", "assignment", "report"));
            reportAdmin.append("-".repeat(74) + "\n");
            reportAdmin.append(String.format("%-20s | %-10s | %-10s | %-10s | %-10s |\n", "admin", "-R--", "----", "----", "CR--"));
            reportAdmin.append("-".repeat(74) + "\n");

            StringBuilder reportAdmin2 = new StringBuilder();
            reportAdmin2.append(String.format("%-20s | %-10s | %-10s | %-10s | %-10s |\n", "admin2", "----", "----", "----", "CR--"));
            reportAdmin2.append("-".repeat(74) + "\n");

            StringBuilder reportGuest = new StringBuilder();
            reportGuest.append(String.format("%-20s | %-10s | %-10s | %-10s | %-10s |\n", "guest", "-R--", "----", "----", "----"));
            reportGuest.append("-".repeat(74) + "\n");

            StringBuilder reportMaria = new StringBuilder();
            reportMaria.append(String.format("%-20s | %-10s | %-10s | %-10s | %-10s |\n", "maria", "-R--", "----", "----", "----"));
            reportMaria.append("-".repeat(74) + "\n");

            StringBuilder reportSegey = new StringBuilder();
            reportSegey.append(String.format("%-20s | %-10s | %-10s | %-10s | %-10s |\n", "SERGEY", "----", "----", "----", "----"));
            reportSegey.append("-".repeat(74) + "\n");

            StringBuilder resultReport = new StringBuilder();
            resultReport.append(reportAdmin.toString());
            resultReport.append(reportAdmin2.toString());
            resultReport.append(reportGuest.toString());
            resultReport.append(reportMaria.toString());
            resultReport.append(reportSegey.toString());
            resultReport.append("C - create\nR - read\nU - update\nD - delete");

            assertEquals(resultReport.toString(), resultGenerator);
        }
    }

    @Nested
    class testFormat {

        public static final String RED = "\u001B[31m";
        public static final String WHITE = "\u001B[37m";
        public static final String RESET = "\u001B[0m";
        public static final String GREEN = "\u001B[32m";

        public static final String BOLD = "\u001B[1m";

        @Test
        void testTable() {
            String[] mass1 = {"Username", "Full name", "Email"};
            List<String[]> mass2 = new ArrayList<>();
            mass2.add(new String[]{"admin", "System Administrator", "admin@company.com"});
            mass2.add(new String[]{"john_manager", "John Smith", "john@company.com"});
            mass2.add(new String[]{"jane_analyst", "Jane Doe", "john@company.com"});
            String resMethod = FormatUtils.formatTable(mass1, mass2);

            String endRes = RED + BOLD + "+" + "-".repeat(14) + "+" + "-".repeat(22) + "+" + "-".repeat(19) + "+\n" +
                    String.format("|%-14s|%-22s|%-19s|\n", "Username", "Full name", "Email") +
                    "+" + "-".repeat(14) + "+" + "-".repeat(22) + "+" + "-".repeat(19) + "+" + RESET +
                    String.format("\n|%-14s|%-22s|%-19s|\n", "admin", "System Administrator", "admin@company.com") +
                    String.format("|%-14s|%-22s|%-19s|\n", "john_manager", "John Smith", "john@company.com") +
                    String.format("|%-14s|%-22s|%-19s|\n", "jane_analyst", "Jane Doe", "john@company.com") +
                    "+" + "-".repeat(14) + "+" + "-".repeat(22) + "+" + "-".repeat(19) + "+";

            assertEquals(endRes, resMethod);
        }

        @Test
        void testBox() {
            String text = "First line\nVary many text in this line\nsmall";
            String resMethod = FormatUtils.formatBox(text);
            String endRes =  "+" + "-".repeat(29) + "+\n" +
                    String.format("|%-29s|\n", "First line") +
                    String.format("|%-29s|\n", "Vary many text in this line") +
                    String.format("|%-29s|\n", "small") +
                    "+" + "-".repeat(29) + "+";

            assertEquals(endRes, resMethod);
        }

        @Test
        void testHeader() {
            String text = "Header text";
            String resMethod = FormatUtils.formatHeader(text);

            String endRes = "*".repeat(17) +
                    "\n*  " + text + "  *\n" +
                    "*".repeat(17);

            assertEquals(endRes, resMethod);
        }

        @Test
        void testCut() {
            String text = "Vary long text";
            String resMethod = FormatUtils.truncate(text, 10);

            String endRes = "Vary lon...";

            assertEquals(endRes, resMethod);
        }

        @Test
        void testPad() {
            String text = "text";
            String resMethod = FormatUtils.padRight(text, 10);
            String endRes = "text      ";
            assertEquals(endRes, resMethod);

            resMethod = FormatUtils.padLeft(text, 10);
            endRes = "      text";
            assertEquals(endRes, resMethod);

            resMethod = FormatUtils.padLeft(text, 3);
            endRes = "t...";
            assertEquals(endRes, resMethod);
        }
    }

    @Nested
    class dateParsetTests {

        @Test
        void getDate() {
            String date = DateUtils.getCurrentDate();

            assertEquals("2026-03-28", date);
        }

        @Test
        void testBefore() {
            boolean res = DateUtils.isBefore("2026-01-02", "2026-01-01 11:11:11");
            assertEquals(false, res);

            res = DateUtils.isBefore("2026-01-02", "2026-01-03 11:11:11");
            assertEquals(true, res);

            res = DateUtils.isBefore("2026-01-01 12:11:11", "2026-01-01 11:11:11");
            assertEquals(false, res);

            res = DateUtils.isBefore("2026-01-01 10:11:11", "2026-01-01 11:11:11");
            assertEquals(true, res);
        }

        @Test
        void testAfter() {
            boolean res = DateUtils.isAfter("2026-01-02", "2026-01-01 11:11:11");
            assertEquals(true, res);

            res = DateUtils.isAfter("2026-01-02", "2026-01-03 11:11:11");
            assertEquals(false, res);

            res = DateUtils.isAfter("2026-01-01 12:11:11", "2026-01-01 11:11:11");
            assertEquals(true, res);

            res = DateUtils.isAfter("2026-01-01 10:11:11", "2026-01-01 11:11:11");
            assertEquals(false, res);
        }

        @Test
        void testAddDays() {
            String res = DateUtils.addDays("2026-01-02", 18);
            assertEquals("2026-01-20", res);

            res = DateUtils.addDays("2026-01-02 11:11:11", 30);
            assertEquals("2026-02-02 11:11:11", res);
        }

        @Test
        void testRelative() {
            String res = DateUtils.formatRelativeTime("2026-03-03");
            assertEquals("25 days ago", res);

            res = DateUtils.formatRelativeTime("2026-03-10 11:11:11");
            assertEquals("18 days ago", res);
        }
    }

    @Nested
    class ParallelTests {

        AssignmentManager assignmentManager = new AssignmentManager();
        UserManager userManager = new UserManager();
        RoleManager roleManager = new RoleManager();

        @BeforeEach
        void initData(){
            for (User value : users) {
                userManager.add(value);
            }

            for (Role value : roles) {
                roleManager.add(value);
            }

            for (AbstractRoleAssignment value : assignmentList) {
                assignmentManager.add(value);
            }
        }

        @Test
        void testUserManager() {
            List<User> resultFilter = userManager.findByFilterParallel(UserFilters.byUsernameContains("ad"));

            List<User> mainResult = List.of(users.get(0), users.get(1));
            assertEquals(mainResult, resultFilter);
        }

        @Test
        void testRoleManager() {
            List<Role> resultFilter = roleManager.findByFilterParallel(RoleFilters.hasPermission(permissions.get(3)));
            List<Role> mainResult = List.of(roles.get(0), roles.get(2));

            List<Role> sortedFilter = resultFilter.stream().sorted(RoleSorters.byName()).toList();
            List<Role> sortedMain = mainResult.stream().sorted(RoleSorters.byName()).toList();
            assertEquals(sortedMain, sortedFilter);
        }

        @Test
        void testAssignmentManager() {
            List<RoleAssignment> resultFilter = assignmentManager.findByFilterParallel(AssignmentFilters.assignedBy("admin-report"));

            List<RoleAssignment> mainResult = List.of(assignmentList.get(3), assignmentList.get(4));
            List<RoleAssignment> sortedFilter = resultFilter.stream().sorted(AssignmentSorters.byUsername()).toList();
            List<RoleAssignment> sortedMain = mainResult.stream().sorted(AssignmentSorters.byUsername()).toList();
            assertEquals(sortedMain, sortedFilter);
        }
    }

    @Nested
    class loadTests {

        RBACSystem system;

        private static final int THREAD_COUNT = 5;
        private static final int OPERATIONS_PER_THREAD = 20;

        @BeforeEach
        void initialData() {
            system = new RBACSystem();
            system.initialize();
            system.setCurrentUser("admin");
            system.startExpiredAssignmentsCleaner(20);

            User adminUser = RBACSystem.getUserManager().findByUsername("admin").orElse(null);
            Role adminRole = RBACSystem.getRoleManager().findByName("admin").orElse(null);
            for(int i=0; i < THREAD_COUNT; i++) {
                String name = "admin" + (i + 1);
                String email = "admin" + (i + 1) + "@mail.ru";
                User user = new User(name, "User Fot Tests", email);
                RBACSystem.getUserManager().add(user);
                AssignmentMetadata metadataAdmin = AssignmentMetadata.now("admin", "Initial data");
                PermanentAssignment assignmentAdmin = new PermanentAssignment(user, adminRole, metadataAdmin);
                RBACSystem.getAssignmentManager().add(assignmentAdmin);
            }
        }

        @AfterEach
        void down() {
            system.shutdown();
        }

        @Test
        void testLoad() throws InterruptedException {
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
            AtomicInteger successCount = new AtomicInteger(0);
            List<String> allCreateUsers = new CopyOnWriteArrayList<>();

            AtomicInteger errorCount = new AtomicInteger(0);
            List<Exception> errorList = new CopyOnWriteArrayList<>();

            for (int i = 0; i < THREAD_COUNT; i++) {
                final int threadId = i;

                String name = "admin" + (i + 1);
                executor.submit(() -> {
                    system.setCurrentUser(name);
                    Random random = new Random();
                    List<String> myUsers = new ArrayList<>();

                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        try {
                            int operation = random.nextInt(6);

                            switch (operation) {
                                case 0:
                                    String username = "user_" + threadId + "_" + j + "_" + System.nanoTime();
                                    String fullName = "User " + username;
                                    String email = username + "@test.com";
                                    User newUser = new User(username, fullName, email);

                                    RBACSystem.getUserManager().add(newUser);
                                    myUsers.add(username);
                                    allCreateUsers.add(username);
                                    successCount.incrementAndGet();
                                    break;
                                case 1:
                                    if (!myUsers.isEmpty()) {
                                        String userToUpdate = myUsers.get(random.nextInt(myUsers.size()));
                                        String newName = "Updated Name";
                                        String newEmail = "updated_" + System.currentTimeMillis() + "@test.com";

                                        RBACSystem.getUserManager().update(userToUpdate, newName, newEmail);
                                    }
                                    successCount.incrementAndGet();
                                    break;
                                case 2:
                                    if (!myUsers.isEmpty()) {
                                        String userToAssign = myUsers.get(random.nextInt(myUsers.size()));
                                        Role role = RBACSystem.getRoleManager().findByName("viewer").orElse(null);
                                        User user = RBACSystem.getUserManager().findByUsername(userToAssign).orElse(null);
                                        int num = random.nextInt(2);

                                        if (num == 0) {
                                            AssignmentMetadata metadata = AssignmentMetadata.now(name, "Load test");
                                            TemporaryAssignment temp = new TemporaryAssignment(user, role, metadata);
                                            RBACSystem.getAssignmentManager().add(temp);
                                        }
                                        else {
                                            AssignmentMetadata metadata = AssignmentMetadata.now(name, "Load test");
                                            PermanentAssignment per = new PermanentAssignment(user, role, metadata);
                                            RBACSystem.getAssignmentManager().add(per);
                                        }
                                    }
                                    successCount.incrementAndGet();
                                    break;

                                case 3:
                                    List<User> filtered = RBACSystem.getUserManager().findByFilterParallel(UserFilters.byUsername(name));
                                    assertNotNull(filtered);
                                    successCount.incrementAndGet();
                                    break;

                                case 4:
                                    Optional<Role> found = RBACSystem.getRoleManager().findByName("viewer");
                                    assertTrue(found.isPresent(), "Error check have role");
                                    successCount.incrementAndGet();
                                    break;

                                case 5:
                                    User user = RBACSystem.getUserManager().findByUsername(name).orElse(null);
                                    Set<Permission> permissions = RBACSystem.getAssignmentManager().getUserPermissions(user);
                                    assertNotNull(permissions);
                                    successCount.incrementAndGet();
                                    break;
                            }

                            if (j % 3 == 0) {
                                Thread.sleep(5);
                            }

                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                            errorList.add(e);
                            System.err.println("Error in thread " + threadId + ": " + e.getMessage());
                        }
                    }
                });
            }

            executor.shutdown();
            executor.awaitTermination(90, TimeUnit.SECONDS);

            System.out.println("Complite " + successCount);
            System.out.println("Errors " + errorCount);

            if (errorCount.get() > 0) {
                System.err.println("\nExceptions occurred:");
                for (Exception e : errorList) {
                    System.err.println("  - " + e.getMessage());
                }
            }

            int allCount = THREAD_COUNT * OPERATIONS_PER_THREAD;
            int succressNum = successCount.get();
            assertEquals(allCount, succressNum);
        }
    }


}
