OUT_DIR = ../out/production/proj1

build:
	javac -d $(OUT_DIR) *.java

rmi:
	rmiregistry -J-Djava.class.path=$(OUT_DIR) &

peer1:
	java -cp $(OUT_DIR) Peer 1.0 1 1 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003

peer2:
	java -cp $(OUT_DIR) Peer 1.0 2 2 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003

clean:
	rm -rf $(OUT_DIR)

backup:
	java -cp $(OUT_DIR) TestApp 1 BACKUP carlota.jpg 2

stop_rmi:
	killall rmiregistry

