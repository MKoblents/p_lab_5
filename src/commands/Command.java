package commands;
/**
 * Base contract for all executable commands in the application.
 * Implements the Command pattern: encapsulates request as an object.
 *
 * @see manger.Invoker
 */
public interface Command {
    /**
     * Executes the command's business logic.
     *
     * <p>May modify application state, interact with I/O, or throw runtime exceptions
     * on validation failure. Implementations should be idempotent where possible.
     */
    void execute();
    /**
     * Returns human-readable help text for this command.
     *
     * @return usage description including syntax, arguments, and examples
     * @implNote Should return consistent string without side effects
     */
    String getHelpInformation();
}
