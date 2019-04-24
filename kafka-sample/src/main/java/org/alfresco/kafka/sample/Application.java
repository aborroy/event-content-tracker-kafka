package org.alfresco.kafka.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

/**
 * Sample application listening to messages from a topic in Apache Kafka.
 * 
 * Messages are sent using String Json format described in "ContentTrackingMessage" class. 
 * 
 * "listen" method receives every new message posted.
 * 
 * "readAll" method receives every message posted from the beginning. 
 * 
 * @author aborroy
 *
 */
@SpringBootApplication
public class Application
{

	private final Logger logger = LoggerFactory.getLogger(Application.class);
	
	public static void main(String[] args)
	{
		SpringApplication.run(Application.class, args);
	}

	/**
	 * Kakfa listener settings.
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
	 * @return Kafka Message
	 */
	@Bean
	public RecordMessageConverter converter()
	{
		return new StringJsonMessageConverter();
	}

	/**
	 * Receives a new message sent to default topic
	 * @param contentTrackingMessage
	 */
	@KafkaListener(id = "${group.live}", topics = "${topic.content.tracking}")
	public void listen(ContentTrackingMessage contentTrackingMessage)
	{
		logger.info("Received: " + contentTrackingMessage);
	}

	/**
	 * Receives every message sent to default topic from the beginning
	 * @param contentTrackingMessage
	 */
	@KafkaListener(id = "${group.history}", topicPartitions =
	    { @TopicPartition(topic = "${topic.content.tracking}", 
	                      partitionOffsets = @PartitionOffset(partition = "0", initialOffset = "0")) })
	public void readAll(ContentTrackingMessage contentTrackingMessage)
	{
		logger.info("[READ ALL] Received: " + contentTrackingMessage);
	}

}