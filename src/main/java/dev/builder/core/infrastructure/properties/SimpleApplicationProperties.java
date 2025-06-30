package dev.builder.core.infrastructure.properties;

import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.xml.crypto.URIReferenceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.spi.LocaleNameProvider;

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
            applicationProperties.load(new FileInputStream(new File(propertiesURL.toURI())));
            activeProfileProvider.getActiveProfile();
        } catch (URISyntaxException | IOException ignored){
        }
    }

    //TODO: real fix without method signature
    private void setDefaultProperties(@NotNull URL propertiesResource) throws URISyntaxException, IOException {
        applicationProperties.load(new FileInputStream(new File(propertiesResource.toURI())));
    }

    private void overrideProperties(@NotNull  URL propertiesResource){
        Properties newProperties = new Properties();
        applicationProperties = new Properties(applicationProperties);
    }

    private URL loadPropertiesResource(String profile){
        String filename = PropertiesNamespaces.MODULE_CONTEXT_PATH + PropertiesNamespaces.FILE_PREFIX + profile + PropertiesNamespaces.FILE_POSTFIX
        return checkResource(filename, getClass().getResource(filename));
    }

    @Contract("_, null -> fail; _, !null -> param2")
    private @NotNull URL checkResource(String filename, URL resource){
        if(resource == null) throw new IllegalStateException(filename + " not found");
        return resource;
    }

    private URL loadDefaultPropertiesResource(){
        String filename = PropertiesNamespaces.MODULE_CONTEXT_PATH + PropertiesNamespaces.FILE_PREFIX + PropertiesNamespaces.FILE_POSTFIX
        return checkResource(filename, getClass().getResource(filename));

    }

    @Override
    public String getAdminPassword() {
        return "";
    }

    @Override
    public String geDbtUrl() {
        return "";
    }

    @Override
    public String getDbUsername() {
        return "";
    }

    @Override
    public String getDbPassword() {
        return "";
    }
}
