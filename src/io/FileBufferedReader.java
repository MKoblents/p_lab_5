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

public class FileBufferedReader implements Reader{
    private String filePath;
    private final BufferedInputStream bufferedInputStream;
    private String currentLine;
    private String nextLine;
    private XMLParser xmlParser;
    private String lastXmlString;
    public FileBufferedReader(String filePath, XMLParser xmlParser) throws IOException {
        this.filePath = filePath;
        this.bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
    }

    @Override
    public boolean hasNextLine() throws IOException {
        if (nextLine != null) return true;
        bufferedInputStream.mark(1);
        int next = bufferedInputStream.read();
        bufferedInputStream.reset();
        return next != -1;
    }

    @Override
    public String nextLine() throws IOException {
        this.currentLine = nextLine;
        if (nextLine != null) {
            getNextLine();
        }
        return currentLine;
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



}
