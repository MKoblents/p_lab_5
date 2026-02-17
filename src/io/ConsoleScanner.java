package io;

import java.io.IOException;
import java.util.Scanner;

public class ConsoleScanner implements Reader{
    private final Scanner scanner;
    //#TODO currentLine field
    public ConsoleScanner(){
        this.scanner = new Scanner(System.in);
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
