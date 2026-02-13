package rbac;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(){

//        User first = User.validate("test1", "Testor First", "test@mail.ru");
//        System.out.println( first.format());

//        User second = User.validate("test@", "Testor 2", "test.@test@mail.ru");
//        System.out.println( second.format());

//        User thirt = User.validate("test", "Testor Fi2rst", "test.test@mail.com");
//        System.out.println( thirt.format());

//        ==============================================================================

        List<Permission> listPerm = List.of(
            new Permission("wrITE", "report", "other test text for premission"),
            new Permission("READ", "report", "222"),
//            new Permission("read", "report", ""),
//            new Permission("read", "report 2", "dfdf"),
            new Permission("read", "users", "other test text for premission"),
            new Permission("read", "report", "test")
        );

        for(Permission value : listPerm){
            System.out.println(value.format());
            if (value.matches("rEAd", "report"))
                System.out.println("    HAVE TEST COMPLITE");
        }

    }
}