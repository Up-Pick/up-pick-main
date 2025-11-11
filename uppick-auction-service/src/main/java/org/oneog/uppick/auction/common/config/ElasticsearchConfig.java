package org.oneog.uppick.auction.common.config;

import java.util.Objects;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;

@Configuration
@EnableElasticsearchRepositories
public class ElasticsearchConfig {

    @Value("${opensearch.uris}")
    private String opensearchUri;

    @Value("${opensearch.region:us-east-1}")
    private String opensearchRegion;

    @Bean
    public OpenSearchClient openSearchClient() {

        SdkHttpClient httpClient = ApacheHttpClient.builder().build();

        // AwsSdk2Transport는 호스트명만 필요하므로 https:// 제거
        String host = opensearchUri.replaceFirst("^https?://", "");

        return new OpenSearchClient(
            new AwsSdk2Transport(
                httpClient,
                Objects.requireNonNull(host),
                Objects.requireNonNull(Region.of(opensearchRegion)),
                AwsSdk2TransportOptions.builder().build()
            )
        );
    }

}
