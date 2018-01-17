package no.cantara.service.model;

import no.cantara.util.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TestSpecificationLoader {
    private static final Logger log = LoggerFactory.getLogger(TestSpecificationLoader.class);


    private static final Map<String, String> configuredTestSpecifications = new LinkedHashMap<String, String>();


    private static final Map<String, String> global_command_replacement_map = new HashMap<>();

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

        /*
        GlobalVariable.1.vame="#TestMe"
GlobalVariable.2.value="{per, ola, petter}"
         */
        try {
            for (int x = 1; x < 20; x++) {
                if (StringUtils.isNotEmpty(Configuration.getString("GlobalVariable." + x + ".name"))) {
                    global_command_replacement_map.put( Configuration.getString("GlobalVariable." + x + ".name"),Configuration.getString("GlobalVariable." + x + ".value"));
                }
            }

        } catch (Exception e) {
            // log.error("Unable to find any predefined TestSpecifications");
        }
    }

    public static Map<String, String> getPersistedTestSpecificationFilenameMap() {
        return configuredTestSpecifications;
    }

    public static Map<String, String> getGlobal_command_replacement_map() {
        return global_command_replacement_map;
    }
}
