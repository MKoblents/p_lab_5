package inputWorkers;

import enums.MeleeWeapon;
import exceptions.*;
import manager.Chapter;
import manager.CollectionManager;
import manager.Coordinates;
import manager.SpaceMarine;

import java.time.ZonedDateTime;
import java.util.List;

public class Validator {
    private CollectionManager manager;
    private List<SpaceMarine> spaceMarines;
    public Validator(CollectionManager manager){
        this.spaceMarines = manager.getSpaceMarines();
        this.manager =manager;
    }
    public boolean isSpaceMarinesValid(List<SpaceMarine> spaceMarines){
        for (SpaceMarine spaceMarine: spaceMarines){
            isSpaceMarineValid(spaceMarine);
        }return true;
    }

    public boolean isSpaceMarineValid(SpaceMarine spaceMarine){
        try {
            return isIdValid(spaceMarine) && isNameValid(spaceMarine) && isCoordinatesValid(spaceMarine)&&isCreationDateValid(spaceMarine) && isHealthValid(spaceMarine) && isMeleeWeaponValid(spaceMarine) && isChapterValid(spaceMarine);
        }catch (Exception e){
            System.out.println(e.getMessage());
            isSpaceMarineValid(spaceMarine);
            return true;
        }
    }

    public boolean isChapterValid(SpaceMarine spaceMarine) throws NullPointerException {
        return isChapterNameValid(spaceMarine)&&isChapterWorldValid(spaceMarine);
    }
    public boolean isChapterNameValid(SpaceMarine spaceMarine) throws NullPointerException{
        String name = spaceMarine.getChapter().getName();
        if (name == null || name.isEmpty()) {
            spaceMarine.getChapter().setName("Chapter"+ spaceMarine.getId());
            throw new NullPointerException("SpaceMarine name can't be null or empty!. Replaced with Chapter" +spaceMarine.getId());
        }return true;
    }
    public boolean isChapterWorldValid(SpaceMarine spaceMarine) throws NullPointerException{
        String world = spaceMarine.getChapter().getWorld();
        if (world == null || world.isEmpty()) {
            spaceMarine.getChapter().setWorld("ChapteWorldr"+ spaceMarine.getId());
            throw new NullPointerException("SpaceMarine world can't be null or empty!. Replaced with ChapterWorld" +spaceMarine.getId());
        }return true;
    }
    public boolean isMeleeWeaponValid(SpaceMarine spaceMarine) throws  UnavailableMeleeWeaponException{
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
            throw new NullPointerException("SpaceMarine name can't be null or empty!. Replaced with SpaceMarine"+spaceMarine.getId());
        }return true;
    }
    public boolean isCoordinatesValid(SpaceMarine spaceMarine) throws UnavailableCoordinateException {
        Coordinates coordinates = spaceMarine.getCoordinates();
        return isXCoordinateValid(coordinates) && isYCoordinateValid(coordinates);
    }
    public boolean isXCoordinateValid(Coordinates coordinates) throws UnavailableCoordinateException {
        if (coordinates.getX()<-617){//TODO diapason
            coordinates.setX(0);
            throw new UnavailableCoordinateException("X must be bigger than -617, replaced with 0");
        }return true;
    }
    public boolean isYCoordinateValid(Coordinates coordinates) throws UnavailableCoordinateException {
        if (coordinates.getY()<-842){
            throw new UnavailableCoordinateException("Y must be bigger than -842, replaced with 0");
        }return true;
    }
    public boolean isHealthValid(SpaceMarine spaceMarine) throws UnavailableHealthException{
        double health = spaceMarine.getHealth();
        if (health <= 0.0){
            spaceMarine.setHealth(0.0);
            throw new UnavailableHealthException(spaceMarine.getName(),health,0.0);
        }return true;
    }

}
