package com.hazelcast.jet.examples.monitor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.YamlClientConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;

@Configuration
public class ApplicationConfig {
	
	@Bean
	public ClientConfig clientConfig() throws Exception {
		ClientConfig clientConfig =
                new YamlClientConfigBuilder("hazelcast-client.yaml").build();
		return clientConfig;
	}

	@Bean
	public JetInstance jetInstance(ClientConfig clientConfig) {
		return Jet.newJetClient(clientConfig);
	}

	@Bean
	public HazelcastInstance hazelcastInstance(JetInstance jetInstance) {
		return jetInstance.getHazelcastInstance();
	}
}
