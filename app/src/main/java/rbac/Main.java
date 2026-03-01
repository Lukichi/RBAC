package rbac;

import rbac.CommandAndMenuSystem.CommandParser;
import rbac.CommandAndMenuSystem.CommandRegistry;
import rbac.CommandAndMenuSystem.RBACSystem;

import java.util.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(){

//        User first = User.validate("test1", "Testor First", "test@mail.ru");
//        System.out.println( first.format());

//        User second = User.validate("test@", "Testor 2", "test.@test@mail.ru");
//        System.out.println( second.format());

//        User thirt = User.validate("test", "Testor Fi2rst", "test.test@mail.com");
//        System.out.println( thirt.format());

//        ==============================================================================

//        List<Permission> listPerm = List.of(
//            new Permission("wrITE", "report", "other test text for premission"),
//            new Permission("READ", "report", "222"),
////            new Permission("read", "report", ""),
////            new Permission("read", "report 2", "dfdf"),
//            new Permission("read", "users", "other test text for premission"),
//            new Permission("read", "report", "test")
//        );
//
//        for(Permission value : listPerm){
//            System.out.println(value.format());
//            if (value.matches("rEAd", "report"))
//                System.out.println("    HAVE TEST COMPLITE");
//        }

//        =============================================================================

//        Permission help1 = new Permission("READ", "users", "Read users");
//        Permission help2 = new Permission("WRITE", "users", "Write users");
//
//        Role role1 = new Role("admin", "Description for admin");
//        role1.addPermission(help1);
//        role1.addPermission(help2);
//
//        System.out.println(role1.toString());
//
//        Set<Permission> permissions = new HashSet<>();
//        permissions.add(new Permission("READ", "testers", "Read users"));
//        permissions.add(new Permission("ReaD", "testers", "Read users"));
//        permissions.add(new Permission("Write", "report", "other text"));
//        permissions.add(new Permission("READ", "tester", "text read"));
//
//        Role role2 = new Role("admin2", "Description for admin", permissions);
//
//        System.out.println(role2.toString());

//        ===========================================================================================

//        AssignmentMetadata metData1 = AssignmentMetadata.now("User1", null);
//        System.out.println(metData1.format());
//
//        AssignmentMetadata metData2 = AssignmentMetadata.now("User1", "Important reason");
//        System.out.println(metData2.format());

//        AssignmentMetadata metData3 = AssignmentMetadata.now(null, "Important reason");
//        System.out.println(metData3.format());

//        ===================================================

//        User user = User.validate("test1", "Tester First", "test@mail.ru");
//
//        Set<Permission> permissions = new HashSet<>();
//        permissions.add(new Permission("READ", "testers", "Read users"));
//        permissions.add(new Permission("ReaD", "testers", "Read users"));
//        permissions.add(new Permission("Write", "report", "other text"));
//        permissions.add(new Permission("READ", "tester", "text read"));
//        Role role = new Role("admin2", "Description for admin", permissions);
//
//        AssignmentMetadata metData = AssignmentMetadata.now("ADMIN", "Important reason");
//
//        PermanentAssignment assignment = new PermanentAssignment(user, role, metData);
//        System.out.println(assignment.summary());
//
//        PermanentAssignment assignment2 = new PermanentAssignment(user, role, metData);
//        assignment2.revoke();
//        System.out.println(assignment2.summary());

//        =======================================================

//        DateTimeFormatter dataFormat = DateTimeFormatter.ofPattern("yyyy MM dd HH:mm:ss");
//
//        User user = User.validate("test1", "Tester First", "test@mail.ru");
//
//        Set<Permission> permissions = new HashSet<>();
//        permissions.add(new Permission("READ", "testers", "Read users"));
//        permissions.add(new Permission("ReaD", "testers", "Read users"));
//        permissions.add(new Permission("Write", "report", "other text"));
//        permissions.add(new Permission("READ", "tester", "text read"));
//        Role role = new Role("admin2", "Description for admin", permissions);
//
//        AssignmentMetadata metData = AssignmentMetadata.now("ADMIN", "Important reason");
//
//        String date1 = LocalDateTime.now().minusDays(19).format(dataFormat);
//        TemporaryAssignment assignment = new TemporaryAssignment(user, role, metData, date1, false);
//        System.out.println(assignment.summary());
//
//        System.out.println();
//
//        String date2 = LocalDateTime.now().format(dataFormat);
//        TemporaryAssignment assignment2 = new TemporaryAssignment(user, role, metData, date2, true);
//        System.out.println(assignment2.summary());
//
//        System.out.println();
//        String date3 = LocalDateTime.now().format(dataFormat);
//        TemporaryAssignment assignment3 = new TemporaryAssignment(user, role, metData, date3, false);
//
//        System.out.println(assignment3.summary());
////        assignment3.extend(LocalDateTime.now().minusHours(22).format(dataFormat));
//        assignment3.extend(LocalDateTime.now().plusHours(2).format(dataFormat));
//        System.out.println(assignment3.summary());

//        =====================================================================

        RBACSystem system = new RBACSystem();
//        system.initialize();
//        system.setCurrentUser("admin");
        CommandParser parser = new CommandParser();
        CommandRegistry.registerCommands(parser);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nInput command:");
            String command = scanner.nextLine();

            parser.parseAndExecute(command, scanner, system);
        }

    }
}