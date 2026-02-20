package rbac;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


class AppTest {
    List<User> users;

    @BeforeEach
    void getData(){
        users = List.of(
                new User("admin", "Главный Админ", "admin@company.com"),
                new User("admin2", "Второй Админ", "john@gmail.com"),
                new User("maria", "Марина Семеновна", "maria@company.com"),
                new User("guest", "Гость", "guest@mail.ru"),
                new User("SERGEY", "Сергей Сергеевич", "super@mail.ru")
        );
    }

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
