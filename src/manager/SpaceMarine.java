package manager;

import enums.AstartesCategory;
import enums.MeleeWeapon;
import enums.Weapon;

import java.time.ZonedDateTime;

public class SpaceMarine implements Comparable<SpaceMarine> {
    private long id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private java.time.ZonedDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private Double health; //Поле может быть null, Значение поля должно быть больше 0
    private AstartesCategory category; //Поле может быть null
    private Weapon weaponType; //Поле может быть null
    private MeleeWeapon meleeWeapon; //Поле не может быть null
    private Chapter chapter; //Поле может быть null
    public SpaceMarine(){
        //TODO id, creation data,...
    }

    @Override
    public int compareTo(SpaceMarine other) {
        return Long.compare(this.id, other.id);
    }

    public SpaceMarine(String name, Coordinates coordinates, MeleeWeapon meleeWeapon){
        this.name = name;

        this.coordinates = coordinates;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {//#TODO do normally
        return super.toString();
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