package com.steatoda.keycloak.spi.events.jms;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.jms.Topic;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQTopic;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ServerInfoAwareProviderFactory;

public class JMSEventListenerProviderFactory implements EventListenerProviderFactory, ServerInfoAwareProviderFactory {

	@Override
	public Map<String, String> getOperationalInfo() {
		Map<String, String> info = new LinkedHashMap<>();
		info.put("connectionFactory", connectionFactory.toString());
		if (topic != null)
			info.put("topic", topic.toString());
		if (adminTopic != null)
			info.put("adminTopic", adminTopic.toString());
		return info;
	}

	@Override
	public void init(Config.Scope config) {
		// connection factory
		String url = config.get("url");
		if (url == null)
			throw new RuntimeException("JMSEventListenerProviderFactory misses url param");
		connectionFactory = new ActiveMQConnectionFactory(url);
		// username
		String username = config.get("username");
		if (username != null)
			connectionFactory.setUser(username);
		// password
		String password = config.get("password");
		if (password != null)
			connectionFactory.setPassword(password);
		// topic
		String topicAddress = config.get("topic");
		if (topicAddress != null)
			topic = new ActiveMQTopic(topicAddress);
		// admin topic
		String adminTopicAddress = config.get("adminTopic");
		if (adminTopicAddress != null)
			adminTopic = new ActiveMQTopic(adminTopicAddress);
		// excludes
		String[] excludes = config.getArray("exclude-events");
		if (excludes != null)
			excludedEvents = Stream.of(excludes).map(exclude -> EventType.valueOf(exclude)).collect(Collectors.toSet());
		System.out.println(String.format("JMSEventListenerProviderFactory initialized: %s %s %s", connectionFactory, topic, adminTopic));
	}

	@Override
	public void postInit(KeycloakSessionFactory factory) {}

	@Override
	public EventListenerProvider create(KeycloakSession session) {
		JMSEventListenerProvider provider = new JMSEventListenerProvider(connectionFactory);
		provider.setTopic(topic);
		provider.setAdminTopic(adminTopic);
		provider.setExcludedEvents(excludedEvents);
		return provider;
	}

	@Override
	public void close() {
		connectionFactory.close();
	}

	@Override
	public String getId() {
		return "steatoda-jms";
	}

	private Set<EventType> excludedEvents = null;
	private ActiveMQConnectionFactory connectionFactory = null;
	private Topic topic = null;
	private Topic adminTopic = null;

}
