package manager;

import enums.AstartesCategory;
import enums.MeleeWeapon;
import enums.Weapon;

public class SpaceMarine {
    private long id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private java.time.ZonedDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private Double health; //Поле может быть null, Значение поля должно быть больше 0
    private AstartesCategory category; //Поле может быть null
    private Weapon weaponType; //Поле может быть null
    private MeleeWeapon meleeWeapon; //Поле не может быть null
    private Chapter chapter; //Поле может быть null
    public SpaceMarine(String name, MeleeWeapon meleeWeapon){
        this.name = name;
        this.meleeWeapon = meleeWeapon;
    }

    @Override
    public String toString() {//#TODO do normally
        return super.toString();
    }

    public long getId() {
        return id;
    }
}