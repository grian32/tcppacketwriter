# tcppacketwriter

This is a simple CLI tool that builds and reads packets via TCP.

## Arguments

Takes in two arguments, ip & port, i.e
`tcppacketwriter 127.0.0.1 4422`

## Commands
Once the application has started and successfully connected, you can then input various commands:

- rc: Reconnects to the specified server
- w: Writes the built packet to the specified server
- q: Quits the program
- byte [byte]: Writes the specified byte to the packet
- str [str]: Writes the string to the packet, this prepends the length of the string as in int.

Example Usage:
```
byte 0x01
str hey
w

rc
byte 1
str hello
w

q
```