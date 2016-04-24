# java-chat-server
Chat server developed as final project for Networking class. Requires Intellij IDE because of GUI dependencies.

# GUI Utility
Utilized the Intellij GUI builder to get the structure of our chat server in order before adding in all of the extra logic.

# Build Instructions
Run the ChatWindow class located inside of the src/Client/ directory from the project root. Pass the ip address of the host you are trying to connect to. Next run the ChatServer class inside src/Server/ from the project root. You will have to create multiple chat rooms on different machines since our thread servicing the ChatWindow requires static access calls. You can also telnet in to see the functionality as well without needing to create multiple windows.

# Thread and File Interactions

### Server ###

ChatServer                 ->    ClientManagerConnection

ClientManagerConnection    ->    RoomConnection

ClientManagerConnection    ->    ClientVerificationConnection

RoomConnection             ->    ClientManagerConnection

RoomConnection             ->    BroadcastConnection

BroadcastConnection        ->    (Writes to client buffer)

### Client ###

ChatWindow                 ->    ChatController

ChatController             ->    (Writes to server buffer)

# Change Log

### v1.0 ###

* 4/24/16
* Client gui implemented and talking with server
* Server rooms finished and able to parse and send message correctly
* Basic protocol put into place

### v0.20 ###

* 4/16/16
* Code moved from server: commandCodes, clientDetails, pendingClients
* verificaiont -> clientManager
* executor in clientManager
* signals semaphore for server and master slave to signal clientManager
* MasterSlave thread construction moved to clientManager
* Server.Client class moved inside clientManager

### v0.11 ###

* 4/13/16
* Server.Client class
* Verification thread
* Logging idea
* Structure redesign
* Server.Client details hash map

### v0.10 ###

* 4/9/16
* Mapping structure
* Designing variables
