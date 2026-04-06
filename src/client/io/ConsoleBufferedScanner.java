package client.io;

import client.inputWorkers.ConsoleInputReader;
import shared.enums.AstartesCategory;
import shared.enums.MeleeWeapon;
import shared.enums.Weapon;
import shared.models.Chapter;
import shared.models.Coordinates;
import shared.models.SpaceMarine;

import java.io.IOException;
import java.util.Arrays;

public class ConsoleBufferedScanner implements Reader {
    private final ConsoleInputReader consoleInputReader = new ConsoleInputReader();

    public ConsoleBufferedScanner() {
        Thread t = new Thread(consoleInputReader);
        t.setDaemon(true);
        t.start();
    }

    @Override
    public String nextLine() throws IOException {
        try {
            return consoleInputReader.pollCommand();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    private String readLineBlocking() throws IOException {
        try {
            return consoleInputReader.takeCommand();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Input interrupted", e);
        }
    }

    private String getTrimmedText() throws IOException {
        String line = nextLine();
        return line != null ? line.trim() : "";
    }

    private String getTrimmedTextBlocking() throws IOException {
        String line = readLineBlocking();
        return line != null ? line.trim() : "";
    }

    @Override
    public boolean hasNextLine() throws IOException {
        return true;
    }

    @Override
    public void clearBuffer() throws IOException {
    }

    @Override
    public SpaceMarine getInputSpaceMarine() {
        try {
            SpaceMarine spaceMarine = new SpaceMarine();
            System.out.print("Enter name: ");
            spaceMarine.setName(getInputString());

            System.out.print("Enter x coordinate: ");
            long x = getInputLong();
            System.out.print("Enter y coordinate: ");
            long y = getInputLong();
            Coordinates coordinates = new Coordinates();
            coordinates.setX(x);
            coordinates.setY(y);
            spaceMarine.setCoordinates(coordinates);

            System.out.print("Enter meleeWeapon: ");
            spaceMarine.setMeleeWeapon(getInputEnum(MeleeWeapon.class));

            System.out.print("Enter health: ");
            spaceMarine.setHealth(getInputDouble());

            System.out.print("Enter weapon: ");
            spaceMarine.setWeaponType(getInputEnum(Weapon.class));

            System.out.print("Enter AstartesCategory: ");
            spaceMarine.setCategory(getInputEnum(AstartesCategory.class));

            System.out.println("Enter Chapter:");
            spaceMarine.setChapter(getInputChapter());
            return spaceMarine;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public long getInputLong() throws IOException {
        System.out.print("(you should enter long type) ");
        try {
            return Long.parseLong(getTrimmedTextBlocking());
        } catch (NumberFormatException e) {
            System.err.println("You had to enter long.");
            if (shouldRetryInput()) return getInputLong();
            return 0;
        }
    }

    public String getInputString() throws IOException {
        System.out.print("(you should enter String type) ");
        return getTrimmedTextBlocking();
    }

    private boolean shouldRetryInput() throws IOException {
        System.out.print("Do you want to correct your data? (Enter 'y' or 'yes'): ");
        String answer = getTrimmedTextBlocking();
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }

    public <T extends Enum<T>> T getInputEnum(Class<T> enumType) throws IOException {
        System.out.println("(you should chose one option) ");
        System.out.println(Arrays.toString(enumType.getEnumConstants()));
        String value = getTrimmedTextBlocking();
        T[] constants = enumType.getEnumConstants();
        try {
            int index = Integer.parseInt(value);
            if (index >= 1 && index <= constants.length) {
                return constants[index - 1];
            }
        } catch (NumberFormatException ignored) {}
        try {
            return Enum.valueOf(enumType, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            if (shouldRetryInput()) return getInputEnum(enumType);
            return null;
        }
    }

    public double getInputDouble() throws IOException {
        System.out.print("(you should enter double type) ");
        try {
            return Double.parseDouble(getTrimmedTextBlocking().replace(',', '.'));
        } catch (NumberFormatException e) {
            System.err.println("You had to enter double.");
            if (shouldRetryInput()) return getInputDouble();
            return 0.0;
        }
    }

    public Chapter getInputChapter() throws IOException {
        System.out.print("Enter name: ");
        String name = getInputString();
        System.out.print("Enter parentLegion: ");
        String parentLegion = getInputString();
        System.out.print("Enter world: ");
        String world = getInputString();
        if (name.isEmpty() && parentLegion.isEmpty() && world.isEmpty()) return null;
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
                if (shouldRetryInput()) return getInputMeleeWeapon();
                return MeleeWeapon.CHAIN_AXE;
            }
            return meleeWeapon;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setLastXmlString(String lastXmlString) {}
}