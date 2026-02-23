package io;

import enums.AstartesCategory;
import enums.MeleeWeapon;
import enums.Weapon;
import manager.Chapter;
import manager.Coordinates;
import manager.SpaceMarine;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class ConsoleScanner implements Reader{
    private final Scanner scanner;
    //#TODO currentLine field
    public ConsoleScanner(){
        this.scanner = new Scanner(System.in);
    }

    @Override
    public String nextLine() throws IOException {
        return scanner.nextLine();
    }

    @Override
    public boolean hasNextLine()throws IOException{
        return scanner.hasNextLine();
    }

    @Override
    public SpaceMarine getInputSpaceMarine(SpaceMarine spaceMarine) {
        try {
            System.out.print("Enter name: ");
            String name = getInputString();
            spaceMarine.setName(name);
            System.out.print("Enter x coordinate: ");
            long x = getInputLong();
            System.out.print("Enter y coordinate: ");
            long y = getInputLong();
            Coordinates coordinates = new Coordinates();
            coordinates.setX(x);
            coordinates.setY(y);
            spaceMarine.setCoordinates(coordinates);
            System.out.print("Enter meleeWeapon: ");
            MeleeWeapon meleeWeapon = getInputEnum(MeleeWeapon.class);
            spaceMarine.setMeleeWeapon(meleeWeapon);
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

    public long getInputLong() throws IOException{
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

    public String getInputString() throws IOException {
        System.out.print("(you should enter String type) ");
        return getTrimmedText();
    }
    private boolean shouldRetryInput() throws IOException {
        System.out.println("Do you want to correct your data? (Enter 'y' or 'yes'): ");
        String answer = getTrimmedText();
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }

    public String getTrimmedText() throws IOException {
        return nextLine().trim();
    }
//    @Override
    public <T extends Enum<T>> T getInputEnum(Class<T> enumType) throws IOException {
        System.out.println("(you should chose one option) ");
        System.out.println(Arrays.toString(enumType.getEnumConstants()));
        String value = getTrimmedText();
        try {
            return Enum.valueOf(enumType, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            if (shouldRetryInput()){
                return getInputEnum(enumType);
            }
            return null;
        }
    }
    public double getInputDouble() throws IOException {
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
    public Chapter getInputChapter() throws IOException {
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
    public MeleeWeapon getInputMeleeWeapon() {
        try {
            MeleeWeapon meleeWeapon = getInputEnum(MeleeWeapon.class);
            if (meleeWeapon == null) {
                System.out.println("You have to chose one option. If you want chose default version, press enter.");
                if (shouldRetryInput()) {
                    return getInputMeleeWeapon();
                }
                return MeleeWeapon.CHAIN_AXE;
            }
            return meleeWeapon;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setLastXmlString(String lastXmlString) {

    }
}
