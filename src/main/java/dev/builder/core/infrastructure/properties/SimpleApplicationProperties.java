package dev.builder.core.infrastructure.properties;

import dev.builder.core.infrastructure.di.annotation.Bean;
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

@Bean
public class SimpleApplicationProperties implements ApplicationProperties {

    private final ActiveProfileProvider activeProfileProvider;

    private static final Properties applicationProperties = new Properties();
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
            }
            Optional<String> activeProfile = activeProfileProvider.getActiveProfile();
            activeProfile.ifPresent(s -> overrideProperties(loadPropertiesResource(s)));
        } catch (URISyntaxException | IOException ex){
            logger.severe(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private void overrideProperties(@NotNull  URL propertiesResource) {
        try {
            Properties newProperties = new Properties();
            newProperties.load(new FileInputStream(new File(propertiesResource.toURI())));
            for (String key : newProperties.stringPropertyNames()) {
                applicationProperties.setProperty(key, newProperties.getProperty(key));
            }
        } catch (IOException | URISyntaxException ex) {
            logger.severe(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private URL loadPropertiesResource(String profile){
        String filename = PropertiesNamespaces.MODULE_CONTEXT_PATH + PropertiesNamespaces.FILE_PREFIX + '-' + profile + PropertiesNamespaces.FILE_POSTFIX;
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

    private String getProperty(String key){
        String prop = applicationProperties.getProperty(key);
        if(prop == null) throw new IllegalStateException(key + " not found");
        return prop;
    }

    @Override
    public String getDbUrl() {
        return System.getenv("DB_URL");
    }

    @Override
    public String getUser() {
        return System.getenv("DB_USER");
    }

    @Override
    public String getPassword() {
        return System.getenv("DB_PASSWORD");
    }

    @Override
    public String getDbName() {
        return System.getenv("DB_NAME");
    }

    @Override
    public String getCipherTransformation() {
        return  applicationProperties.getProperty(PropertiesNamespaces.Session.TRANSFORMATION);
    }

    @Override
    public String getCipherKeyType() {
        return applicationProperties.getProperty(PropertiesNamespaces.Session.KEY_TYPE);
    }

    @Override
    public String getFilename() {
        return applicationProperties.getProperty(PropertiesNamespaces.Session.FILENAME);
    }
}
