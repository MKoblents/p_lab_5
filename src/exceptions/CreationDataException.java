package exceptions;

import java.time.ZonedDateTime;

public class CreationDataException extends RuntimeException {
    public CreationDataException(String name, ZonedDateTime wrongTime, ZonedDateTime correctTime) {
        super("SpaceMarine "+name+" had wrong creation data "+wrongTime+" replaced with "+correctTime);
    }
}
