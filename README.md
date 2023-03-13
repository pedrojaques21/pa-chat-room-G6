# pa-chat-room

## The Project

The proposed project, defined hereafter as "pa-chat-room", aims at creating a message server between several clients, messages that are to be transmitted via broadcasting.

In general, the pa-chat-room should consist of a server, to which one or more clients can connect. The implementation must allow that when a client sends a message to the server, all clients connected to the server at that moment will receive that message.

The highest priority for this program (and therefore for the server) is to ensure data integrity and availability. Therefore, some implementation decisions should be made so that the server is always available. So the server should, for example, create child threads for all the tasks that could potentially block the main thread.

The program has a server/server.log file, which should store all the interactions that happened during the execution: messages, client input and output, clients waiting for a slot and associated timestamps.

All the settings you consider necessary for the server should be read from the server/server.config file shared in the base project repository. Any additional parameterization that each group finds necessary should be added to this configuration file.
