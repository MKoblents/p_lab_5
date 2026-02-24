package inputWorkers;

import enums.MeleeWeapon;
import exceptions.*;
import manager.CollectionManager;
import manager.Coordinates;
import manager.SpaceMarine;

import java.time.ZonedDateTime;
import java.util.List;
/**
 * Validates SpaceMarine objects against business rules and constraints.
 * Applies auto-fixes for minor issues and throws custom exceptions for critical failures.
 */
public class Validator {
    /** Manager delegate for collection operations. */
    private CollectionManager manager;
    /** Reference to collection list (note: mutable reference). */
    private List<SpaceMarine> spaceMarines;
    /**
     * Initializes validator with manager reference.
     * @param manager the CollectionManager to associate with
     */
    public Validator(CollectionManager manager){
        this.spaceMarines = manager.getSpaceMarines();
        this.manager =manager;
    }
    /**
     * Validates each SpaceMarine in the provided list.
     * @param spaceMarines list of objects to validate
     */
    public void spaceMarinesValidate(List<SpaceMarine> spaceMarines){
        for (SpaceMarine spaceMarine: spaceMarines){
            spaceMarineValidate(spaceMarine);
        }
    }
    /**
     * Validates a single SpaceMarine against all field rules.
     * Auto-fixes minor issues; logs errors for critical failures.
     * @param spaceMarine object to validate (skipped if null)
     */
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
    /**
     * Validates Chapter subfields if Chapter is present.
     * @param spaceMarine parent object containing Chapter
     * @throws NullPointerException if Chapter name/world is invalid
     */
    public void chapterValidate(SpaceMarine spaceMarine) throws NullPointerException {
        if (spaceMarine.getChapter() == null){
            return;
        }
         chapterNameValidate(spaceMarine);
         chapterWorldValidate(spaceMarine);
    }
    /**
     * Ensures Chapter name is non-empty; auto-fills default if missing.
     * @param spaceMarine parent object
     * @throws NullPointerException with message if name was fixed
     */
    public void chapterNameValidate(SpaceMarine spaceMarine) throws NullPointerException{
        String name = spaceMarine.getChapter().getName();
        if (name == null || name.isEmpty()) {
            spaceMarine.getChapter().setName("Chapter"+ spaceMarine.getId());
            throw new NullPointerException("SpaceMarine name can't be null or empty!. Replaced with Chapter" +spaceMarine.getId());
        }
    }
    /**
     * Ensures Chapter world is non-empty; auto-fills default if missing.
     * @param spaceMarine parent object
     * @throws NullPointerException with message if world was fixed
     */
    public void chapterWorldValidate(SpaceMarine spaceMarine) throws NullPointerException{
        String world = spaceMarine.getChapter().getWorld();
        if (world == null || world.isEmpty()) {
            spaceMarine.getChapter().setWorld("ChapteWorldr"+ spaceMarine.getId());
            throw new NullPointerException("SpaceMarine world can't be null or empty!. Replaced with ChapterWorld" +spaceMarine.getId());
        };
    }
    /**
     * Ensures melee weapon is set; applies default if missing.
     * @param spaceMarine object to validate
     * @throws UnavailableMeleeWeaponException if weapon was auto-set
     */
    public void meleeWeaponValidate(SpaceMarine spaceMarine) throws  UnavailableMeleeWeaponException{
        if (spaceMarine.getMeleeWeapon() == null){
            spaceMarine.setMeleeWeapon(MeleeWeapon.CHAIN_AXE);
            throw new UnavailableMeleeWeaponException(spaceMarine.getName(),MeleeWeapon.CHAIN_AXE);
        }
    }
    /**
     * Ensures creation date is valid (non-null, positive epoch); auto-sets to now if invalid.
     * @param spaceMarine object to validate
     */
    public void creationDateValidate(SpaceMarine spaceMarine){
        ZonedDateTime originalTime = spaceMarine.getCreationDate();
        if (originalTime == null || originalTime.toInstant().toEpochMilli() <=0){
            ZonedDateTime correctTime = ZonedDateTime.now();
            spaceMarine.setCreationDate(correctTime);
        }
    }
    /**
     * Ensures name is non-null and non-empty; auto-fills default if missing.
     * @param spaceMarine object to validate
     * @throws NullPointerException with message if name was fixed
     */
    public void nameValidate(SpaceMarine spaceMarine) throws NullPointerException{
        String name = spaceMarine.getName();
        if (name == null || name.isEmpty()){
            spaceMarine.setName("SpaceMarine"+spaceMarine.getId());
            throw new NullPointerException("SpaceMarine name can't be null or empty!. Replaced with SpaceMarine"+spaceMarine.getId());
        }
    }
    /**
     * Validates Coordinates object and its x/y bounds.
     * @param spaceMarine parent object
     * @throws UnavailableCoordinateException if coordinates were corrected
     */
    public void coordinatesValidate(SpaceMarine spaceMarine) throws UnavailableCoordinateException {
        Coordinates coordinates = spaceMarine.getCoordinates();
        if (coordinates == null){
            spaceMarine.setCoordinates(new Coordinates());
            return;
        }
        xCoordinateValidate(coordinates);
        yCoordinateValidate(coordinates);
    }
    /**
     * Ensures X coordinate >= -617; clamps to 0 if violated.
     * @param coordinates object to validate
     * @throws UnavailableCoordinateException if value was corrected
     */
    public void xCoordinateValidate(Coordinates coordinates) throws UnavailableCoordinateException {
        if (coordinates.getX()<-617){
            coordinates.setX(0);
            throw new UnavailableCoordinateException("X must be bigger than -617, replaced with 0");
        }
    }
    /**
     * Ensures Y coordinate >= -842; clamps to 0 if violated.
     * @param coordinates object to validate
     * @throws UnavailableCoordinateException if value was corrected
     */
    public void yCoordinateValidate(Coordinates coordinates) throws UnavailableCoordinateException {
        if (coordinates.getY()<-842){
            coordinates.setY(0);
            throw new UnavailableCoordinateException("Y must be bigger than -842, replaced with 0");
        }
    }
    /**
     * Ensures health is non-negative; clamps to 0.0 if violated.
     * @param spaceMarine object to validate
     * @throws UnavailableHealthException if value was corrected
     */
    public void healthValidate(SpaceMarine spaceMarine) throws UnavailableHealthException{
        double health = spaceMarine.getHealth();
        if (health < 0.0){
            spaceMarine.setHealth(0.0);
            throw new UnavailableHealthException(spaceMarine.getName(),health,0.0);
        }
    }

}
