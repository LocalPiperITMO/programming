package app;

import com.opencsv.exceptions.CsvException;
import datatype.Vehicle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;

public class App {

    public static void main(String[] args) throws IOException, CsvException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        CSVtoStringListConverter csvToString = new CSVtoStringListConverter(reader);
        List<String[]> text = csvToString.convertCSVtoStringList();
        if (text != null) {
            StringListToObjectVectorConverter stringToVector = new StringListToObjectVectorConverter(text);
            Vector<Vehicle> dataSet = stringToVector.convertStringListToObjectVector();
        }
    }
}
