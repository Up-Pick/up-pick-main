package org.oneog.uppick.auction;

import org.opensearch.spring.boot.autoconfigure.OpenSearchRestHighLevelClientAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;

@SpringBootApplication(exclude = {
	ElasticsearchRestClientAutoConfiguration.class,
	ElasticsearchDataAutoConfiguration.class,
	OpenSearchRestHighLevelClientAutoConfiguration.class
})
public class UppickProductApplication {

	public static void main(String[] args) {

		SpringApplication.run(UppickProductApplication.class, args);
	}

}
