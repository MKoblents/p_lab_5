package inputWorkers;

import Exceptions.AlreadyExistedIdException;
import Exceptions.UnavailableCoordinateException;
import Exceptions.UnavailableHealthException;
import manager.Coordinates;
import manager.ProgramManager;
import manager.SpaceMarine;

public class Validator {
    private final ProgramManager programManager;
    public Validator(ProgramManager programManager){
        this.programManager = programManager;
    }
    public boolean isValid(String data){
//        #TODO
        return true;
    }
    public boolean isIdValid(long id) throws AlreadyExistedIdException{
        for (SpaceMarine spaceMarine: programManager.getCollectionManager().getSpaceMarines()){
            if (spaceMarine.getId() == id){//#TODO diapason
                throw new AlreadyExistedIdException();
            }
        }
        return true;
    }
    public boolean isNameValid(String name) throws NullPointerException{
        if (name == null || name.isEmpty()){
            throw new NullPointerException("Name can't be null or empty!");
        }return true;

    }
    public boolean isCoordinatesValid(Coordinates coordinates) throws UnavailableCoordinateException {
        return isXCoordinateValid(coordinates.getX()) && isYCoordinateValid(coordinates.getY());
    }
    public boolean isXCoordinateValid(long x) throws UnavailableCoordinateException {
        if (x<-617){//TODO diapason
            throw new UnavailableCoordinateException("X must be bigger than -617");
        }return true;
    }
    public boolean isYCoordinateValid(long y) throws UnavailableCoordinateException {
        //#TODO diapason
        if (y<-842){//TODO diapason
            throw new UnavailableCoordinateException("X must be bigger than -617");
        }return true;
    }
    public boolean isHealthValid(double health) throws UnavailableHealthException{
        if (health == 0){
            throw new UnavailableHealthException("Health must be bigger than 0");
        }return true;
    }

}
