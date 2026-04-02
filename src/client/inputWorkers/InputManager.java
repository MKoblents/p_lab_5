package client.inputWorkers;

import shared.enums.MeleeWeapon;
import client.io.Reader;
import shared.models.SpaceMarine;

import java.io.IOException;
/**
 * Facade for input operations: delegates to Reader, Validator, and CommandParser.
 * Provides unified access to parsed command arguments and SpaceMarine input.
 */
public class InputManager {
    /** Reader delegate for abstracted input source (console/file). */
    private Reader reader;
    /** CommandParser delegate for tokenizing and extracting arguments. */
    private CommandParser commandParser;
    /**
     * Initializes manager with required dependencies via constructor injection.
     * @param reader input source abstraction
     * @param commandParser argument extraction helper
     */
    public InputManager(Reader reader, CommandParser commandParser){
        this.reader= reader;
        this.commandParser = commandParser;
    }
    public String getTargetClientId(){
        return commandParser.getTargetClientId();
    }
    /**
     * Returns the last parsed long argument from command input.
     * @return long value (0 if not set or parse failed)
     */
    public long getLastLong(){
        return commandParser.getLongArg();
    }

    /**
     * Returns the last parsed file path argument.
     * @return path string or null if not provided
     */
    public String getLastPath(){
        return commandParser.getPathArg();
    }
    /**
     * Returns the last parsed double argument.
     * @return double value (0.0 if not set or parse failed)
     */
    public double getLastDouble(){
        return commandParser.getDoubleArg();
    }

    /**
     * Returns the last cached XML string argument (for add/update commands).
     * @return XML fragment or null if not set
     */
    public String getLastXmlString(){
        return commandParser.getXmlArg();
    }
    /**
     * Parses next command from current Reader and extracts its name.
     * @return lowercase command name, or null if input was empty/comment
     * @throws IOException if read operation fails
     */
    public String parseCommand() throws IOException {
        commandParser.parse(reader);
        return commandParser.getCommandName();
    }
    /**
     * Creates new SpaceMarine via manager and populates it via Reader.
     * @return fully populated SpaceMarine instance
     */
    public SpaceMarine getInputSpaceMarine(){
        return reader.getInputSpaceMarine();
    }
    /**
     * Returns the last parsed int argument.
     * @return int value (0 if not set or parse failed)
     */
    public int getLastInt() {
        return commandParser.getIntArg();
    }
    /**
     * Returns parsed MeleeWeapon enum from command argument.
     * @return enum value or null if invalid/not provided
     */
    public MeleeWeapon getInputMeleeWeapon() {
        System.out.println("2");
        System.out.println(commandParser.getEnumValue(MeleeWeapon.class));
        return commandParser.getEnumValue(MeleeWeapon.class);
    }
    /**
     * Replaces the current Reader (useful for switching input sources).
     * @param reader new input source
     * @throws IOException if reader initialization fails
     */
    public void setReader(Reader reader) throws IOException {
        this.reader = reader;
    }
    /**
     * Returns current Reader instance.
     * @return active input source abstraction
     */
    public Reader getReader() {
        return reader;
    }

    public void setLastPath(String scriptPath) {
        commandParser.setLastPath(scriptPath);
    }
}
