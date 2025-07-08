package dev.builder.core.infrastructure.properties;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

public class SimpleApplicationProperties implements ApplicationProperties {


    private final ActiveProfileProvider activeProfileProvider;

    private static Properties applicationProperties = new Properties();
    private static final Logger logger = Logger.getLogger(SimpleApplicationProperties.class.getSimpleName());


    public SimpleApplicationProperties(@NotNull ActiveProfileProvider activeProfileProvider){
        this.activeProfileProvider = activeProfileProvider;
        loadProperties();
    }

    private void loadProperties(){
        try {
            URL propertiesURL = loadDefaultPropertiesResource();
            if(propertiesURL != null) {
                applicationProperties.load(new FileInputStream(new File(propertiesURL.toURI())));
                activeProfileProvider.getActiveProfile();
            }
            Optional<String> activeProfile = activeProfileProvider.getActiveProfile();
            activeProfile.ifPresent(s -> overrideProperties(loadPropertiesResource(s)));
        } catch (URISyntaxException | IOException ignored){
        }
    }

    private void overrideProperties(@NotNull  URL propertiesResource){
        Properties newProperties = new Properties();
        applicationProperties = new Properties(applicationProperties);
    }

    private URL loadPropertiesResource(String profile){
        String filename = PropertiesNamespaces.MODULE_CONTEXT_PATH + PropertiesNamespaces.FILE_PREFIX + profile + PropertiesNamespaces.FILE_POSTFIX;
        return checkResource(filename, getClass().getResource(filename));
    }

    @Contract("_, null -> fail; _, !null -> param2")
    private @NotNull URL checkResource(String filename, URL resource){
        if(resource == null) throw new IllegalStateException(filename + " not found");
        return resource;
    }

    private URL loadDefaultPropertiesResource(){
        String filename = PropertiesNamespaces.MODULE_CONTEXT_PATH + PropertiesNamespaces.FILE_PREFIX + PropertiesNamespaces.FILE_POSTFIX;
        return getClass().getResource(filename);

    }


    @Override
    public String getDbUrl() {
        String prop = applicationProperties.getProperty(PropertiesNamespaces.DataSource.DB_URL);
        if(prop == null) throw new IllegalStateException(PropertiesNamespaces.DataSource.DB_URL + " not found");
        return prop;
    }
}
