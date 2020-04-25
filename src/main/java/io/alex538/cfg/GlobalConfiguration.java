package io.alex538.cfg;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class GlobalConfiguration {

    int port;

    Credentials credentials;

    Services services;

    Storage storage;

}
