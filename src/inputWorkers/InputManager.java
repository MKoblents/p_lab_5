package inputWorkers;

import enums.AstartesCategory;
import enums.MeleeWeapon;
import enums.Weapon;
import io.ConsoleScanner;
import io.Reader;
import manager.Chapter;
import manager.CollectionManager;
import manager.Coordinates;
import manager.SpaceMarine;

import java.io.IOException;
import java.util.Arrays;

public class InputManager {
    private Validator validator;
    private Reader reader;
    private String lastString; //#TODO do normal logic
    private long lastLong;
    private double lastDouble;
    private int lastInt;
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
                lastDouble = Double.parseDouble(parts[parts.length - 1]);
                for (int i = 0; i < parts.length - 1; i++) {
                    key.append(parts[i]);
                }
                return key.toString();
            } catch (NumberFormatException e2) {
                try {
                    lastInt = Integer.parseInt(parts[parts.length - 1]);
                    for (int i = 0; i < parts.length - 1; i++) {
                        key.append(parts[i]);
                    }
                    return key.toString();
                } catch (NumberFormatException e3) {
                    return line.replaceAll("\\s+", "").toLowerCase();
                }
            }
        }
    }

    public SpaceMarine getInputSpaceMarine(){
        SpaceMarine spaceMarine = collectionManager.getNewSpaceMarine();
        return reader.getInputSpaceMarine(spaceMarine);
    }

    private Chapter getInputChapter() throws IOException {
        return reader.getInputChapter();
    }

    private double getInputDouble() throws IOException {
        return reader.getInputDouble();
    }

    private <T extends Enum<T>> T getInputEnum(Class<T> enumType) throws IOException {
        return reader.getInputEnum(enumType);
    }

    private String getTrimmedText() throws IOException {
        return reader.getTrimmedText();
    }
//    private boolean shouldRetryInput() throws IOException {
//        if (!(reader instanceof ConsoleScanner)) {
//            return false;
//        }
//        System.out.println("Do you want to correct your data? (Enter 'y' or 'yes'): ");
//        String answer = getTrimmedText();
//        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
//    }

    private String getInputString() throws IOException {
        return reader.getInputString();
    }
    private long getInputLong() throws IOException{
        return reader.getInputLong();
    }


    public Validator getValidator() {
        return validator;
    }

    public int getLastInt() {
        return lastInt;
    }

    public MeleeWeapon getInputMeleeWeapon() {
        return reader.getInputMeleeWeapon();
    }
}
