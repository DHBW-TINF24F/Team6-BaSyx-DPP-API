package com.team6.dpp.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.AbstractHttp11Protocol;

@Component
public class TomcatConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addConnectorCustomizers(connector -> {
            ProtocolHandler protocol = connector.getProtocolHandler();
            if (protocol instanceof AbstractHttp11Protocol<?>) {
                AbstractHttp11Protocol<?> p = (AbstractHttp11Protocol<?>) protocol;
                p.setRelaxedPathChars("<>[\\]^`{|}\":;/?@&=+$,");
                p.setRelaxedQueryChars("<>[\\]^`{|}\":;/?@&=+$,");
            }
        });
    }
}
