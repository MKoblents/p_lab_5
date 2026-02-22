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
            MeleeWeapon meleeWeapon = getInputEnum(MeleeWeapon.class);
            SpaceMarine spaceMarine = collectionManager.getNewSpaceMarine(name, coordinates, meleeWeapon);
            System.out.print("Enter health: ");
            double health = getInputDouble();
            spaceMarine.setHealth(health);
            System.out.print("Enter weapon: ");
            Weapon weapon = getInputEnum(Weapon.class);
            spaceMarine.setWeaponType(weapon);
            System.out.print("Enter AstartesCategory: ");
            AstartesCategory astartesCategory = getInputEnum(AstartesCategory.class);
            spaceMarine.setCategory(astartesCategory);
            System.out.println("Enter Chapter:");
            Chapter chapter = getInputChapter();
            spaceMarine.setChapter(chapter);
            return spaceMarine;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Chapter getInputChapter() throws IOException {
        System.out.print("Enter name: ");
        String name = getInputString();
        System.out.print("Enter parentLegion: ");
        String parentLegion = getInputString();
        System.out.print("Enter world: ");
        String world = getInputString();
        if (name.isEmpty() && parentLegion.isEmpty() && world.isEmpty()){
            return null;
        }
        Chapter chapter = new Chapter();
        chapter.setName(name);
        chapter.setWorld(world);
        chapter.setParentLegion(parentLegion);
        return chapter;
    }

    private double getInputDouble() throws IOException {
        System.out.print("(you should enter double type) ");
        try {
            return Double.parseDouble(getTrimmedText());
        }catch (NumberFormatException e){
            System.err.println("You had to enter long.");
            if (shouldRetryInput()){
                return getInputDouble();
            }return 0.0;
        }
    }

    private <T extends Enum<T>> T getInputEnum(Class<T> enumType) throws IOException {
        System.out.println("(you should chose one option) ");
        System.out.println(Arrays.toString(enumType.getEnumConstants()));
        String value = getTrimmedText();
        if (value != null && !value.isEmpty()) {
            try {
                return Enum.valueOf(enumType, value.toUpperCase());
            } catch (IllegalArgumentException e) {
                if (shouldRetryInput()){
                    return getInputEnum(enumType);
                }
                return null;
            }
        }return null;
    }

    private String getTrimmedText() throws IOException {
        return reader.nextLine().trim();
    }
    private boolean shouldRetryInput() throws IOException {
        if (!(reader instanceof ConsoleScanner)) {
            return false;
        }
        System.out.println("Do you want to correct your data? (Enter 'y' or 'yes'): ");
        String answer = getTrimmedText();
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }

    private String getInputString() throws IOException {
        System.out.print("(you should enter String type) ");
        return getTrimmedText();
    }
    private long getInputLong() throws IOException{
        System.out.print("(you should enter long type) ");
        try {
            return Long.parseLong(getTrimmedText());
        }catch (NumberFormatException e){
            System.err.println("You had to enter long.");
            if (shouldRetryInput()){
                return getInputLong();
            }return 0;
        }
    }


    public Validator getValidator() {
        return validator;
    }
}
