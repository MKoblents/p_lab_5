package server.commands;

import server.manager.CollectionManager;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

public class InfoCommand implements Command{
    private CollectionManager collectionManager;
    private String helpInformation = "info : вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)";
    public InfoCommand(CollectionManager collectionManager){
        this.collectionManager= collectionManager;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        return  new CommandResponse(true, collectionManager.getSpaceMarines().getClass()+"\n"
                + collectionManager.getCreationData()+ "\n"
                + collectionManager.getSpaceMarines().size(), "Info command done", commandRequest.requestId());
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }
}
