# SDIS Project

SDIS Project for group T5G02.

Group members:

1. Ana Inês Barros (up201806593@fe.up.pt)
2. Eduardo Correia (up201806433@fe.up.pt)

## How to run

### Compile

To compile the project code, execute one of the following commands in the shell from the root directory of the source tree.

```shell
../scripts/compile.sh
```

### Peer

To run a peer, execute the following commands in the shell from the root directory of the build tree.

```shell
../../scripts/peer.sh <version> <peer_id> <svc_access_point> <mc_addr> <mc_port> <mdb_addr> <mdb_port> <mdr_addr> <mdr_port>
```

### Test

To test the application, execute the following commands in the shell from the root directory of the build tree.

```shell
../../scripts/test.sh <peer_ap> BACKUP|RESTORE|DELETE|RECLAIM|STATE [<opnd_1> [<optnd_2]]
```

### Cleanup

To cleanup the directory tree used by a peer for its storage, execute the following commands in the shell from the root directory of the build tree.

```shell
../../scripts/test.sh <peer_id>
```

### Setup

There isn't any *setup* script, because our code already performs the necessary setup when executing.

## Project structure

- 📁 **[doc](doc)** - Relevant document files.
- 📁 **[scripts](scripts)** - Shell scripts to run the project.
- 📁 **[src](src)** - Source code for the project.
    - 📁 **[build](src/build)** - Compiled project files.
        - 📁 **[filesystem](src/build/filesystem)** - Filesystem used by the backup service.
    - 📁 **[channels](src/channels)** - Channels to be used by peers for communication.
    - 📁 **[handlers](src/handlers)** - Message handlers.
    - 📁 **[messages](src/messages)** - Control messages.
    - 📁 **[peer](src/peer)** - Class to represent a peer.
        - 📁 **[storage](src/peer/storage)** - Class to represent a peer's storage.
    - 📁 **[subprotocols](src/subprotocols)** - Subprotocols used by the backup service.
    - 📁 **[test](src/test)** - Client interface to test the application.
    - 📁 **[utils](src/utils)** - Miscellaneous utilities.
