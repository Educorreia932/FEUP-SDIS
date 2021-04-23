OUT_DIR = out
HOST = localhost
PORT = 8080
CYPHER_SUITES = SSL_RSA_WITH_RC4_128_MD5 SSL_RSA_WITH_RC4_128_SHA SSL_RSA_WITH_NULL_MD5 TLS_RSA_WITH_AES_128_CBC_SHA TLS_DHE_RSA_WITH_AES_128_CBC_SHA TLS_DHE_DSS_WITH_AES_128_CBC_SHA TLS_DH_anon_WITH_AES_128_CBC_SHA

build:
	@javac -d $(OUT_DIR) *.java

client:
	@java -cp $(OUT_DIR) \
		-Djavax.net.ssl.keyStore=../keys/server.keys \
		-Djavax.net.ssl.keyStorePassword=123456 \
		-Djavax.net.ssl.trustStore=../keys/truststore \
		-Djavax.net.ssl.trustStorePassword=123456 \
		SSLClient $(HOST) $(PORT) REGISTER fe.up.pt 196.168.0.1 $(CYPHER_SUITES)

server:
	@java -cp $(OUT_DIR) \
		-Djavax.net.ssl.keyStore=../keys/client.keys \
		-Djavax.net.ssl.keyStorePassword=123456 \
		-Djavax.net.ssl.trustStore=../keys/truststore \
		-Djavax.net.ssl.trustStorePassword=123456 \
		SSLServer $(PORT) $(CYPHER_SUITES)
