package com.berke.urlshortener.util;

import org.springframework.stereotype.Component;
import ua_parser.Client;
import ua_parser.Parser;

@Component
public class UserAgentUtil {

    private final Parser parser;

    public UserAgentUtil() {
        this.parser = new Parser();
    }

    public String getBrowser(String userAgent) {
        if (userAgent == null) return "Unknown";
        Client client = parser.parse(userAgent);
        
        String family = client.userAgent.family;
        String major = client.userAgent.major;
        
        return family + (major != null ? " " + major : "");
    }

    public String getOs(String userAgent) {
        if (userAgent == null) return "Unknown";
        Client client = parser.parse(userAgent);
        
        String family = client.os.family;
        String major = client.os.major;
        
        return family + (major != null ? " " + major : "");
    }

    public String getDevice(String userAgent) {
        if (userAgent == null) return "Unknown";
        Client client = parser.parse(userAgent);
        return client.device.family;
    }
}