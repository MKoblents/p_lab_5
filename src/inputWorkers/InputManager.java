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
    private CommandParser commandParser;
    private CollectionManager collectionManager;
    public InputManager(Reader reader, Validator validator, CollectionManager collectionManager, CommandParser commandParser){
        this.reader= reader;
        this.validator =validator;
        this.collectionManager = collectionManager;
        this.commandParser = commandParser;
    }
    public long getLastLong(){
        return commandParser.getLastLong();
    }
    public String getLastPath(){
        return commandParser.getLastPath();
    }
    public double getLastDouble(){
        return commandParser.getLastDouble();
    }
    public String getLastXmlString(){
        return commandParser.getLastXmlString();
    }
    public String parseCommand() throws IOException {
        return commandParser.parseCommand(reader);
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
        return commandParser.getLastInt();
    }

    public MeleeWeapon getInputMeleeWeapon() {
        return reader.getInputMeleeWeapon();
    }
}
