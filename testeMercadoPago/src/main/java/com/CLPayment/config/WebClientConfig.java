package com.CLPayment.config;

import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContextBuilder;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {
    @Value("${MP.AccessToken}")
    private String mpAccessToken;

    @Value("${webhook}")
    private String webhook;

    @Bean
    WebClient preferenceWebClient() {
	return WebClient.builder()
			.baseUrl("https://api.mercadopago.com/checkout/preferences")
			.defaultHeaders(h -> h.setBearerAuth(mpAccessToken))
			.build();
    }

    @Bean
    WebClient paymentWebClient() {
	return WebClient.builder()
			.baseUrl("https://api.mercadopago.com/v1/payments")
			.defaultHeaders(h -> h.setBearerAuth(mpAccessToken))
			.build();
    }

    @Bean
    WebClient cLMainService() {
	return WebClient.builder()
			.baseUrl(webhook)
			.build();
    }

    // WebClient específico para a rota base que requer autenticação com certificado
    @Bean
    WebClient efiWebClient() throws Exception {
	String p12Password = ""; // O certificado não possui senha

	// Carregar o KeyStore com o certificado .p12
	KeyStore keyStore = KeyStore.getInstance("PKCS12");

	// Usando getResourceAsStream para carregar o arquivo do classpath
	try (InputStream keyStoreStream = getClass().getClassLoader()
						    .getResourceAsStream("homologacao-625689-CL saque homologação.p12")) {
	    if (keyStoreStream == null) {
		throw new IllegalArgumentException("Certificado não encontrado no classpath");
	    }
	    keyStore.load(keyStoreStream, p12Password.toCharArray());
	}

	// Configurar o KeyManagerFactory com o KeyStore carregado
	KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
	keyManagerFactory.init(keyStore, p12Password.toCharArray());

	// Construir o SslContext do Netty usando o KeyManagerFactory
	HttpClient httpClient = HttpClient.create()
					  .secure(sslContextSpec -> {
					      try {
						  sslContextSpec.sslContext(SslContextBuilder.forClient()
											     .keyManager(keyManagerFactory)
											     .build());
					      } catch (SSLException e) {
						  System.out.println("erro em sslContextSpec");
						  e.printStackTrace();
					      }
					  });

	return WebClient.builder()
			.baseUrl("https://pix-h.api.efipay.com.br") // Base URL para o WebClient seguro
			.clientConnector(new ReactorClientHttpConnector(httpClient))
			.build();
    }
}
