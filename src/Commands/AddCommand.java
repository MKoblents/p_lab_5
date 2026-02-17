package Commands;
import manager.ProgramManager;
import manager.SpaceMarine;

public class AddCommand implements Command {
    private String helpInformation = "добавить новый элемент в коллекцию";
    private final ProgramManager programManager;
    public AddCommand(ProgramManager programManager){
        this.programManager = programManager;
    }
    @Override
    public String getHelpInformation() {
        return helpInformation;
    }
    // функцию ниже перенести в отдельный парсер со статическим методом
//    private SpaceMarine parseAddInput(){
//        int end;
//        for (int i = 0; i < input.length(); i++) {
//            char character = input.charAt(i);
//            if (character == ' '){
//                end = i;
//                break;
//            }
//        }
//        String name = input.substring(0,end);
//        String meleeWeapon = input.substring(end);
//    }


    @Override
    public void execute() {
        SpaceMarine spaceMarine = programManager.getElement();
        programManager.getCollectionManager().addItem(spaceMarine);
    }
//    public String toString(){
//        return
//    }
}
