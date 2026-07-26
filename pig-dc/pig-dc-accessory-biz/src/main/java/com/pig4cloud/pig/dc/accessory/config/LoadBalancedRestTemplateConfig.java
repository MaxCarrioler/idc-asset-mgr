package com.pig4cloud.pig.dc.accessory.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.ArrayList;

@Configuration
public class LoadBalancedRestTemplateConfig {

	@Bean
	@LoadBalanced
	RestTemplate loadBalancedRestTemplate(ObjectProvider<ObjectMapper> objectMapperProvider) {
		ObjectMapper mapper = objectMapperProvider.getObject().copy();
		SimpleModule longAsStringModule = new SimpleModule("PigLongStringDeserializer");
		longAsStringModule.addDeserializer(Long.class, new StringCompatibleLongDeserializer());
		longAsStringModule.addDeserializer(long.class, new StringCompatibleLongDeserializer());
		mapper.registerModule(longAsStringModule);

		MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter(mapper);
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setMessageConverters(new ArrayList<>(restTemplate.getMessageConverters()));
		restTemplate.getMessageConverters().removeIf(MappingJackson2HttpMessageConverter.class::isInstance);
		restTemplate.getMessageConverters().add(0, jsonConverter);
		restTemplate.getInterceptors().add(authorizationForwardingInterceptor());
		return restTemplate;
	}

	private ClientHttpRequestInterceptor authorizationForwardingInterceptor() {
		return (request, body, execution) -> {
			ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			if (attrs != null) {
				String authorization = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
				if (authorization != null && !authorization.isBlank()
						&& request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION) == null) {
					request.getHeaders().set(HttpHeaders.AUTHORIZATION, authorization);
				}
			}
			return execution.execute(request, body);
		};
	}

	private static class StringCompatibleLongDeserializer extends StdDeserializer<Long> {

		StringCompatibleLongDeserializer() {
			super(Long.class);
		}

		@Override
		public Long deserialize(JsonParser parser, DeserializationContext context) throws IOException {
			JsonToken token = parser.currentToken();
			if (token == JsonToken.VALUE_STRING) {
				String text = parser.getText();
				if (text == null || text.isBlank()) {
					return null;
				}
				return Long.parseLong(text);
			}
			if (token == JsonToken.VALUE_NUMBER_INT) {
				return parser.getLongValue();
			}
			return (Long) context.handleUnexpectedToken(Long.class, parser);
		}

	}

}
