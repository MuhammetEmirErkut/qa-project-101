package com.hepsiburada.qa.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources({
    "system:properties",
    "system:env",
    "classpath:config/config.properties",
    "classpath:config.properties"
})
public interface ConfigReader extends Config {
    @Key("browser")
    @DefaultValue("chrome")
    String browser();

    @Key("baseUrl")
    @DefaultValue("https://www.hepsiburada.com")
    String baseUrl();

    @Key("explicitWait")
    @DefaultValue("15")
    int explicitWait();

    @Key("headless")
    @DefaultValue("false")
    boolean headless();

    @Key("remote")
    @DefaultValue("true")
    boolean remote();

    @Key("gridUrl")
    @DefaultValue("http://localhost:4444")
    String gridUrl();
}
