package manager;

import inputWorkers.InputManager;
import inputWorkers.Validator;
import outputWorkers.CollectionSaver;

import java.io.BufferedReader;

public class ProgramManager {
    private BufferedReader reader;
    private InputManager inputManager;
    private CollectionManager collectionManager;
    private CollectionSaver collectionSaver;
    private Validator validator;

    public SpaceMarine getElement(){
        return inputManager.getElement();
    }

    public CollectionManager getCollectionManager() {
        return collectionManager;
    }

    public CollectionSaver getCollectionSaver() {
        return collectionSaver;
    }

    public InputManager getInputManager() {
        return inputManager;
    }

}
