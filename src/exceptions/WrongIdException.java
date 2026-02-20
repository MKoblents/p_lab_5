package exceptions;

public class WrongIdException extends RuntimeException {
    public WrongIdException(String name, long realId, long correctId) {
        super("SpaceMarine "+ name+ " got wrong id: "+realId+" which was replaced with "+correctId);
    }
}
