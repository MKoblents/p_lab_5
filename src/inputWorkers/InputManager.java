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
        return commandParser.getLongArg();
    }
    public String getLastPath(){
        return commandParser.getPathArg();
    }
    public double getLastDouble(){
        return commandParser.getDoubleArg();
    }
    public String getLastXmlString(){
        return commandParser.getXmlArg();
    }
    public String parseCommand() throws IOException {
        commandParser.parse(reader);
        return commandParser.getCommandName();
    }

    public SpaceMarine getInputSpaceMarine(){
        SpaceMarine spaceMarine = collectionManager.getNewSpaceMarine();
        return reader.getInputSpaceMarine(spaceMarine);
    }



    public Validator getValidator() {
        return validator;
    }

    public int getLastInt() {
        return commandParser.getIntArg();
    }

    public MeleeWeapon getInputMeleeWeapon() {
        return commandParser.getEnumValue(MeleeWeapon.class);
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }

    public Reader getReader() {
        return reader;
    }

    public String getRawArgument() {
        return commandParser.getRawArgument();
    }
}
