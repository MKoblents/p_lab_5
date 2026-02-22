package io;

import enums.MeleeWeapon;
import manager.Chapter;
import manager.SpaceMarine;

import java.io.IOException;

public interface Reader {
    String nextLine() throws IOException;
    boolean hasNextLine() throws IOException;
    SpaceMarine getInputSpaceMarine(SpaceMarine spaceMarine);
    String getInputString() throws IOException;
    String getTrimmedText() throws IOException;
    <T extends Enum<T>> T getInputEnum(Class<T> enumType) throws IOException;
    double getInputDouble() throws IOException;
    Chapter getInputChapter() throws IOException;
    public long getInputLong() throws IOException;
    public MeleeWeapon getInputMeleeWeapon();
}
