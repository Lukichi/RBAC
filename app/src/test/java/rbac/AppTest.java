package rbac;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;


class AppTest {
    private List<User> users;
    private List<Permission> permissions;
    private List<Role> roles;

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
            System.out.println("Тест фильтрации по содержанию подстроки в названии роли");
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
            System.out.println("Тест фильтрации по правам доступа");
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
            System.out.println("Тест фильтрации по нескольким признакам");
        }
    }
}
