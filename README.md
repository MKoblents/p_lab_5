## Overview
A distributed client-server application for managing a collection of `SpaceMarine` entities. The system implements a non-blocking network architecture using Java NIO, supports multi-user concurrent access, hierarchical parent-child client relationships, command-based interaction, XML persistence, and external script execution.

# SpaceMarine Collection Management System


## Build and Installation
1. Clone the repository:
   ```bash
     git clone <repository_url>
     cd <repository_directory>
   ```
2. Build the project:
   ```bash
   mvn clean package
   ```
  This generates two executable JAR files in the `target/` directory:
     - `p_lab_5-server.jar`
     - `p_lab_5-client.jar`

## Running the Application

### 1. Start the Server
```bash
java -jar target/p_lab_5-server.jar [--port <PORT>] [--file <PATH>] [--log-level <LEVEL>]
```
- `--port`: Network port for incoming connections (default: `12345`)
- `--file`: Path to the collection XML file (default: `collection.xml` or `$PLAB5` environment variable)
- `--log-level`: Logging verbosity (default: `INFO`)

### 2. Start the Client
  Open a new terminal and run:
```bash
java -jar target/p_lab_5-client.jar [--host <HOST>] [--port <PORT>] [--client-id <ID>] [--parent-id <PARENT_ID>] [--log-level <LEVEL>]
```
- `--host`: Server address (default: `localhost`)
- `--port`: Server port
- `--client-id`: Unique client identifier (auto-generated if omitted)
- `--parent-id`: Parent client ID for hierarchical setups
- `--log-level`: Logging verbosity

### 3. Multi-Client Mode (tmux Integration)
  The application supports a tree-like client structure. To demonstrate multi-client functionality:
```bash
# Install tmux (Ubuntu/Debian)
sudo apt install tmux

# Start a tmux session
tmux

# Launch the root client inside tmux
java -jar target/p_lab_5-client.jar

# Inside the client, spawn a child client:
spawn_client
```
  The `spawn_client` command automatically creates a new `tmux` window, launches a child client, and registers the parent-child relationship on the server. Terminating a parent client will cascade a shutdown to all its descendants.
  #### tmux Navigation Guide
  Each spawned client runs in a separate tmux window within the same session. Use the following default keybindings to switch between clients:

    Ctrl+b then 0–9: Jump directly to a numbered window.
    Ctrl+b then n / p: Switch to the next / previous window.
    Ctrl+b then w: Open an interactive window selection menu.
    Ctrl+b then d: Detach from the session (clients continue running in the background). Reattach later with tmux attach.

Note: All tmux shortcuts require pressing the Ctrl+b prefix first, releasing both keys, and then pressing the second key.
## Logging Configuration
  Logging verbosity is controlled via the `--log-level` CLI argument. Supported levels:
- `DEBUG`: Detailed network traces, command parsing, registration states, and internal operations
- `INFO`: Standard startup events, connections, command execution, and collection saves
- `WARN`: Invalid requests, timeouts, and validation auto-corrections
- `ERROR`: Critical failures (network drops, deserialization errors, I/O failures)

  Example:
```bash
java -jar target/p_lab_5-server.jar --log-level DEBUG
java -jar target/p_lab_5-client.jar --log-level DEBUG
```

  Logs are managed via Logback and written to:
- `logs/server.log` / `logs/client.log` (full logs)
- `logs/server_error.log` / `logs/client_error.log` (ERROR level only)

Console output can be toggled in `src/logback.xml` via the `LOG_CONSOLE` property.
  #### How to control it:
  You can override these settings at runtime using JVM system properties:

- `-DLOG_CONSOLE=false` → disables console output
- `-DLOG_FILE=false` → disables file output
- `-DLOG_LEVEL=DEBUG` → changes log verbosity (defaults to INFO)

## Command Forwarding & Parent-Child Control
The system supports hierarchical client management. A root client can execute commands on its child processes using the `-c` flag:
```bash
show -c a1b2c3d4
add -c x9y8z7w1 <space_marine_xml>
```

## Available Commands

  | Command | Description |
  |---------|-------------|
  | `add` | Add a new element to the collection |
  | `update <id>` | Update an element by ID |
  | `remove_by_id <id>` | Remove an element by ID |
  | `remove_greater <element>` | Remove all elements exceeding the given one by health |
  | `clear` | Clear the entire collection |
  | `show` | Display all elements, sorted by name |
  | `info` | Show collection metadata (type, creation date, size) |
  | `sum_of_health` | Output the sum of all health values |
  | `min_by_melee_weapon` | Output the element with the minimal melee weapon |
  | `filter_less_than_melee_weapon <weapon>` | Output elements with melee weapon less than specified |
  | `insert_at <index> <element>` | Insert an element at a specific index |
  | `shuffle` | Randomly reorder the collection |
  | `execute_script <path>` | Run a command script from a file |
  | `spawn_client` | Create a child client process |
  | `kill_client <id>` | Terminate a specified child client |
  | `help` | Display command reference |
  | `exit` | Shut down the client/server |
