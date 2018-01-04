package no.cantara.service.model;

import no.cantara.service.util.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestSpecificationLoader {
    private static final Logger log = LoggerFactory.getLogger(TestSpecificationLoader.class);


    private static Map<String, String> configuredTestSpecifications = new LinkedHashMap<String, String>();

    static {
        try {
            for (int x = 1; x < 20; x++) {
                if (StringUtils.isNotEmpty(Configuration.getString("TestSpecification." + x + ".read.filename"))) {
                    configuredTestSpecifications.put(x + ".readfilename", Configuration.getString("TestSpecification." + x + ".read.filename"));
                    configuredTestSpecifications.put(x + ".writefilename", Configuration.getString("TestSpecification." + x + ".write.filename"));
                }
            }

        } catch (Exception e) {
            // log.error("Unable to find any predefined TestSpecifications");
        }
    }

    public static Map<String, String> getPersistedTestSpacificationFilenameMap() {
        return configuredTestSpecifications;
    }
}
