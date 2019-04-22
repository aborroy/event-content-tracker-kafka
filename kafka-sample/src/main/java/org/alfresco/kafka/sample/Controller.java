package org.alfresco.kafka.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sample REST API to create new messages in default topic.
 * 
 * @author aborroy
 *
 */
@RestController
public class Controller
{

	@Autowired
	private KafkaTemplate<Object, Object> template;
	
	@Value("${topic.content.tracking}")
	private String topic;

	@PostMapping(path = "/send")
	public void newMessage(@RequestBody ContentTrackingMessage contentTrackingMessage)
	{
		this.template.send(topic, contentTrackingMessage);
	}

}