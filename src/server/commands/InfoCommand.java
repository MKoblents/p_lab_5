package server.commands;

import server.manager.CollectionService;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

public class InfoCommand implements Command{
    private CollectionService collectionService;
    private String helpInformation = "info : вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)";
    public InfoCommand(CollectionService collectionService){
        this.collectionService= collectionService;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        return  new CommandResponse(true, collectionService.getSpaceMarines().getClass()+"\n"
                + collectionService.getCreationData()+ "\n"
                + collectionService.getSpaceMarines().size(), "Info command done", commandRequest.requestId(), commandRequest.clientId());
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }
}
