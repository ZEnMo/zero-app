package com.zenmo.vallum;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BackwardCompatibilityTest {
    @Test
    public void test() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String[] shadowJarPaths = {
                "../shadowJar/vallum-14c1cb1-all.jar",
                "../shadowJar/vallum-production-all.jar",
                "../shadowJar/vallum-current-all.jar",
        };

        var stopZtor = InitKt.initZtor(8082);

        for (var shadowJarPath: shadowJarPaths) {
            testVallumVersion(shadowJarPath);
        }

        stopZtor.stop();
    }

    public void testVallumVersion(String shadowJarPath) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        File jarFile = new File(shadowJarPath).getCanonicalFile();
        System.out.println(jarFile.getPath());
        if (!jarFile.exists()) {
            throw new RuntimeException("Jarfile doesnt exist");
        }
        URL jarUrl = jarFile.toURI().toURL();
        URL[] urls = {jarUrl};
        var loader = new URLClassLoader(urls, null);

        var vallumClass = loader.loadClass("com.zenmo.vallum.Vallum");
        var constructor = vallumClass.getDeclaredConstructor(String.class, String.class, String.class, String.class);
        var oldVallum = constructor.newInstance(System.getenv("CLIENT_ID"), System.getenv("CLIENT_SECRET"), "http://localhost:8082", "https://keycloak.zenmo.com/realms/testrealm/protocol/openid-connect/token");

        /*
         * TODO: write the code like normal java rather then reflection.
         * I don't know how to do this with this class loader stuff
         */
        var getSurveysByProject = vallumClass.getMethod("getSurveysByProject", String.class);
        var surveys = (List) getSurveysByProject.invoke(oldVallum, "Waardkwartier");
        assertEquals(1, surveys.size());
    }
}
