package server.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.manager.ClientRegistry;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import java.util.UUID;

public class SpawnClientCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(SpawnClientCommand.class);
    private final String HELP_INFO = "spawn_client : создать нового клиента (дочерний процесс)";
    public SpawnClientCommand() {
    }

    @Override
    public String getHelpInformation() {
        return HELP_INFO;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        String childClientId = UUID.randomUUID().toString().substring(0, 8);
        String parentClientId = commandRequest.clientId();
        logger.info("Creating child client {} for parent {}", childClientId, parentClientId);
        logger.debug("Child {} registered in registry", childClientId);
        return new CommandResponse(true, null, "Child client created: "+childClientId,
                commandRequest.requestId(), childClientId);
    }
}