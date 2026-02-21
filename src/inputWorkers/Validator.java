package inputWorkers;

import enums.MeleeWeapon;
import exceptions.*;
import manager.CollectionManager;
import manager.Coordinates;
import manager.SpaceMarine;

import java.time.ZonedDateTime;
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
        }catch ( e){

        }
    }

    public boolean isMeleeWeaponValid(SpaceMarine spaceMarine) {
        if (spaceMarine.getMeleeWeapon() == null){
            spaceMarine.setMeleeWeapon(MeleeWeapon.CHAIN_AXE);
            throw new UnavailableMeleeWeaponException(spaceMarine.getName(),MeleeWeapon.CHAIN_AXE);
        }return true;
    }

    public boolean isCreationDateValid(SpaceMarine spaceMarine) throws CreationDataException{
        ZonedDateTime originalTime = spaceMarine.getCreationDate();
        if (originalTime == null || originalTime.toInstant().toEpochMilli() <=0){
            ZonedDateTime correctTime = ZonedDateTime.now();
            spaceMarine.setCreationDate(correctTime);
            throw new CreationDataException(spaceMarine.getName(),originalTime, correctTime);
        }return true;
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
    public static boolean isCoordinatesValid(SpaceMarine spaceMarine) throws UnavailableCoordinateException {
        Coordinates coordinates = spaceMarine.getCoordinates();
        return isXCoordinateValid(coordinates) && isYCoordinateValid(coordinates);
    }
    public static boolean isXCoordinateValid(Coordinates coordinates) throws UnavailableCoordinateException {
        if (coordinates.getX()<-617){//TODO diapason
            coordinates.setX(0);
            throw new UnavailableCoordinateException("X must be bigger than -617, replaced with 0");
        }return true;
    }
    public static boolean isYCoordinateValid(Coordinates coordinates) throws UnavailableCoordinateException {
        if (coordinates.getY()<-842){
            throw new UnavailableCoordinateException("Y must be bigger than -842, replaced with 0");
        }return true;
    }
    public static boolean isHealthValid(SpaceMarine spaceMarine) throws UnavailableHealthException{
        double health = spaceMarine.getHealth();
        if (health <= 0.0){
            spaceMarine.setHealth(0.0);
            throw new UnavailableHealthException(spaceMarine.getName(),health,0.0);
        }return true;
    }

}
