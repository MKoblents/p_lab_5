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
    private boolean eof = false;
    public FileBufferedReader(String filePath, XMLParser xmlParser) throws IOException {
        this.filePath = filePath;
        this.bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
        preloadNextLine();
        this.xmlParser = xmlParser;
    }
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
        if (!readSomething && currentByte == -1) {
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


    public void close() throws IOException {
        bufferedInputStream.close();
    }
}
