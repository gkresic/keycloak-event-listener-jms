package com.steatoda.keycloak.spi.events.jms;

import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.MapMessage;
import javax.jms.Topic;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;

public class JMSEventListenerProvider implements EventListenerProvider {

	public JMSEventListenerProvider(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}
	
	public Topic getTopic() { return topic; }
	public void setTopic(Topic topic) { this.topic = topic; }
	public Topic getAdminTopic() { return adminTopic; }
	public void setAdminTopic(Topic adminTopic) { this.adminTopic = adminTopic; }
	public Set<EventType> getExcludedEvents() { return excludedEvents; }
	public void setExcludedEvents(Set<EventType> excludedEvents) { this.excludedEvents = excludedEvents; }

	@Override
	public void onEvent(Event event) {
		
		if (topic == null)
			return;
		
		if (excludedEvents != null && excludedEvents.contains(event.getType()))
			return;

//		if (event.getUserId() == null)
//			return;

		try (JMSContext jmsContext = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
			MapMessage message = jmsContext.createMapMessage();
			// properties (filterable on MQ)
			message.setStringProperty("type", event.getType().toString());
			message.setStringProperty("realmId", event.getRealmId());
			message.setLongProperty("time", event.getTime());
			// fields
			message.setString("userId", event.getUserId());
			jmsContext.createProducer().send(topic, message);
		} catch (Exception e) {
			// yes, System.out.println :-) This gets logged back via jboss-logging into the main server log,
			// and as this is deployed as a module, we don't get in trouble with classpath/module dependencies.
			System.out.println(String.format("WARNING: Couldn't publish event to MQ. Event: %s. Cause: %s", event, e.getMessage()));
		}

	}

	@Override
	public void onEvent(AdminEvent event, boolean includeRepresentation) {

		if (adminTopic == null)
			return;
		
		try (JMSContext jmsContext = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
			MapMessage message = jmsContext.createMapMessage();
			// properties (filterable on MQ)
			message.setStringProperty("resourceType", event.getResourceType().toString());
			message.setStringProperty("operationType", event.getOperationType().toString());
			message.setStringProperty("realmId", event.getRealmId());
			message.setLongProperty("time", event.getTime());
			// fields
			message.setString("resourcePath", event.getResourcePath());
			message.setString("representation", event.getRepresentation());
			jmsContext.createProducer().send(adminTopic, message);
		} catch (Exception e) {
			// yes, System.out.println :-) This gets logged back via jboss-logging into the main server log,
			// and as this is deployed as a module, we don't get in trouble with classpath/module dependencies.
			System.out.println(String.format("WARNING: Couldn't publish admin event to MQ. Event: %s. Cause: %s", event, e.getMessage()));
		}

	}

	@Override
	public void close() {}

	private final ConnectionFactory connectionFactory;

	private Topic topic = null;
	private Topic adminTopic = null;
	private Set<EventType> excludedEvents = null;

}
