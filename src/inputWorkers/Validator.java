package inputWorkers;

import enums.MeleeWeapon;
import exceptions.*;
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
    public void spaceMarinesValidate(List<SpaceMarine> spaceMarines){
        for (SpaceMarine spaceMarine: spaceMarines){
            spaceMarineValidate(spaceMarine);
        }
    }

    public void spaceMarineValidate(SpaceMarine spaceMarine){
        if (spaceMarine == null){return;}
        try {
            nameValidate(spaceMarine);
            coordinatesValidate(spaceMarine);
            creationDateValidate(spaceMarine);
            healthValidate(spaceMarine);
            meleeWeaponValidate(spaceMarine);
            chapterValidate(spaceMarine);
        }catch (Exception e){
            System.err.println(e.getMessage());
            spaceMarineValidate(spaceMarine);
        }
    }

    public void chapterValidate(SpaceMarine spaceMarine) throws NullPointerException {
        if (spaceMarine.getChapter() == null){
            return;
        }
         chapterNameValidate(spaceMarine);
         chapterWorldValidate(spaceMarine);
    }
    public void chapterNameValidate(SpaceMarine spaceMarine) throws NullPointerException{
        String name = spaceMarine.getChapter().getName();
        if (name == null || name.isEmpty()) {
            spaceMarine.getChapter().setName("Chapter"+ spaceMarine.getId());
            throw new NullPointerException("SpaceMarine name can't be null or empty!. Replaced with Chapter" +spaceMarine.getId());
        }
    }
    public void chapterWorldValidate(SpaceMarine spaceMarine) throws NullPointerException{
        String world = spaceMarine.getChapter().getWorld();
        if (world == null || world.isEmpty()) {
            spaceMarine.getChapter().setWorld("ChapteWorldr"+ spaceMarine.getId());
            throw new NullPointerException("SpaceMarine world can't be null or empty!. Replaced with ChapterWorld" +spaceMarine.getId());
        };
    }
    public void meleeWeaponValidate(SpaceMarine spaceMarine) throws  UnavailableMeleeWeaponException{
        if (spaceMarine.getMeleeWeapon() == null){
            spaceMarine.setMeleeWeapon(MeleeWeapon.CHAIN_AXE);
            throw new UnavailableMeleeWeaponException(spaceMarine.getName(),MeleeWeapon.CHAIN_AXE);
        }
    }
    public void creationDateValidate(SpaceMarine spaceMarine) throws CreationDataException{
        ZonedDateTime originalTime = spaceMarine.getCreationDate();
        if (originalTime == null || originalTime.toInstant().toEpochMilli() <=0){
            ZonedDateTime correctTime = ZonedDateTime.now();
            spaceMarine.setCreationDate(correctTime);
            throw new CreationDataException(spaceMarine.getName(),originalTime, correctTime);
        }
    }
//    public void idValidate(SpaceMarine spaceMarine) throws WrongIdException{
//        long correctId = manager.getCorrectId(spaceMarine);
//        long realId = spaceMarine.getId();
//        if ( correctId != realId){
//            spaceMarine.setId(correctId);
//            throw new WrongIdException(spaceMarine.getName(), realId, correctId);
//        }
//    }
    public void nameValidate(SpaceMarine spaceMarine) throws NullPointerException{
        String name = spaceMarine.getName();
        if (name == null || name.isEmpty()){
            spaceMarine.setName("SpaceMarine"+spaceMarine.getId());
            throw new NullPointerException("SpaceMarine name can't be null or empty!. Replaced with SpaceMarine"+spaceMarine.getId());
        }
    }
    public void coordinatesValidate(SpaceMarine spaceMarine) throws UnavailableCoordinateException {
        Coordinates coordinates = spaceMarine.getCoordinates();
        xCoordinateValidate(coordinates);
        yCoordinateValidate(coordinates);
    }
    public void xCoordinateValidate(Coordinates coordinates) throws UnavailableCoordinateException {
        if (coordinates.getX()<-617){
            coordinates.setX(0);
            throw new UnavailableCoordinateException("X must be bigger than -617, replaced with 0");
        }
    }
    public void yCoordinateValidate(Coordinates coordinates) throws UnavailableCoordinateException {
        if (coordinates.getY()<-842){
            coordinates.setY(0);
            throw new UnavailableCoordinateException("Y must be bigger than -842, replaced with 0");
        }
    }
    public void healthValidate(SpaceMarine spaceMarine) throws UnavailableHealthException{
        double health = spaceMarine.getHealth();
        if (health < 0.0){
            spaceMarine.setHealth(0.0);
            throw new UnavailableHealthException(spaceMarine.getName(),health,0.0);
        }
    }

}
