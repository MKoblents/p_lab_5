package client.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.enums.MeleeWeapon;
import shared.exceptions.*;
import shared.models.Coordinates;
import shared.models.SpaceMarine;

import java.time.ZonedDateTime;
import java.util.List;
/**
 * Validates SpaceMarine objects against business rules and constraints.
 * Applies auto-fixes for minor issues and throws custom exceptions for critical failures.
 * Technical events are logged via SLF4J; user-facing errors use System.err.
 */
public class Validator {
    private static final Logger logger = LoggerFactory.getLogger(Validator.class);
    /**
     * Validates each SpaceMarine in the provided list.
     * @param spaceMarines list of objects to validate
     */
    public static void spaceMarinesValidate(List<SpaceMarine> spaceMarines) {
        if (spaceMarines == null) {
            logger.debug("spaceMarinesValidate called with null list - skipped");
            return;
        }
        for (SpaceMarine spaceMarine : spaceMarines) {
            spaceMarineValidate(spaceMarine);
        }
    }
    /**
     * Validates a single SpaceMarine against all field rules.
     * Auto-fixes minor issues; logs errors for critical failures.
     * @param spaceMarine object to validate (skipped if null)
     */
    public static void spaceMarineValidate(SpaceMarine spaceMarine) {
        if (spaceMarine == null) {
            logger.debug("spaceMarineValidate called with null object - skipped");
            return;
        }
        try {
            nameValidate(spaceMarine);
            coordinatesValidate(spaceMarine);
            creationDateValidate(spaceMarine);
            healthValidate(spaceMarine);
            meleeWeaponValidate(spaceMarine);
            chapterValidate(spaceMarine);
        } catch (Exception e) {
            logger.debug("Validation failed for SpaceMarine {}: {}", spaceMarine.getId(), e.getMessage(), e);
            System.err.println("Validation warning: " + e.getMessage());
        }
    }
    /**
     * Validates Chapter subfields if Chapter is present.
     * @param spaceMarine parent object containing Chapter
     * @throws NullPointerException if Chapter name/world is invalid
     */
    public static void chapterValidate(SpaceMarine spaceMarine) throws NullPointerException {
        if (spaceMarine.getChapter() == null) {
            logger.debug("Chapter is null - skipping chapter validation");
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
    public static void chapterNameValidate(SpaceMarine spaceMarine) throws NullPointerException {
        if (spaceMarine.getChapter() == null) return;
        String name = spaceMarine.getChapter().getName();
        if (name == null || name.trim().isEmpty()) {
            String defaultName = "Chapter" + spaceMarine.getId();
            spaceMarine.getChapter().setName(defaultName);
            logger.debug("Auto-fixed empty Chapter name to: {}", defaultName);
            throw new NullPointerException("Chapter name was empty. Auto-set to: " + defaultName);
        }
    }
    /**
     * Ensures Chapter world is non-empty; auto-fills default if missing.
     * @param spaceMarine parent object
     * @throws NullPointerException with message if world was fixed
     */
    public static void chapterWorldValidate(SpaceMarine spaceMarine) throws NullPointerException {
        if (spaceMarine.getChapter() == null) return;
        String world = spaceMarine.getChapter().getWorld();
        if (world == null || world.trim().isEmpty()) {
            String defaultWorld = "ChapterWorld" + spaceMarine.getId();
            spaceMarine.getChapter().setWorld(defaultWorld);
            logger.debug("Auto-fixed empty Chapter world to: {}", defaultWorld);
            throw new NullPointerException("Chapter world was empty. Auto-set to: " + defaultWorld);
        }
    }

    /**
     * Ensures melee weapon is set; applies default if missing.
     * @param spaceMarine object to validate
     * @throws UnavailableMeleeWeaponException if weapon was auto-set
     */
    public static void meleeWeaponValidate(SpaceMarine spaceMarine) throws UnavailableMeleeWeaponException {
        if (spaceMarine.getMeleeWeapon() == null) {
            MeleeWeapon defaultWeapon = MeleeWeapon.CHAIN_AXE;
            spaceMarine.setMeleeWeapon(defaultWeapon);
            logger.debug("Auto-set missing melee weapon to: {} for SpaceMarine {}", defaultWeapon, spaceMarine.getId());
            throw new UnavailableMeleeWeaponException(spaceMarine.getName(), defaultWeapon);
        }
    }
    /**
     * Ensures creation date is valid (non-null, positive epoch); auto-sets to now if invalid.
     * @param spaceMarine object to validate
     */
    public static void creationDateValidate(SpaceMarine spaceMarine) {
        ZonedDateTime originalTime = spaceMarine.getCreationDate();
        if (originalTime == null || originalTime.toInstant().toEpochMilli() <= 0) {
            ZonedDateTime correctTime = ZonedDateTime.now();
            spaceMarine.setCreationDate(correctTime);
            logger.debug("Auto-fixed invalid creation date to current time for SpaceMarine {}", spaceMarine.getId());
        }
    }

    /**
     * Ensures name is non-null and non-empty; auto-fills default if missing.
     * @param spaceMarine object to validate
     * @throws NullPointerException with message if name was fixed
     */
    public static void nameValidate(SpaceMarine spaceMarine) throws NullPointerException {
        String name = spaceMarine.getName();
        if (name == null || name.trim().isEmpty()) {
            String defaultName = "SpaceMarine" + spaceMarine.getId();
            spaceMarine.setName(defaultName);
            logger.debug("Auto-fixed empty name to: {}", defaultName);
            throw new NullPointerException("SpaceMarine name was empty. Auto-set to: " + defaultName);
        }
    }
    /**
     * Validates Coordinates object and its x/y bounds.
     * @param spaceMarine parent object
     * @throws UnavailableCoordinateException if coordinates were corrected
     */
    public static void coordinatesValidate(SpaceMarine spaceMarine) throws UnavailableCoordinateException {
        Coordinates coordinates = spaceMarine.getCoordinates();
        if (coordinates == null) {
            spaceMarine.setCoordinates(new Coordinates());
            logger.debug("Auto-created null Coordinates for SpaceMarine {}", spaceMarine.getId());
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
    public static void xCoordinateValidate(Coordinates coordinates) throws UnavailableCoordinateException {
        if (coordinates.getX() < -617) {
            coordinates.setX(0);
            logger.debug("Auto-corrected X coordinate from {} to 0 (must be >= -617)", coordinates.getX());
            throw new UnavailableCoordinateException("X coordinate was below minimum (-617). Auto-set to 0.");
        }
    }

    /**
     * Ensures Y coordinate >= -842; clamps to 0 if violated.
     * @param coordinates object to validate
     * @throws UnavailableCoordinateException if value was corrected
     */
    public static void yCoordinateValidate(Coordinates coordinates) throws UnavailableCoordinateException {
        if (coordinates.getY() < -842) {
            coordinates.setY(0);
            logger.debug("Auto-corrected Y coordinate from {} to 0 (must be >= -842)", coordinates.getY());
            throw new UnavailableCoordinateException("Y coordinate was below minimum (-842). Auto-set to 0.");
        }
    }

    /**
     * Ensures health is non-negative; clamps to 0.0 if violated.
     * @param spaceMarine object to validate
     * @throws UnavailableHealthException if value was corrected
     */
    public static void healthValidate(SpaceMarine spaceMarine) throws UnavailableHealthException {
        double health = spaceMarine.getHealth();
        if (health < 0.0) {
            spaceMarine.setHealth(0.0);
            logger.debug("Auto-corrected negative health from {} to 0.0 for SpaceMarine {}", health, spaceMarine.getId());
            throw new UnavailableHealthException(spaceMarine.getName(), health, 0.0);
        }
    }
}