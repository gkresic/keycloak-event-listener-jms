package com.steatoda.keycloak;

import javax.jms.JMSContext;
import javax.jms.Topic;

import org.junit.After;
import org.junit.Before;
import org.junit.Assert;
import org.junit.Test;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQTopic;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;

import com.steatoda.keycloak.spi.events.jms.JMSEventListenerProvider;

public class EventTest {

	@Before
	public void init() {
		connectionFactory = new ActiveMQConnectionFactory(
			System.getProperty("TEST_MQ_URL"),
			System.getProperty("TEST_MQ_USER"),
			System.getProperty("TEST_MQ_PASSWORD")
		);
		connectionFactory.setClientID("keycloak-test");
	}
	
	@Test
    public void testConnectionFactory() {
		Assert.assertNotNull("Failed to initialize ActiveMQConnectionFactory", connectionFactory);
	}
	
	@Test
    public void testContext() {
		try (JMSContext jmsContext = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
			Assert.assertNotNull("Failed to initialize JMSContext", jmsContext);
		}
	}

    //@Test
    public void testPing() {
		Topic topic = new ActiveMQTopic("ping");
		try (JMSContext jmsContext = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
			jmsContext.createProducer().send(topic, jmsContext.createTextMessage("Yo!"));
		}
	}
	
	//@Test
    public void testUserEvent() {
		
		Topic topic = new ActiveMQTopic(System.getProperty("TEST_MQ_TOPIC_USER_EVENT"));

		Event event = new Event();
		event.setType(EventType.VERIFY_EMAIL);
		event.setRealmId("DontUseRealmID");
		event.setTime(System.currentTimeMillis());
		event.setUserId("<TEST> 0e04afad-19b1-4906-9ed4-4ab1097b10a2");
		
		JMSEventListenerProvider provider = new JMSEventListenerProvider(null, connectionFactory);
		provider.setTopic(topic);
		
		provider.onEvent(event);
		
	}
	
	//@Test
    public void testAdminEvent() {
		
		Topic adminTopic = new ActiveMQTopic(System.getProperty("TEST_MQ_TOPIC_ADMIN_EVENT"));

		AdminEvent event = new AdminEvent();
		event.setResourceType(ResourceType.USER);
		event.setOperationType(OperationType.DELETE);
		event.setRealmId("DontUseRealmID");
		event.setTime(System.currentTimeMillis());
		event.setResourcePath("<TEST> 0e04afad-19b1-4906-9ed4-4ab1097b10a2");
		event.setRepresentation("<TEST> user@foo.com");
		
		JMSEventListenerProvider provider = new JMSEventListenerProvider(null, connectionFactory);
		provider.setAdminTopic(adminTopic);
		
		provider.onEvent(event, false);
		
	}

	@After
	public void close() {
		connectionFactory.close();
	}
	
	private static ActiveMQConnectionFactory connectionFactory;
	
}
