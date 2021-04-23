all:
	javac *java

client:
	javac Client.java
	java Client localhost RemoteInterface register sigarra.up.pt 193.137.35.140
	java Client localhost RemoteInterface lookup sigarra.up.pt

server:
	javac Server.java
	java Server RemoteInterface

clean:
	del *.class