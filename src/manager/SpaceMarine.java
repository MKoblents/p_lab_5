package manager;

import enums.AstartesCategory;
import enums.MeleeWeapon;
import enums.Weapon;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.ZonedDateTime;
/**
 * Represents a Space Marine entity in the collection management system.
 *
 * <p>This class implements {@link Comparable} to allow sorting by health value.
 * Instances are intended to be managed by {@link CollectionManager}, which
 * handles ID generation and validation.
 *
 * <p><strong>Validation rules:</strong>
 * <ul>
 *   <li>{@code id} must be > 0 and unique (auto-generated)</li>
 *   <li>{@code name} cannot be null or empty</li>
 *   <li>{@code coordinates} cannot be null</li>
 *   <li>{@code creationDate} cannot be null (auto-generated)</li>
 *   <li>{@code health} if present, must be > 0</li>
 *   <li>{@code meleeWeapon} cannot be null</li>
 * </ul>
 *
 * @author Your Name
 * @version 1.0
 * @see CollectionManager
 * @see Coordinates
 * @see Chapter
 */
@XmlRootElement(name = "spaceMarine")
@XmlAccessorType(XmlAccessType.FIELD)
public class SpaceMarine implements Comparable<SpaceMarine> {
    @XmlElement
    private long id =1; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    @XmlElement
    private String name; //Поле не может быть null, Строка не может быть пустой
    @XmlElement
    private Coordinates coordinates; //Поле не может быть null
    @XmlElement
    private ZonedDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    @XmlElement
    private Double health = 0.0; //Поле может быть null, Значение поля должно быть больше 0
    @XmlElement
    private AstartesCategory category; //Поле может быть null
    @XmlElement
    private Weapon weaponType; //Поле может быть null
    @XmlElement
    private MeleeWeapon meleeWeapon; //Поле не может быть null
    @XmlElement
    private Chapter chapter; //Поле может быть null
    protected SpaceMarine(){
    }
    /**
     * Compares this Space Marine to another by health value.
     *
     * <p><strong>Implementation note:</strong> Uses {@link Double#compare(double, double)},
     * which handles {@code null} health values by treating them as 0.0.
     *
     * @param other the Space Marine to compare to
     * @return negative if this.health < other.health,
     *         zero if equal,
     *         positive if this.health > other.health
     * @throws NullPointerException if other is null
     * @see Comparable
     */
    @Override
    public int compareTo(SpaceMarine other) {
        return Double.compare(this.health, other.health);
    }

    /**
     * Parameterized constructor for creating initialized instances.
     *
     * @param name the Space Marine name (must not be null/empty)
     * @param coordinates the position coordinates (must not be null)
     * @param meleeWeapon the melee weapon type (must not be null)
     * @see CollectionManager#getNewSpaceMarine(String, Coordinates, MeleeWeapon)
     */
    protected SpaceMarine(String name, Coordinates coordinates, MeleeWeapon meleeWeapon){
        this.name = name;
        this.coordinates = coordinates;
        this.meleeWeapon=meleeWeapon;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "SpaceMarine id: "+this.id+":\n" +
                "   Name: "+this.name+
                "\n   Health: "+this.health+
                "\n   Coordinates: " + this.coordinates +
                "\n   MeleeWeapon: "+this.meleeWeapon+
                "\n   Chapter: "+ this.chapter                ;
    }

    public long getId() {
        return id;
    }
    /**
     * Sets the Space Marine name.
     * @param name the new name (should not be null/empty per validation rules)
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * Sets the tactical category.
     * @param category the new category value, or null
     */
    public void setCategory(AstartesCategory category) {
        this.category = category;
    }

    /**
     * Sets the unique identifier.
     *
     * <p><strong>Warning:</strong> Should only be called by {@link CollectionManager}.
     * Manual calls may break collection integrity.
     *
     * @param id the new ID value (must be > 0)
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Sets the chapter affiliation.
     * @param chapter the new chapter object, or null
     */
    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    /**
     * Sets the spatial coordinates.
     * @param coordinates the new coordinates (should not be null per contract)
     */
    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }
    /**
     * Sets the health value.
     * @param health the new health value (if not null, must be >= 0)
     */
    public void setHealth(Double health) {
        this.health = health;
    }
    /**
     * Sets the melee weapon type.
     * @param meleeWeapon the new melee weapon (should not be null per contract)
     */
    public void setMeleeWeapon(MeleeWeapon meleeWeapon) {
        this.meleeWeapon = meleeWeapon;
    }
    /**
     * Sets the ranged weapon type.
     * @param weaponType the new weapon value, or null
     */
    public void setWeaponType(Weapon weaponType) {
        this.weaponType = weaponType;
    }

    public AstartesCategory getCategory() {
        return category;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public Double getHealth() {
        return health;
    }

    public MeleeWeapon getMeleeWeapon() {
        return meleeWeapon;
    }

    public String getName() {
        return name;
    }

    public Weapon getWeaponType() {
        return weaponType;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

}