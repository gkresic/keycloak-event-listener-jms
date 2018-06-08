package com.steatoda.keycloak;

import javax.jms.JMSContext;
import javax.jms.Topic;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQTopic;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;

import com.steatoda.keycloak.spi.events.jms.JMSEventListenerProvider;

public class EventTest {

	@BeforeAll
	public static void init() {
		connectionFactory = new ActiveMQConnectionFactory(
			"tcp://127.0.0.1:61616",
			"keycloak",
			"udu3foQu"
		);
		connectionFactory.setClientID("keycloak");
	}
	
	@Test
    public void testConnectionFactory() {
		if (connectionFactory == null)
			throw new NullPointerException();
	}
	
	@Test
    public void testContext() {
		try (JMSContext jmsContext = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
			if (jmsContext == null)
				throw new NullPointerException();
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
		
		Topic topic = new ActiveMQTopic("KeycloakEvent");

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
		
		Topic adminTopic = new ActiveMQTopic("KeycloakAdminEvent");

		AdminEvent event = new AdminEvent();
		event.setResourceType(ResourceType.USER);
		event.setOperationType(OperationType.DELETE);
		event.setRealmId("DontUseRealmID");
		event.setTime(System.currentTimeMillis());
		event.setResourcePath("<TEST> 0e04afad-19b1-4906-9ed4-4ab1097b10a2");
		event.setRepresentation("<TEST> gordan.kresic@steatoda.com");
		
		JMSEventListenerProvider provider = new JMSEventListenerProvider(null, connectionFactory);
		provider.setAdminTopic(adminTopic);
		
		provider.onEvent(event, false);
		
	}

	@AfterAll
	public static void close() {
		connectionFactory.close();
	}
	
	private static ActiveMQConnectionFactory connectionFactory;
	
}
