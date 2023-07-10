package dev.ocpd.oss.config;

import cloud.localstack.Localstack;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

public class AwsPropertiesEnvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final String TEST_BUCKET_NAME = "test-bucket";
    public static final String TEST_ACCESS_KEY = "test";
    public static final String TEST_SECRET_KEY = "test";

    private static final Map<String, String> ENVS = Map.of(
        "oss.provider", "s3",
        "oss.s3.access-key", TEST_ACCESS_KEY,
        "oss.s3.secret-key", TEST_SECRET_KEY,
        "oss.s3.bucket", TEST_BUCKET_NAME,
        "oss.s3.region", Localstack.getDefaultRegion(),
        "oss.s3.endpoint", Localstack.INSTANCE.getEndpointS3()
    );

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(ENVS).applyTo(applicationContext.getEnvironment());
    }
}
