package io;

import manager.SpaceMarine;

import java.io.IOException;
/**
 * Abstraction for input sources: console, file, or stream.
 * Provides line reading and SpaceMarine object input.
 */
public interface Reader {
    /**
     * Reads the next line of input.
     * @return the line content without line terminator
     * @throws IOException if read operation fails
     */
    String nextLine() throws IOException;
    /**
     * Checks if more input is available.
     * @return true if another line can be read
     * @throws IOException if stream check fails
     */
    boolean hasNextLine() throws IOException;
    /**
     * Prompts user to fill/modify SpaceMarine fields via input.
     * @param spaceMarine the object to populate (may be partially filled)
     * @return the same instance with updated fields
     */
    SpaceMarine getInputSpaceMarine(SpaceMarine spaceMarine);
    /**
     * Stores XML string for deferred parsing (used by add/update commands).
     * @param lastXmlString the XML data to cache
     */
    void setLastXmlString(String lastXmlString);
    /**
     * Clears any buffered input (useful before/after script execution).
     * @throws IOException if buffer reset fails
     */
    void clearBuffer() throws IOException;
}
