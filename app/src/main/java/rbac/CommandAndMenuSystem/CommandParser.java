package rbac.CommandAndMenuSystem;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandParser {
    private static Map<String, Command> commands = new HashMap<>();
    private static Map<String, String> commandDescriptions = new HashMap<>();

    public void registerCommand(String name, String description, Command command) {
        commands.put(name.toLowerCase(), command);
        commandDescriptions.put(name.toLowerCase(), description);
    }

    private void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        Command command = commands.get(commandName.toLowerCase());

        if (command == null)
            throw new IllegalArgumentException("Invalid command");

        command.execute(scanner, system);
    }

    public void printHelp() {
        for (Map.Entry<String, String> value : commandDescriptions.entrySet()){
            System.out.println(value.getKey() + ": " + value.getValue());
        }
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Input not be empty");
        }
        if (scanner == null){
            throw new IllegalArgumentException("Scaner not be empty");
        }
        if (system == null){
            throw new IllegalArgumentException("System not be empty");
        }

        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        if (!commands.containsKey(commandName)) {
            throw new IllegalArgumentException("Invalid command");
        }

        executeCommand(commandName, scanner, system);
    }
}
