package commands;

import datatype.Vehicle;
import exceptions.EmptyDatasetException;
import pattern.Command;
import pattern.Receiver;

import java.util.Collections;


public class ReorderCommand implements Command {
    private final Receiver receiver;

    public ReorderCommand(Receiver receiver) {
        this.receiver = receiver;
    }

    public void execute(String arg) {
        try {
            if (receiver.dataSet().size() == 0) {
                throw new EmptyDatasetException();
            }
            Collections.reverse(receiver.dataSet());
            for (Vehicle vehicle : receiver.dataSet()) {
                System.out.println(vehicle.toString());
            }
        } catch (EmptyDatasetException noData) {
            System.out.println("Dataset is empty: nothing to sort");
        }
    }
}
