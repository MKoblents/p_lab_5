package inputWorkers;

import enums.MeleeWeapon;
import io.Reader;
import manager.CollectionManager;
import manager.SpaceMarine;

import java.io.IOException;
/**
 * Facade for input operations: delegates to Reader, Validator, and CommandParser.
 * Provides unified access to parsed command arguments and SpaceMarine input.
 */
public class InputManager {
    /** Validator delegate for data integrity checks. */
    private Validator validator;
    /** Reader delegate for abstracted input source (console/file). */
    private Reader reader;
    /** CommandParser delegate for tokenizing and extracting arguments. */
    private CommandParser commandParser;
    /** CollectionManager delegate for SpaceMarine factory methods. */
    private CollectionManager collectionManager;
    /**
     * Initializes manager with required dependencies via constructor injection.
     * @param reader input source abstraction
     * @param validator validation logic handler
     * @param collectionManager factory for SpaceMarine creation
     * @param commandParser argument extraction helper
     */
    public InputManager(Reader reader, Validator validator, CollectionManager collectionManager, CommandParser commandParser){
        this.reader= reader;
        this.validator =validator;
        this.collectionManager = collectionManager;
        this.commandParser = commandParser;
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
        SpaceMarine spaceMarine = collectionManager.getNewSpaceMarine();
        return reader.getInputSpaceMarine(spaceMarine);
    }
    /**
     * Returns the validator instance for external validation calls.
     * @return validator delegate
     */
    public Validator getValidator() {
        return validator;
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

}
