package server.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class NonBlockingConsoleReader {
    private final InputStream in = System.in;
    private final byte[] buffer = new byte[1024];
    private final List<String> completeLines = new ArrayList<>();

    public String pollLine() throws IOException {
        int availible = in.available();
        if (availible == 0){
            if (!completeLines.isEmpty()){
                return completeLines.remove(0);
            }
            return null;
        }
        int toRead = Math.min(availible, buffer.length);
        int read = in.read(buffer,0,toRead);
        if (read <=0){
            return completeLines.isEmpty() ? null : completeLines.remove(0);
        }
        String chunk = new String(buffer, 0, read, StandardCharsets.UTF_8);
        processChunk(chunk);
        return completeLines.isEmpty() ? null : completeLines.remove(0);
    }

    private void processChunk(String chunk) {
        StringBuffer current = new StringBuffer();
        for (char c: chunk.toCharArray()){
            if (c == '\n'){
                completeLines.add(current.toString().trim());
                current.setLength(0);
            } else if (c != '\r') {
                current.append(c);
                }
            }
        if (current.length()>0){
            completeLines.add(0, current.toString().trim());
        }
    }
}
