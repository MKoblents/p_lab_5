package exceptions;

public class UnavailableHealthException extends RuntimeException {
    public UnavailableHealthException(String name, double originalHealth, double newHealth) {
        super("SpaceMarine "+name+" had wrong health "+originalHealth+" replaced with "+newHealth);
    }
}
