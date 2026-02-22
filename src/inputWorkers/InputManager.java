package inputWorkers;

import enums.MeleeWeapon;
import io.ConsoleScanner;
import io.Reader;
import manager.CollectionManager;
import manager.Coordinates;
import manager.SpaceMarine;

import javax.swing.text.html.parser.Parser;
import java.io.IOException;

public class InputManager {
    private Validator validator;
    private Reader reader;
    private String lastString; //#TODO do normal logic
    private long lastLong;
    private double lastDouble;
    private CollectionManager collectionManager;
    public InputManager(Reader reader, Validator validator, CollectionManager collectionManager){
        this.reader= reader;
        this.validator =validator;
        this.collectionManager = collectionManager;
    }
    public long getLastLong(){//#TODO
        return lastLong;
    }
    public String parseCommand() throws IOException {
        String line = reader.nextLine();
        String[] parts = line.trim().split("\\s+");
        StringBuilder key = new StringBuilder();
        try {
            lastLong = Long.parseLong(parts[parts.length-1]);
            for (int i = 0; i< parts.length-1; i++){
                key.append(parts[i]);
            }return key.toString();
        } catch (NumberFormatException e) {
            try {
                lastDouble = Double.parseDouble(parts[parts.length-1]);
                for (int i = 0; i< parts.length-1; i++){
                    key.append(parts[i]);
                }return key.toString();
            }catch (NumberFormatException e2){
                return line.replaceAll("\\s+","").toLowerCase();
            }
        }
    }

    public SpaceMarine getInputSpaceMarine(){
        try {
            System.out.print("Enter name: ");
            String name = getInputString();
            System.out.print("Enter x coordinate: ");
            long x = getInputLong();
            System.out.print("Enter y coordinate: ");
            long y = getInputLong();
            Coordinates coordinates = new Coordinates();
            coordinates.setX(x);
            coordinates.setY(y);
            System.out.print("Enter meleeWeapon: ");
            MeleeWeapon meleeWeapon = MeleeWeapon.valueOf(reader.nextLine());
            SpaceMarine spaceMarine = collectionManager.getNewSpaceMarine(name, coordinates, meleeWeapon);
            return spaceMarine;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private String getInputString() throws IOException {
        System.out.print("(you should enter String type) ");
        return reader.nextLine().trim();
    }
    private long getInputLong() throws IOException{
        System.out.print("(you should enter long type) ");
        try {
            return Long.parseLong(reader.nextLine().trim());
        }catch (NumberFormatException e){
            System.err.println("You had to enter long.");
            if (reader instanceof ConsoleScanner){
                System.out.println("Do you want to correct yor data? (Enter 'y' or 'yes'): ");
                String answer = reader.nextLine().trim();
                if (answer.equalsIgnoreCase("y")|| answer.equalsIgnoreCase("yes")){
                    return getInputLong();
                }else {
                    return 0;
                }
            }else {
                return 0;
            }
        }
    }


    public Validator getValidator() {
        return validator;
    }
}
