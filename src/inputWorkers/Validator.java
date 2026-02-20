package inputWorkers;

import exceptions.AlreadyExistedIdException;
import exceptions.UnavailableCoordinateException;
import exceptions.UnavailableHealthException;
import exceptions.WrongIdException;
import manager.CollectionManager;
import manager.Coordinates;
import manager.ProgramManager;
import manager.SpaceMarine;

import java.util.ArrayList;
import java.util.List;

public class Validator {
    private CollectionManager manager;
    private List<SpaceMarine> spaceMarines;
    public Validator(CollectionManager manager){
        this.spaceMarines = manager.getSpaceMarines();
        this.manager =manager;
    }
    public boolean isSpaceMarinesValid(){
        ArrayList<SpaceMarine> badSpaceMarines = new ArrayList<>();
        for (SpaceMarine spaceMarine: spaceMarines){
            if (!isSpaceMarineValid()){
                badSpaceMarines.add(spaceMarine);//TODO
            }
        }
        return true;
    }

    public boolean isSpaceMarineValid(SpaceMarine spaceMarine){
        try {
            return isIdValid(spaceMarine) && isNameValid(spaceMarine) && isCoordinatesValid(spaceMarine)&&isCreationDateValid(spaceMarine) && isHealthValid(spaceMarine) && isMeleeWeaponValid(spaceMarine) && isChapterValid(spaceMarine);
        }catch (AlreadyExistedIdException e){

        }
    }

    public boolean isIdValid(SpaceMarine spaceMarine) throws WrongIdException{
        long correctId = manager.getCorrectId(spaceMarine);
        long realId = spaceMarine.getId();
        if ( correctId== realId){
            return true;
        }else {
            spaceMarine.setId(correctId);
            throw new WrongIdException(spaceMarine.getName(), realId, correctId);
        }

    }
    public boolean isNameValid(SpaceMarine spaceMarine) throws NullPointerException{
        String name = spaceMarine.getName();
        if (name == null || name.isEmpty()){
            spaceMarine.setName("SpaceMarine"+spaceMarine.getId());
            throw new NullPointerException("Name can't be null or empty!. Replaced with 'SpaceMarine'"+spaceMarine.getId());
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
