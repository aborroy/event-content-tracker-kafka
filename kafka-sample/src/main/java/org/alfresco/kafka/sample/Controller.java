package org.alfresco.kafka.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sample REST API to create new messages in default topic.
 * 
 * KAFKA PRODUCER
 * 
 * @author aborroy
 *
 */
@RestController
public class Controller
{

	private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

	@Autowired
	private KafkaTemplate<Object, Object> template;

	@Value("${topic.content.tracking}")
	private String topic;

	/**
	 * Produce a new message to Kafka Topic 
	 * @param contentTrackingMessage Message to be posted
	 */
	@PostMapping(path = "/send")
	public void newMessage(@RequestBody ContentTrackingMessage contentTrackingMessage)
	{

		ListenableFuture<SendResult<Object, Object>> future = template.send(topic, contentTrackingMessage);

		future.addCallback(new ListenableFutureCallback<SendResult<Object, Object>>()
		{

			@Override
			public void onSuccess(SendResult<Object, Object> result)
			{
				LOGGER.info("Sent message='{}' with partition-offset='{}'", 
						result.getProducerRecord().value(),
						result.getRecordMetadata().partition() + "-" + result.getRecordMetadata().offset() + "'");
			}

			@Override
			public void onFailure(Throwable ex)
			{
				LOGGER.info("Unable to send message=[" + contentTrackingMessage + "] due to : " + ex.getMessage());
			}

		});

	}

}