package io;

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
    public FileBufferedReader(String filePath) throws IOException {
        this.filePath = filePath;
        this.bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
    }

    @Override
    public boolean hasNextLine() throws IOException {
        return false;
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
}
