package shared.dto;

import shared.enums.ClientState;

import java.time.Instant;
import java.util.Optional;

public record  ClientStatus (String clientId, ClientState clientState, Instant lastHeartbeat, int commandsExecuted, Optional<String> lastCommand){
}
