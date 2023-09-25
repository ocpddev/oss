# File Store Project

The OSS project is a versatile solution for interacting with multiple cloud storage providers such as Google Cloud
Storage and those providers that support the AWS S3 protocol. It simplifies and unifies file management across various
providers, making it easier to store, retrieve, and manage your files.

## Modules

This project includes the following modules:

- `oss-common`: The core module that provides the base interfaces and classes for the storage provider implementations.
- `oss-gcs`: The Google Cloud Storage provider implementation and its Spring Boot starter.
- `oss-s3`: The AWS S3 protocol implementation and its Spring Boot starter.

## Integration

To integrate the File Store Integration project with your Spring Boot application, add the appropriate Spring Boot
Starter to your `build.gradle.kts` file:

For AWS S3:

```kotlin
dependencies {
    implementation("dev.ocpd.oss:oss-s3:$version")
}
```

For Google Cloud Storage:

```kotlin
dependencies {
    implementation("dev.ocpd.oss:oss-gcs:$version")
}
```

Next, configure the properties for your chosen storage provider in your `application.properties` or `application.yml`
file.

For AWS S3:

```yaml
oss:
  provider: s3
  s3:
    # If the access-key and secret-key is not set, the default credential provider will be used
    access-key: <your-access-key>
    secret-key: <your-secret-key>
    region: <your-region>
    bucket: <your-bucket>
    # Optional, if you want to use endpoint that provided by third party implementation, such as Alibaba Cloud OSS, you can set endpoint here
    endpoint: http://localhost:4566
```

For Google Cloud Storage:

```yaml
oss:
  provider: gcs
  gcs:
    bucket: <your-bucket>

# Optional. There are many ways to provide credentials, you can choose one of them
# 1. Implicitly set the location of the credentials file as below
# 2. Set the location of the credentials file by environment variable GOOGLE_APPLICATION_CREDENTIALS 
# 3. In GKE environment, you can use Workload Identity to provide credentials, see https://cloud.google.com/kubernetes-engine/docs/how-to/workload-identity
spring:
  cloud:
    gcp:
      storage:
        enabled: true
      credentials:
        location: <your-credentials-location>
```

### Usage

Once integrated, you can use the `FileStore` bean to interact with your chosen storage provider. This service provides
methods to store, retrieve, and manage files across different providers in a consistent manner.

### Cautions

- The 'key' parameter CAN NOT start with '/', otherwise an invalid object exception will be thrown
- The 'key' parameter can be a file(abc.png) or a path(your/folder/abc.png).
