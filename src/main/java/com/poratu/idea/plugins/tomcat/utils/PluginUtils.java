package com.poratu.idea.plugins.tomcat.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfo;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Stream;

/**
 * Author : zengkid
 * Date   : 2017-03-06
 * Time   : 21:35
 */
public abstract class PluginUtils {

    private static final Logger logger = Logger.getInstance(PluginUtils.class);

    private static Sdk getDefaultJDK() {
        Sdk[] allJdks = ProjectJdkTable.getInstance().getAllJdks();
        if (allJdks.length == 0) {
            throw new RuntimeException("Please setup your project JDK first");
        }
        return allJdks[0];
    }

    public static TomcatInfo getTomcatInfo(String tomcatHome) {
        return getTomcatInfo(getDefaultJDK().getHomePath(), tomcatHome);
    }

    private static TomcatInfo getTomcatInfo(String javaHome, String tomcatHome) {
        String[] cmd = new String[]{
                javaHome + "/bin/java",
                "-cp",
                "lib/catalina.jar",
                "org.apache.catalina.util.ServerInfo"
        };
        BufferedReader reader = null;
        final TomcatInfo tomcatInfo = new TomcatInfo();
        tomcatInfo.setPath(tomcatHome);
        try {
            Process process = Runtime.getRuntime().exec(cmd, null, new File(tomcatHome));
            process.waitFor();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            Stream<String> lines = reader.lines();
            lines.forEach(s -> {
                if (s.startsWith("Server version")) {
                    String name = StringUtils.replace(getValue(s), "/", " ");
                    tomcatInfo.setName(name);
                } else if (s.startsWith("Server number")) {
                    String version = getValue(s);
                    tomcatInfo.setVersion(version);
                }

            });

            reader.close();

        } catch (Exception e) {
            logger.error("Error getting Tomcat information", e);
            throw new RuntimeException(e.getMessage(), e);

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error("Error closing buffered reader", e);
                }
            }
        }
        return tomcatInfo;
    }

    private static String getValue(String s) {
        String[] strings = StringUtils.split(s, ":");
        String result = "";
        if (strings != null && strings.length == 2) {
            result = strings[1].trim();
        }
        return result;
    }
}
