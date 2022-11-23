# Definition of the socket json


## From Server

|Key|Data Type|Explanation|
|:-:|:-:|-|
|room|string|Name of the chatting room|
|sender|string|Sender name|
|system|bool|Whether the message is a system message|
|time|string|Server time|
|message|string|Message to send|


## From Client

|Key|Data Type|Explanation|
|:-:|:-:|-|
|sender|string|Sender name|
|status|int|-1: Disconnect<br/>0: Check Connection<br/>1: With message|
|message|string|Message to send|
