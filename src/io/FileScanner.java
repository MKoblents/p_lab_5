package io;

import java.io.IOException;
import java.util.Scanner;

public class FileScanner implements Reader{
    private final Scanner scanner;
    private final String filePath;
    //#TODO currentLine field
    public FileScanner(String filePath){
        this.scanner = new Scanner(filePath);
        this.filePath = filePath;
    }

    @Override
    public String nextLine() throws IOException {
        return scanner.nextLine();
    }

    @Override
    public boolean hasNextLine()throws IOException{
        return scanner.hasNextLine();
    }
}

