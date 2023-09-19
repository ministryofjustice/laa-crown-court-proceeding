package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.integration;

import cloud.localstack.docker.annotation.IHostNameResolver;

public class HostNameResolverImpl implements IHostNameResolver {
    @Override
    public String getHostName() {
        String dockerHost = System.getenv("DOCKER_HOST");
        return System.getenv("TEMP_HOST");
    }
}
