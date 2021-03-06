package no.cantara.util;

import org.apache.commons.io.IOUtils;
import org.constretto.ConstrettoBuilder;
import org.constretto.ConstrettoConfiguration;
import org.constretto.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

public class Configuration {

    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    private static final ConstrettoConfiguration configuration = new ConstrettoBuilder()
            .createPropertiesStore()
			.addResource(Resource.create("classpath:application.properties"))
			.addResource(Resource.create("file:./config_override/application_override.properties"))
            .done()
            .getConfiguration();
	
	private Configuration() {}
	
	public static String getString(String key) {
		return configuration.evaluateToString(key);
	}
	
	public static Integer getInt(String key) {
		return configuration.evaluateToInt(key);
	}

	public static Integer getInt(String key, int defaultValue) {
		return configuration.evaluateTo(key, defaultValue);
	}

	public static boolean getBoolean(String key) {
		return configuration.evaluateToBoolean(key);
	}

    public static java.io.InputStream loadByName(String name) {
        try {
            java.io.File f = new java.io.File(name);
            if (f.isFile()) {
                log.info("Loading {} from Filesystem", name);
                return new FileInputStream(f);
            } else {

                log.info("Loading {} from Classpath", name);
                return Configuration.class.getClassLoader().getResourceAsStream(name);
            }
        } catch (Exception e) {
            log.error("Unable to access file:{}, exception: {} ", name, e);
        }
        return null;
    }

    public static String loadFromDiskByName(String name) {
        return convertStreamToString(loadByName(name));
    }


    public static String convertStreamToString(java.io.InputStream is) {
        String encoding = StandardCharsets.UTF_8.name();
        try {
            return IOUtils.toString(is, encoding);
        } catch (Exception e) {
            log.error("Unable to read inputstream ", e);
        }
        return "";
    }

}