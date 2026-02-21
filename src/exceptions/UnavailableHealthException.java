package exceptions;

public class UnavailableHealthException extends Exception {
    public UnavailableHealthException(String name, double originalHealth, double newHealth) {
        super("SpaceMarine "+name+" had wrong health "+originalHealth+" replaced with "+newHealth);
    }
}
