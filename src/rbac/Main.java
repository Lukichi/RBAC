package rbac;

public class Main {
    public static void main(){

        User first = User.validate("test1", "Testor First", "test@mail.ru");
        System.out.println( first.format());

//        User second = User.validate("test@", "Testor 2", "test.@test@mail.ru");
//        System.out.println( second.format());

//        User thirt = User.validate("test", "Testor Fi2rst", "test.test@mail.com");
//        System.out.println( thirt.format());

    }
}