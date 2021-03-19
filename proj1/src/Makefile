OUT_DIR = ../out/production/proj1

all: FORCE
	javac -d $(OUT_DIR) *.java

peer:
	java -cp $(OUT_DIR) Peer "$1" "$2" "$3" 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003

peer1: clean all
	rmiregistry -J-Djava.class.path=$(OUT_DIR) &
	java -cp $(OUT_DIR) Peer 1.0 1 1 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003

clean:
	killall rmiregistry
	rm -rf $(OUT_DIR)

FORCE: