package com.hotel.booking.gateway;

import java.util.ArrayList;
import java.util.List;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServiceApplication {

	@Autowired
	RouteDefinitionLocator locator;
	
	public static void main(String[] args) {

		SpringApplication.run(GatewayServiceApplication.class, args);
	}

	@Bean
	public List<GroupedOpenApi> apis() {
		List<GroupedOpenApi> groups = new ArrayList<>();
		List<RouteDefinition> definitions = locator.getRouteDefinitions().collectList().block();
		assert definitions != null;
		definitions.stream().filter(routeDefinition -> routeDefinition.getId().matches("hotel-services"))
				.forEach(routeDefinition -> {
					String name = routeDefinition.getId();
					groups.add(GroupedOpenApi.builder().pathsToMatch("/" + name + "/**").group(name).build());
				});

		return groups;
	}

}
