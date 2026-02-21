package commands;

public class ExitCommand implements Command{
    public String helpInformation = "завершить программу (без сохранения в файл)";

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public void execute() {
        System.exit(0);
    }
}
