package user;


import collection.CollectionStorage;
import commands.*;
import datatype.Vehicle;
import exceptions.BuildingInterruptionException;
import exceptions.InvalidCommandNameException;
import exceptions.NoArgumentException;
import receivers.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

/**
 * Invoker class
 * Used for processing user inputs and sending commands
 * Stores commandHashMap used for accessing commands
 */
public class Invoker {
    private final HashMap<String, Command> commandHashMap;
    private final HashSet<String> complexCommandSet;
    private String argument;
    private String commandName;
    private final TextReceiver textReceiver;
    private final ManualBuildingReceiver manualBuildingReceiver;
    private final CollectionModifyingCommandReceiver collectionModifyingCommandReceiver;

    /**
     * Initializes map of commands
     * Creates command receivers, that are send to corresponding commands
     * Crates text receiver for output purposes
     * @param storage contains the collection
     */
    public Invoker(CollectionStorage storage) {
        this.commandHashMap = new HashMap<>();
        this.complexCommandSet = new HashSet<>();

        DisplayingCommandReceiver displayingCommandReceiver = new DisplayingCommandReceiver(storage, this);
        SortingCommandReceiver sortingCommandReceiver = new SortingCommandReceiver(storage);
        CollectionProcessingCommandReceiver collectionProcessingCommandReceiver = new CollectionProcessingCommandReceiver(storage);
        SimpleArgumentCommandReceiver simpleArgumentCommandReceiver = new SimpleArgumentCommandReceiver(storage);
        this.textReceiver = new TextReceiver();
        ScriptBuildingReceiver scriptBuildingReceiver = new ScriptBuildingReceiver(storage.getIdGenerator(), this.textReceiver);
        this.manualBuildingReceiver = new ManualBuildingReceiver(storage.getIdGenerator(), this.textReceiver);
        this.collectionModifyingCommandReceiver = new CollectionModifyingCommandReceiver(storage);
        ExecuteScriptCommandReceiver executeScriptCommandReceiver = new ExecuteScriptCommandReceiver(collectionModifyingCommandReceiver, scriptBuildingReceiver, this);

        complexCommandSet.add("add");
        complexCommandSet.add("update");
        complexCommandSet.add("add_if_max");
        complexCommandSet.add("remove_greater");

        commandHashMap.put("help", new HelpCommand(displayingCommandReceiver));
        commandHashMap.put("info", new InfoCommand(displayingCommandReceiver));
        commandHashMap.put("show", new ShowCommand(displayingCommandReceiver));
        commandHashMap.put("add", new AddElementCommand(collectionModifyingCommandReceiver));
        commandHashMap.put("update", new UpdateElementCommand(collectionModifyingCommandReceiver));
        commandHashMap.put("remove_by_id", new RemoveByIDCommand(simpleArgumentCommandReceiver));
        commandHashMap.put("clear", new ClearCommand(collectionProcessingCommandReceiver));
        commandHashMap.put("save", new SaveCommand(collectionProcessingCommandReceiver));
        commandHashMap.put("execute_script", new ExecuteScriptCommand(executeScriptCommandReceiver));
        commandHashMap.put("add_if_max", new AddIfMaxElementCommand(collectionModifyingCommandReceiver));
        commandHashMap.put("remove_greater", new RemoveGreaterElementsCommand(collectionModifyingCommandReceiver));
        commandHashMap.put("reorder", new ReorderCommand(sortingCommandReceiver));
        commandHashMap.put("filter_by_fuel_consumption", new FilterByFuelConsumptionCommand(simpleArgumentCommandReceiver));
        commandHashMap.put("print_ascending", new PrintAscendingCommand(sortingCommandReceiver));
        commandHashMap.put("print_field_ascending_fuel_type", new PrintFieldAscendingFuelTypeCommand(sortingCommandReceiver));
    }

    /**
     * @return map of commands
     */
    public HashMap<String, Command> getCommandHashMap() {
        return commandHashMap;
    }

    /**
     * Gets user input, preprocesses it, gets the command name and calls the command if it matches user input.
     *
     * @param userInput whatever user writes
     */
    public void workWithUser(String userInput) {
        try {
            setCommandNameAndArgument(readRequest(userInput));
            if (checkIfComplexCommand()) {
                if (checkIfUpdate()) {
                    performActionsIfUpdate();
                } else {
                    askUserToInputArgumentsAndSetBuiltVehicle();
                    textReceiver.print(callCommandAndReturnExecutionReport(findCommandElseThrowError()));
                }
            } else {
                textReceiver.print(callCommandAndReturnExecutionReport(findCommandElseThrowError()));
            }
        } catch (InvalidCommandNameException e) {
            if (commandName.length() != 0) {
                textReceiver.print("There is no command named \"" + commandName + "\". Try again");
            }
        } catch (BuildingInterruptionException e) {
            textReceiver.print("User terminated building process");
        }
    }

    private String[] readRequest(String userInput) {
        return userInput.split(" ", 2);
    }

    private void setCommandNameAndArgument(String[] request) {
        commandName = request[0];
        argument = (request.length == 2) ? request[1] : "";
    }

    private Command findCommandElseThrowError() throws InvalidCommandNameException {
        Command command = commandHashMap.get(commandName);
        if (command != null) {
            return command;
        }
        throw new InvalidCommandNameException();
    }

    private boolean checkIfComplexCommand() {
        return complexCommandSet.contains(commandName);
    }

    private boolean checkIfUpdate() {
        return Objects.equals(commandName, "update");
    }

    private void performActionsIfUpdate() throws BuildingInterruptionException {
        UpdateElementCommand command = (UpdateElementCommand) commandHashMap.get("update");
        if (command.checkIfValidID(argument)) {
            askUserToInputArgumentsAndSetBuiltVehicle();
            textReceiver.print(callCommandAndReturnExecutionReport(commandHashMap.get("update")));
        }
    }

    private void askUserToInputArgumentsAndSetBuiltVehicle() throws BuildingInterruptionException {
        Vehicle vehicle = manualBuildingReceiver.build();
        collectionModifyingCommandReceiver.setCurrentVehicle(vehicle);
    }

    private String callCommandAndReturnExecutionReport(Command command) {
        try {
            return command.execute(argument);
        } catch (IOException e) {
            return "Error!";
        } catch (NoArgumentException e) {
            return commandName + " requires an argument: none were given";
        } catch (NumberFormatException e) {
            return commandName + " requires a different argument type, but " + argument.getClass().getSimpleName() + " was given";
        }
    }

}
