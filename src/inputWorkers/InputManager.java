package inputWorkers;

import enums.MeleeWeapon;
import io.Reader;
import manager.SpaceMarine;

import javax.swing.text.html.parser.Parser;
import java.io.IOException;

public class InputManager {
    private Parser parser;
    private Validator validator;
    private Reader reader;
    private String lastString; //#TODO do normal logic
    public InputManager(Parser parser, Reader reader){
        this.parser=parser;
        this.reader= reader;
    }

    public SpaceMarine getElement(){
        try {
            String element = reader.nextLine();
//            parser.parse(element); parse an element ->
            if (validator.isValid(element)){
                return new SpaceMarine();
            }else {  // #TODO throw exception
                System.out.println("your data is incorrect, please, try again"); // How to quit this method???
                return getElement();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public long getLastLong(){//#TODO
        return 0;
    }
}
