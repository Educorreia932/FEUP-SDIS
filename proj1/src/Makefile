OUT_DIR =../out/production/proj1

build:
	javac -d ../out/production/proj1 *.java
	javac -d ../out/production/proj1 Peer/*.java

clean:
	rm -rf $(OUT_DIR)
	rm -rf ../filesystem

rmi:
	killall rmiregistry
	rmiregistry -J-Djava.class.path=$(OUT_DIR)

peer1:
	java -cp $(OUT_DIR) Peer.Peer 1.0 1 1 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003

peer2:
	java -cp $(OUT_DIR) Peer.Peer 1.0 2 2 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003

backup:
	java -cp $(OUT_DIR) TestApp 1 BACKUP carlota.jpg 2
