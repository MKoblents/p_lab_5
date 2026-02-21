package exceptions;

import enums.MeleeWeapon;

public class UnavailableMeleeWeaponException extends RuntimeException {
    public UnavailableMeleeWeaponException(String name, MeleeWeapon meleeWeapon) {
        super("SpaceMarine "+ name+ " had null meleeWeapon that's unavailable, replaced with: "+meleeWeapon);
    }
}
