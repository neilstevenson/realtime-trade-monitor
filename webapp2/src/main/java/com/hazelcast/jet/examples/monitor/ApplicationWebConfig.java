package com.hazelcast.jet.examples.monitor;
import java.util.Currency;
import java.util.Locale;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.web.WebFilter;

@Configuration
public class ApplicationWebConfig {

	@Autowired
	private HazelcastInstance hazelcastInstance;

	@Bean
	public WebFilter webFilter() {
		Properties properties = new Properties();

		properties.put("map-name", "jsessionid");
		properties.put("instance-name", this.hazelcastInstance.getName());
		properties.put("sticky-session", "false");
		properties.put("use-client", "true");

		return new WebFilter(properties);
	}

	@Bean(name="currencySymbol")
	public String currencySymbol() {
		try {
			Locale locale = Locale.getDefault();
			if (locale.getCountry() != null && locale.getCountry().length() > 0) {
				return Currency.getInstance(locale).getSymbol();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Currency.getInstance("GBP").getSymbol();
	}
}