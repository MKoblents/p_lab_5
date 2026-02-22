package inputWorkers;

import enums.MeleeWeapon;
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
            String name = reader.nextLine();
            System.out.print("Enter x coordinate: ");
            long x = Long.parseLong(reader.nextLine());
            System.out.print("Enter y coordinate: ");
            long y = Long.parseLong(reader.nextLine());
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

    public Validator getValidator() {
        return validator;
    }
}
