package inputWorkers;

import enums.MeleeWeapon;
import io.Reader;
import manager.SpaceMarine;

import javax.swing.text.html.parser.Parser;
import java.io.IOException;

public class InputManager {
    private Validator validator;
    private Reader reader;
    private String lastString; //#TODO do normal logic
    private long lastLong;
    private double lastDouble;
    public InputManager(Reader reader){
        this.reader= reader;
    }
    public long getLastLong(){//#TODO
        return 0;
    }
    public String parseCommand() throws IOException {
        String line = reader.nextLine();
        String[] parts = line.trim().split("\\s+");
        StringBuilder key = new StringBuilder();
        try {
            lastLong = Long.parseLong(parts[parts.length-1]);
            for (int i = 0; i< parts.length-2; i++){
                key.append(parts[i]);
            }return key.toString();
        } catch (NumberFormatException e) {
            try {
                lastDouble = Double.parseDouble(parts[parts.length-1]);
                for (int i = 0; i< parts.length-2; i++){
                    key.append(parts[i]);
                }return key.toString();
            }catch (NumberFormatException e2){
                return line.replaceAll("\\s+","").toLowerCase();
            }
        }
    }
}
