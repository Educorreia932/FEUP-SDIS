MAKEFLAGS += -j 4

OUT_DIR = ../build

all: rmi peer1 peer2 backup

build:
	javac -d $(OUT_DIR) test/*.java
	javac -d $(OUT_DIR) peer/*.java

clean:
	rm -rf $(OUT_DIR)
	rm -rf ../filesystem

rmi:
	killall --quiet rmiregistry || true
	rmiregistry -J-Djava.class.path=$(OUT_DIR)

peer1:
	java -cp $(OUT_DIR) peer.Peer 1.0 1 1 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003

peer2:
	java -cp $(OUT_DIR) peer.Peer 1.0 2 2 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003

peer3:
	java -cp $(OUT_DIR) peer.Peer 1.0 3 3 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003

backup:
	java -cp $(OUT_DIR) test.TestApp 1 BACKUP test.txt 1

restore:
	java -cp $(OUT_DIR) test.TestApp 1 RESTORE test.txt

delete:
	java -cp $(OUT_DIR) test.TestApp 1 DELETE oinc.jpg

reclaim:
	java -cp $(OUT_DIR) test.TestApp 2 RECLAIM 0

state:
	java -cp $(OUT_DIR) test.TestApp 1 STATE