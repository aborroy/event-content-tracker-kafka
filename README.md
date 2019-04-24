# Event Content Tracker PoC with Apache Kafka

**Description**

This project is related with Alfresco Search Services event oriented tracking spike.

SOLR indexes require a full history of events (transactions) to be rebuilt from scratch, so message brokers with no storage (like ActiveMQ) are not recommended for this scenario. Since Amazon is providing an [MSK](https://aws.amazon.com/msk/) (Amazon Managed Streaming for Kafka) service and [Apache Kafka](https://kafka.apache.org) is a streaming data store, this approach seems to fit the requirements for the PoC.

**Components**

* `docker-compose.yml` Based in default `acs-deployment`, includes `zookeeper` and `kafka` services from [wurstmeister](https://github.com/wurstmeister). The Alfresco Stack is not used, but it has been included to test compatibility with Kafka services

* `kafka-sample` is a Spring Boot Application using `spring-kafka` (to produce and consume Kafka events) and `spring-boot-starter-web` (to provide a simple REST API interface)

**Usage**

Start Docker Compose.

```bash
$ docker-compose up --build
```

Zookeeper service will be available at *localhost* in port 2181 and Kafka will be available at *localhost* in port 9092.

Start Spring Boot REST API app.

```bash
$ cd kafka-sample
$ java -jar target/kafka-sample-0.0.1-SNAPSHOT.jar
...
2019-04-22 12:41:51.428  INFO : Started Application in 2.189 seconds (JVM running for 2.521)
```

Web Application will be available at *localhost* in port 9999.

Create a new message for the topic using cURL.

```bash
$ curl -X POST   http://localhost:9999/send -H 'Content-Type: application/json' -d '{
    "txId":"1",
    "nodeId":"2"
}'
```

Three new entries in Spring Boot Application log will appear : one for the "BEGINNING" listener (reading every event from offset 0), another one for the "FROM OFFSET" listener (reading every event from an `offset` number) and another one for the "LIVE" listener (reading every new event).

```
FROM BEGINNING: Received message='ContentTrackingMessage [txId=1, nodeId=2]' with partition-offset='0-0'
FROM OFFSET '0': Received message='ContentTrackingMessage [txId=1, nodeId=2]' with partition-offset='0-0'
LIVE: Received message='ContentTrackingMessage [txId=1, nodeId=2]' with partition-offset='0-0'
```

"FROM OFFSET" is listening from offset 0 by default, but it can be set the starting offset by command line parameter:

```bash
$ java -jar target/kafka-sample-0.0.1-SNAPSHOT.jar --from.offset=1
```

**Configuration**

*Zookeeper* and *Kafka* parameters can be configured in `docker-compose.yml`

```
zookeeper:
  image: wurstmeister/zookeeper
  ports:
    - 2181:2181

kafka:
  image: wurstmeister/kafka
  ports:
    - 9092:9092
  environment:
    - KAFKA_ADVERTISED_HOST_NAME=localhost
    - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
```

*Spring Boot* web application parameters can be configured in `kafka-sample/src/main/resources/application.properties` file.

```
# REST API Tomcat port
server.port=9999

# Kafka server settings, including "zk" (zookeeper) server
spring.cloud.stream.kafka.binder.brokers=localhost
spring.cloud.stream.kafka.binder.defaultBrokerPort=9092
spring.cloud.stream.kafka.binder.zkNodes=localhost
spring.cloud.stream.kafka.binder.defaultZkPort=2181

# Default serializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer

# Groups and topics configuration
group.live=groupLive
group.from=groupFrom
group.history=groupHistory
topic.content.tracking=topicContentTracking

# Default from offset and default partition, can be overwritten from command line
# java -jar target/kafka-sample-0.0.1-SNAPSHOT.jar --from.partition=0 --from.offset=1
from.partition=0
from.offset=0
```

## Building

Java 8 and Maven are required.

```bash
$ cd kafka-sample

$ mvn clean package

$ ls -la target/kafka-sample-0.0.1-SNAPSHOT.jar
-rw-r--r--  1 aborroy  staff  22126052 22 Apr 12:52 target/kafka-sample-0.0.1-SNAPSHOT.jar
```
