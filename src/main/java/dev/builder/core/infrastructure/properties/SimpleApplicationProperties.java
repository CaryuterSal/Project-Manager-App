package dev.builder.core.infrastructure.properties;

import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.jetbrains.annotations.NotNull;

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

public class SimpleApplicationProperties implements DataSourceProperties, AdminProperties {


    private final ActiveProfileProvider  activeProfileProvider;

    private static Properties applicationProperties = new Properties();
    private static final Logger logger = Logger.getLogger(SimpleApplicationProperties.class.getSimpleName());


    //TODO: real fix without method signature
    public SimpleApplicationProperties(ActiveProfileProvider activeProfileProvider) throws URISyntaxException, IOException {
        this.activeProfileProvider = activeProfileProvider;
        try {
            URL propertiesURL = loadDefaultPropertiesResource();
            if (propertiesURL != null) {
                applicationProperties.load(new FileInputStream(new File(propertiesURL.toURI())));
            }
        } finally {

        }
        activeProfileProvider.getActiveProfile();
    }

    //TODO: real fix without method signature
    private void setDefaultProperties(@NotNull URL propertiesResource) throws URISyntaxException, IOException {
        applicationProperties.load(new FileInputStream(new File(propertiesResource.toURI())));
    }

    private void overrideProperties(@NotNull  URL propertiesResource){
        Properties newProperties = new Properties();
        applicationProperties = new Properties(applicationProperties);
    }

    //TODO: return real default properties
    private URL loadDefaultPropertiesResource(){
        URL resource = getClass().getResource(PropertiesNamespaces.MODULE_CONTEXT_PATH + PropertiesNamespaces.FILE_PREFIX + PropertiesNamespaces.FILE_POSTFIX);
        return resource;
    }

    private URL loadPropertiesResource(String profile){
        return getClass().getResource(PropertiesNamespaces.MODULE_CONTEXT_PATH + PropertiesNamespaces.FILE_PREFIX + profile + PropertiesNamespaces.FILE_POSTFIX);
    }

    @Override
    public String getAdminUsername() {
        return applicationProperties.getProperty(PropertiesNamespaces.Admin.ADMIN_USERNAME);
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
