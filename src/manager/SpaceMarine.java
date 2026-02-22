package manager;

import enums.AstartesCategory;
import enums.MeleeWeapon;
import enums.Weapon;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.ZonedDateTime;
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
    private java.time.ZonedDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
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
        //TODO id, creation data,...
    }

    @Override
    public int compareTo(SpaceMarine other) {
        return Long.compare(this.id, other.id);
    }

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

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(AstartesCategory category) {
        this.category = category;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public void setHealth(Double health) {
        this.health = health;
    }

    public void setMeleeWeapon(MeleeWeapon meleeWeapon) {
        this.meleeWeapon = meleeWeapon;
    }

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