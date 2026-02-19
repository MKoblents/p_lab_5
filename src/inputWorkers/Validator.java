package inputWorkers;

import exceptions.AlreadyExistedIdException;
import exceptions.UnavailableCoordinateException;
import exceptions.UnavailableHealthException;
import manager.Coordinates;
import manager.ProgramManager;
import manager.SpaceMarine;

import java.util.List;

public class Validator {
//    private final ProgramManager programManager;
//    public Validator(ProgramManager programManager){
//        this.programManager = programManager;
//    }
    public static boolean isValid(String data){
//        #TODO
        return true;
    }
    public static boolean isIdValid(List<SpaceMarine> spaseMarines, long id) throws AlreadyExistedIdException{
        for (SpaceMarine spaceMarine: spaseMarines){
            if (spaceMarine.getId() == id){//#TODO diapason
                throw new AlreadyExistedIdException();
            }
        }
        return true;
    }
    public static boolean isNameValid(String name) throws NullPointerException{
        if (name == null || name.isEmpty()){
            throw new NullPointerException("Name can't be null or empty!");
        }return true;

    }
    public static boolean isCoordinatesValid(Coordinates coordinates) throws UnavailableCoordinateException {
        return isXCoordinateValid(coordinates.getX()) && isYCoordinateValid(coordinates.getY());
    }
    public static boolean isXCoordinateValid(long x) throws UnavailableCoordinateException {
        if (x<-617){//TODO diapason
            throw new UnavailableCoordinateException("X must be bigger than -617");
        }return true;
    }
    public static boolean isYCoordinateValid(long y) throws UnavailableCoordinateException {
        //#TODO diapason
        if (y<-842){//TODO diapason
            throw new UnavailableCoordinateException("X must be bigger than -617");
        }return true;
    }
    public static boolean isHealthValid(double health) throws UnavailableHealthException{
        if (health == 0){
            throw new UnavailableHealthException("Health must be bigger than 0");
        }return true;
    }

}
