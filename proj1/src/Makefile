MAKEFLAGS += -j 4

OUT_DIR = ../out/production/proj1

all: rmi peer1 peer2 backup

build:
	javac -d ../out/production/proj1 *.java
	javac -d ../out/production/proj1 peer/*.java

clean:
	rm -rf $(OUT_DIR)
	rm -rf ../filesystem

rmi:
	killall --quiet rmiregistry || true # This is necessary since killall would return false if no processes were running
	sleep 1
	rmiregistry -J-Djava.class.path=$(OUT_DIR) &

peer1:
	java -cp $(OUT_DIR) peer.Peer 1.0 1 1 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003

peer2:
	java -cp $(OUT_DIR) peer.Peer 1.0 2 2 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003

peer3:
	java -cp $(OUT_DIR) peer.Peer 1.0 3 3 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003

backup:
	java -cp $(OUT_DIR) TestApp 1 BACKUP test.txt 1

restore:
	java -cp $(OUT_DIR) TestApp 1 RESTORE test.txt
