OUT_DIR = ../out
HOST = localhost
PORT = 8080

build:
	@javac -d $(OUT_DIR) *.java

server:
	@java -cp $(OUT_DIR) Server $(PORT)

client:
	@java -cp $(OUT_DIR) Client $(HOST) $(PORT) fe.up.pt 196.168.0.1
