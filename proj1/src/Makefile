OUT_DIR =../out/production/proj1

# RUN Peers
peer1:
	java -cp $(OUT_DIR) Peer.Peer 1.0 1 1 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003

peer2:
	java -cp $(OUT_DIR) Peer.Peer 1.0 2 2 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003

# RUN TestApp

backup:
	java -cp $(OUT_DIR) TestApp 1 BACKUP carlota.jpg 2

# BUILD

# build test app
build_test:
	javac -d ../out/production/proj1 *.java

## build peer
build_peer:
	javac -d ../out/production/proj1 Peer/*.java

# build all
build: build_test build_peer

# clean
clean:
	rm -rf ../out

# RMI
rmi:
	rmiregistry -J-Djava.class.path=$(OUT_DIR) &

stop_rmi:
	killall rmiregistry

