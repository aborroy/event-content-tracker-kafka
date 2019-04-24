package org.alfresco.kafka.sample;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.messaging.handler.annotation.Header;

/**
 * Sample application listening to messages from a topic in Apache Kafka.
 * 
 * KAFKA CONSUMER
 * 
 * Messages are sent using String JSON format described in "ContentTrackingMessage" class.
 * 
 * "listen" method receives every new message posted.
 * 
 * "fromOffset" method receives every message posted from offset specified.
 * 
 * "readAll" method receives every message posted from the beginning.
 * 
 * @author aborroy
 *
 */
@SpringBootApplication
public class Application
{

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	/**
	 * Apache Kafka offset to start listening to
	 */
	@Value("${from.offset}")
	private String fromOffset;

	/**
	 * Apache Kafka partition to listen to
	 */
	@Value("${from.partition}")
	private String fromPartition;
	
	/**
	 * Start web application
	 * 
	 * @param args --from-offset=N, where N is the starting Apache Kafka offset to start listening to
	 */
	public static void main(String[] args)
	{
		SpringApplication.run(Application.class, args);
	}

	/**
	 * Kakfa Factory Listener settings.
	 * 
	 * @param configurer
	 * @param kafkaConsumerFactory
	 * @param template
	 * @return KafkaListenerContainerFactory
	 */
	@Bean
	public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
			ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
			ConsumerFactory<Object, Object> kafkaConsumerFactory, KafkaTemplate<Object, Object> template)
	{
		ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
		configurer.configure(factory, kafkaConsumerFactory);
		factory.setErrorHandler(new SeekToCurrentErrorHandler(new DeadLetterPublishingRecoverer(template), 3));
		return factory;
	}

	/**
	 * Message converter strategy.
	 * 
	 * @return Record Message Converter strategy, String to Json
	 */
	@Bean
	public RecordMessageConverter converter()
	{
		return new StringJsonMessageConverter();
	}

	/**
	 * Receives every new message posted to default topic 
	 * 
	 * @param contentTrackingMessage Message received
	 * @param partitions Partition number
	 * @param offsets Offset number
	 */
	@KafkaListener(id = "${group.live}", topics = "${topic.content.tracking}")
	public void listen(ContentTrackingMessage contentTrackingMessage,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
			@Header(KafkaHeaders.OFFSET) List<Long> offsets)
	{

		for (int i = 0; i < partitions.size(); i++)
		{
			LOGGER.info("LIVE: Received message='{}' with partition-offset='{}'", 
					contentTrackingMessage,
					partitions.get(i) + "-" + offsets.get(i));
		}

	}

	/**
	 * Receives every message sent to default topic from the offset specified fromOffset
	 * 
	 * "fromOffset" can be set as parameter in command line: 
	 *     java -jar target/kafka-sample-0.0.1-SNAPSHOT.jar --from.offset=1
	 * 
	 * @param contentTrackingMessage Message received
	 * @param partitions Partition number
	 * @param offsets Offset number
	 */
	@KafkaListener(id = "${group.from}", topicPartitions =
	{ 
	    @TopicPartition(topic = "${topic.content.tracking}", 
	        partitionOffsets = @PartitionOffset(partition = "${from.partition}", initialOffset = "${from.offset}")) 
	})
	public void fromOffset(ContentTrackingMessage contentTrackingMessage,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
			@Header(KafkaHeaders.OFFSET) List<Long> offsets)
	{
		for (int i = 0; i < partitions.size(); i++)
		{
			LOGGER.info("FROM OFFSET '{}': Received message='{}' with partition-offset='{}'",
					fromOffset,
					contentTrackingMessage,
					partitions.get(i) + "-" + offsets.get(i));
		}
		
	}

	/**
	 * Receives every message sent to default topic from the beginning
	 * 
	 * @param contentTrackingMessage Message received
	 * @param partitions Partition number
	 * @param offsets Offset number
	 */
	@KafkaListener(id = "${group.history}", topicPartitions =
	{ 
	    @TopicPartition(topic = "${topic.content.tracking}", 
	    		partitionOffsets = @PartitionOffset(partition = "${from.partition}", initialOffset = "0")) 
	})
	public void readAll(ContentTrackingMessage contentTrackingMessage,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
			@Header(KafkaHeaders.OFFSET) List<Long> offsets)
	{
		for (int i = 0; i < partitions.size(); i++)
		{
			LOGGER.info("FROM BEGINNING: Received message='{}' with partition-offset='{}'",
					contentTrackingMessage,
					partitions.get(i) + "-" + offsets.get(i));
		}
		
	}

}