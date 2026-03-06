package rbac;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import rbac.CommandAndMenuSystem.RBACSystem;
import rbac.Components.*;
import rbac.Filters.*;
import rbac.Managers.AssignmentManager;
import rbac.Managers.RoleManager;
import rbac.Managers.UserManager;
import rbac.Sorters.AssignmentSorters;
import rbac.Sorters.RoleSorters;
import rbac.Sorters.UserSorters;
import rbac.SystemValidation.ValidationUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


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
                new Permission("Write", "report", "other text"),
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
        DateTimeFormatter dataFormat = DateTimeFormatter.ofPattern("yyyy MM dd HH:mm:ss");
        TemporaryAssignment temp1 = new TemporaryAssignment(users.get(2), roles.get(2), new AssignmentMetadata("ADMIN", LocalDateTime.now().plusHours(3).format(dataFormat), "Important reason"));
        temp1.extend(LocalDateTime.now().plusHours(3).format(dataFormat));
        TemporaryAssignment temp2 = new TemporaryAssignment(users.get(3), roles.get(2), new AssignmentMetadata("admin-report", LocalDateTime.now().plusHours(1).format(dataFormat), "Important reason"));
        temp2.extend(LocalDateTime.now().plusHours(1).format(dataFormat));
        assignmentList = List.of(
                new PermanentAssignment(users.get(0), roles.get(0), metData),   // admin
                new TemporaryAssignment(users.get(1), roles.get(1), metData),   // admin-report
                temp1,   // user
                temp2,  // user
                new TemporaryAssignment(users.get(4), roles.get(3), metData2)   // guest
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

            System.out.println("Требуемый результат");
            for(User value : result){
                System.out.println(value);
            }
            System.out.println("\nДанные после фильтрации");
            for(User value : filterData){
                System.out.println(value);
            }

            assertEquals(result.size(), filterData.size(), "Размеры должны совпадать");
            assertEquals(result, filterData, "Элементы должны совпадать");

            System.out.println("Тест прошёл");
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

            System.out.println("Требуемый результат фильтрации по названию роли");
            for(Role value : result){
                System.out.println(value);
            }
            System.out.println("\nДанные после фильтрации");
            for(Role value : filterResult){
                System.out.println(value);
            }

            assertEquals(result.size(), filterResult.size(), "Количество записей должно совпадать");
            assertEquals(result, filterResult, "Записи должны совпадать");
            System.out.println("Тест фильтрации по названию прошёл");
        }

        @Test
        void testRoleFilterMoreResults(){
            RoleFilter filter = RoleFilters.byNameContains("ADMIN");
            List<Role> filterResult = roles.stream().filter(filter::test).toList();

            List<Role> result = List.of(roles.get(0), roles.get(1));

            System.out.println("Требуемый результат фильтрации по содержанию подстроки в названии роли");
            for(Role value : result){
                System.out.println(value);
            }
            System.out.println("\nДанные после фильтрации");
            for(Role value : filterResult){
                System.out.println(value);
            }

            assertEquals(result.size(), filterResult.size(), "Количество записей должно совпадать");
            assertEquals(result, filterResult, "Записи должны совпадать");
            System.out.println("Тест фильтрации по содержанию подстроки в названии роли пройден");
        }

        @Test
        void testRoleFilterPermission(){
            RoleFilter filter = RoleFilters.hasPermission(permissions.get(3));
            List<Role> filterResult = roles.stream().filter(filter::test).toList();

            List<Role> result = List.of(roles.get(0), roles.get(2));

            System.out.println("Требуемый результат фильтрации по правам доступа");
            for(Role value : result){
                System.out.println(value);
            }
            System.out.println("\nДанные после фильтрации");
            for(Role value : filterResult){
                System.out.println(value);
            }

            assertEquals(result.size(), filterResult.size(), "Количество записей должно совпадать");
            assertEquals(result, filterResult, "Записи должны совпадать");
            System.out.println("Тест фильтрации по правам доступа пройден");
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

        @Test
        void testAssigmentUser(){
            AssignmentFilter filter1 = AssignmentFilters.byUser(users.get(0));
            AssignmentFilter filter2 = AssignmentFilters.byUser(users.get(1));
            AssignmentFilter filter = filter1.or(filter2);

            List<AbstractRoleAssignment> resultFilter = assignmentList.stream().filter(filter::test).toList();
            List<AbstractRoleAssignment> result = List.of(assignmentList.get(0), assignmentList.get(1));

            System.out.println("Требуемый результат фильтрации назначенных имён" );
            for(AbstractRoleAssignment value : result){
                System.out.println(value.summary());
            }
            System.out.println("\nДанные после фильтрации");
            for(AbstractRoleAssignment value : resultFilter){
                System.out.println(value.summary());
            }

            assertEquals(result.size(), resultFilter.size(), "Количество записей должно совпадать");
            assertEquals(result, resultFilter, "Записи должны совпадать");
            System.out.println("Тест фильтрации назначенных имён пройден");
        }

        @Test
        void testAssigmentRole(){
            AssignmentFilter filter1 = AssignmentFilters.byRole(roles.get(0));
            AssignmentFilter filter2 = AssignmentFilters.byRoleName("user");
            AssignmentFilter filter = filter1.or(filter2);

            List<AbstractRoleAssignment> resultFilter = assignmentList.stream().filter(filter::test).toList();
            List<AbstractRoleAssignment> result = List.of(assignmentList.get(0), assignmentList.get(2), assignmentList.get(3));

            System.out.println("Требуемый результат фильтрации назначенных ролей" );
            for(AbstractRoleAssignment value : result){
                System.out.println(value.summary());
            }
            System.out.println("\nДанные после фильтрации");
            for(AbstractRoleAssignment value : resultFilter){
                System.out.println(value.summary());
            }

            assertEquals(result.size(), resultFilter.size(), "Количество записей должно совпадать");
            assertEquals(result, resultFilter, "Записи должны совпадать");
            System.out.println("Тест фильтрации назначенных ролей пройден");
        }

        @Test
        void testAssigmentActive(){
            AssignmentFilter filter1 = AssignmentFilters.activeOnly();
            AssignmentFilter filter2 = AssignmentFilters.inactiveOnly();

            List<AbstractRoleAssignment> resultFilter = assignmentList.stream().filter(filter1::test).toList();
            List<AbstractRoleAssignment> result = List.of(assignmentList.get(0), assignmentList.get(2), assignmentList.get(3));

            List<AbstractRoleAssignment> resultFilter2 = assignmentList.stream().filter(filter2::test).toList();
            List<AbstractRoleAssignment> result2 = List.of(assignmentList.get(1), assignmentList.get(4));

            System.out.println("\nТребуемый результат фильтрации назначенной активности (активны)" );
            for(AbstractRoleAssignment value : result){
                System.out.println(value.summary());
            }
            System.out.println("\nДанные после фильтрации (активны)");
            for(AbstractRoleAssignment value : resultFilter){
                System.out.println(value.summary());
            }
            assertEquals(result.size(), resultFilter.size(), "Количество записей должно совпадать");
            assertEquals(result, resultFilter, "Записи должны совпадать");

            System.out.println("\nТребуемый результат фильтрации назначенной активности (не активны)" );
            for(AbstractRoleAssignment value : result2){
                System.out.println(value.summary());
            }
            System.out.println("\nДанные после фильтрации (не активны)");
            for(AbstractRoleAssignment value : resultFilter2){
                System.out.println(value.summary());
            }
            assertEquals(result.size(), resultFilter.size(), "Количество записей должно совпадать");
            assertEquals(result, resultFilter, "Записи должны совпадать");
            System.out.println("Тест фильтрации назначенной активности пройден");
        }

        @Test
        void testAssigmentByType(){
            AssignmentFilter filter1 = AssignmentFilters.byType("PERMANENT");
            AssignmentFilter filter2 = AssignmentFilters.byType("TEMpoRARY");

            List<AbstractRoleAssignment> resultFilter = assignmentList.stream().filter(filter1::test).toList();
            List<AbstractRoleAssignment> result = List.of(assignmentList.get(0));

            List<AbstractRoleAssignment> resultFilter2 = assignmentList.stream().filter(filter2::test).toList();
            List<AbstractRoleAssignment> result2 = List.of(assignmentList.get(1),assignmentList.get(2), assignmentList.get(3), assignmentList.get(4));

            System.out.println("\nТребуемый результат фильтрации назначенного типа (PERMANENT)" );
            for(AbstractRoleAssignment value : result){
                System.out.println(value.summary());
            }
            System.out.println("\nДанные после фильтрации (PERMANENT)");
            for(AbstractRoleAssignment value : resultFilter){
                System.out.println(value.summary());
            }
            assertEquals(result.size(), resultFilter.size(), "Количество записей должно совпадать");
            assertEquals(result, resultFilter, "Записи должны совпадать");

            System.out.println("\nТребуемый результат фильтрации назначенного типа (TEMPORARY)" );
            for(AbstractRoleAssignment value : result2){
                System.out.println(value.summary());
            }
            System.out.println("\nДанные после фильтрации (TEMPORARY)");
            for(AbstractRoleAssignment value : resultFilter2){
                System.out.println(value.summary());
            }
            assertEquals(result.size(), resultFilter.size(), "Количество записей должно совпадать");
            assertEquals(result, resultFilter, "Записи должны совпадать");

            System.out.println("Тест фильтрации назначенного типа пройден");
        }

        @Test
        void testAssigmentWhoAssigmenting(){
            AssignmentFilter filter = AssignmentFilters.assignedBy("adMin-Report");

            List<AbstractRoleAssignment> resultFilter = assignmentList.stream().filter(filter::test).toList();
            List<AbstractRoleAssignment> result = List.of(assignmentList.get(3), assignmentList.get(4));

            System.out.println("\nТребуемый результат фильтрации автора назначения" );
            for(AbstractRoleAssignment value : result){
                System.out.println(value.summary());
            }
            System.out.println("\nДанные после фильтрации");
            for(AbstractRoleAssignment value : resultFilter){
                System.out.println(value.summary());
            }
            assertEquals(result.size(), resultFilter.size(), "Количество записей должно совпадать");
            assertEquals(result, resultFilter, "Записи должны совпадать");

            System.out.println("Тест фильтрации автора назначения пройден");
        }

        @Test
        void testAssigmentAfterAssigmenting(){
            DateTimeFormatter dataFormat = DateTimeFormatter.ofPattern("yyyy MM dd HH:mm:ss");
            AssignmentFilter filter = AssignmentFilters.assignedAfter(LocalDateTime.now().plusHours(2).format(dataFormat));

            List<AbstractRoleAssignment> resultFilter = assignmentList.stream().filter(filter::test).toList();
            List<AbstractRoleAssignment> result = List.of(assignmentList.get(2));

            System.out.println("\nТребуемый результат фильтрации по времени назначения (после даты)" );
            for(AbstractRoleAssignment value : result){
                System.out.println(value.summary());
            }
            System.out.println("\nДанные после фильтрации");
            for(AbstractRoleAssignment value : resultFilter){
                System.out.println(value.summary());
            }
            assertEquals(result.size(), resultFilter.size(), "Количество записей должно совпадать");
            assertEquals(result, resultFilter, "Записи должны совпадать");

            System.out.println("Тест фильтрации по времени назначения пройден");
        }

        @Test
        void testAssigmentBeforeAssigmenting(){
            DateTimeFormatter dataFormat = DateTimeFormatter.ofPattern("yyyy MM dd HH:mm:ss");
            AssignmentFilter filter = AssignmentFilters.expiringBefore(LocalDateTime.now().plusHours(2).format(dataFormat));

            List<AbstractRoleAssignment> resultFilter = assignmentList.stream().filter(filter::test).toList();
            List<AbstractRoleAssignment> result = List.of(assignmentList.get(1), assignmentList.get(3), assignmentList.get(4));

            System.out.println("\nТребуемый результат фильтрации по времени назначения (до даты)" );
            for(AbstractRoleAssignment value : result){
                System.out.println(value.summary());
            }
            System.out.println("\nДанные после фильтрации");
            for(AbstractRoleAssignment value : resultFilter){
                System.out.println(value.summary());
            }
            assertEquals(result.size(), resultFilter.size(), "Количество записей должно совпадать");
            assertEquals(result, resultFilter, "Записи должны совпадать");

            System.out.println("Тест фильтрации по времени назначения пройден");
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
            List<Role> result = List.of(roles.get(1), roles.get(0));

            for (Role value : result)
                System.out.println(value.toString());

            assertEquals(result.size(), roleList.size(), "Размеры должны совпадать");
            assertEquals(result, roleList);
        }

        @Test
        void testAssigmentsManager() {
            AssignmentManager assignmentManager = new AssignmentManager();
            assignmentManager.add(assignmentList.get(0));
            assignmentManager.add(assignmentList.get(1));
            assignmentManager.add(assignmentList.get(2));
            assignmentManager.add(assignmentList.get(3));
            assignmentManager.add(assignmentList.get(4));

            List<RoleAssignment> activeManager = assignmentManager.getActiveAssignments(); // сортируем по нику, иначе порядок рандомный
            List<RoleAssignment> activeResult = List.of(assignmentList.get(0), assignmentList.get(3), assignmentList.get(2));
            assertEquals(activeResult.size(), activeManager.size(), "Размеры должны совпадать");
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
            assertEquals(true, ValidationUtils.isValidDate("2026 03 06 11:11:00"));
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

}
