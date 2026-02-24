package io;

import inputWorkers.XMLParser;
import manager.Chapter;
import manager.SpaceMarine;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
/**
 * File-based implementation of {@link Reader} with line buffering.
 * Reads from file path, handles CR/LF line endings, and delegates XML parsing.
 */
public class FileBufferedReader implements Reader{
    /** Path to the source file. */
    private String filePath;
    /** Buffered stream for efficient byte reading. */
    private BufferedInputStream bufferedInputStream;
    /** Current line being processed (unused in current impl). */
    private String currentLine;
    /** Preloaded next line for hasNextLine/nextLine logic. */
    private String nextLine;
    /** XML parser delegate for SpaceMarine deserialization. */
    private XMLParser xmlParser;
    /** Cached XML string for deferred parsing. */
    private String lastXmlString;
    /** Flag indicating end-of-file reached. */
    private boolean eof = false;
    /**
     * Initializes reader with file path and XML parser.
     * Preloads first line for immediate availability.
     * @param filePath path to the input file
     * @param xmlParser parser for SpaceMarine XML blocks
     * @throws IOException if file cannot be opened
     */
    public FileBufferedReader(String filePath, XMLParser xmlParser) throws IOException {
        this.filePath = filePath;
        this.bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
        preloadNextLine();
        this.xmlParser = xmlParser;
    }
    /**
     * Returns the source file path.
     * @return file path string
     */
    public String getFilePath() {
        return filePath;
    }
    /**
     * Preloads next line from stream into nextLine field.
     * Handles both LF and CRLF line endings, sets eof flag when done.
     * @throws IOException if read operation fails
     */
    private void preloadNextLine() throws IOException {
        if (eof) {
            nextLine = null;
            return;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int currentByte;
        boolean readSomething = false;
        while ((currentByte = bufferedInputStream.read()) != -1) {
            readSomething = true;
            if (currentByte == '\n') {
                break;
            } else if (currentByte == '\r') {
                if (bufferedInputStream.available() > 0) {
                    bufferedInputStream.mark(1);
                    int next = bufferedInputStream.read();
                    if (next != '\n') {
                        bufferedInputStream.reset();
                    }
                }
                break;
            }
            output.write(currentByte);
        }
        if (!readSomething) {
            eof = true;
            nextLine = null;
        } else {
            nextLine = output.toString(StandardCharsets.UTF_8);
        }
    }
    @Override
    public boolean hasNextLine() throws IOException {
        return nextLine != null;
    }
    @Override
    public String nextLine() throws IOException {
        String result = nextLine;
        preloadNextLine();
        return result;
    }
    public String getNextLine() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int currentByte;
        while ((currentByte = bufferedInputStream.read()) != -1) {
            if ((char) currentByte == '\n') {
                break;
            } else if ((char) currentByte == '\r') {
                break;
            }
            output.write(currentByte);
        }
        if (output.size() == 0 && currentByte == -1){
            return null;
        }
        return this.nextLine = output.toString(StandardCharsets.UTF_8);
    }

    @Override
    public SpaceMarine getInputSpaceMarine(SpaceMarine spaceMarine){
        try {
            return xmlParser.parseSpaceMarineFromString(lastXmlString);
        } catch (Exception e ) {
            throw new RuntimeException(e);
        }
    }

    public void setLastXmlString(String lastXmlString) {
        this.lastXmlString = lastXmlString;
    }

    /**
     * Closes the underlying file stream.
     * @throws IOException if close fails
     */
    public void close() throws IOException {
        bufferedInputStream.close();
    }
    public void clearBuffer() throws IOException {
        this.nextLine = null;
        this.eof = false;
        reset();
    }
    /**
     * Reopens file and resets reader state to beginning.
     * Useful for re-executing scripts.
     * @throws IOException if file cannot be reopened
     */
    public void reset() throws IOException {
        bufferedInputStream.close();
        this.bufferedInputStream = new BufferedInputStream(
                new FileInputStream(filePath)
        );
        this.nextLine = null;
        this.eof = false;
        preloadNextLine();
    }
}
