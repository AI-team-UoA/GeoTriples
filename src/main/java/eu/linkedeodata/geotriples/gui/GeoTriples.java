/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.linkedeodata.geotriples.gui;

import java.net.URL;
import java.util.Locale;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;

/**
 * Stock Tracker application.
 */
public class GeoTriples extends Application.Adapter {

    private GeoTriplesWindow window = null;

    public static final String LANGUAGE_KEY = "language";

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
    	ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    	//classLoader.loadClass(GeoTriplesWindow.class.getName());
    	//System.out.println(classLoader);
       // URL resource = classLoader.getResource("/");  
       //System.out.println(resource);
    	//Resources fff=new 
        String language = properties.get(LANGUAGE_KEY);
        System.out.println(language); //na to dw kati paei x edw
        //Locale locale = (language == null) ? Locale.ENGLISH : new Locale(language);
        Locale locale = (language == null) ? Locale.getDefault() : new Locale(language);
        System.out.println(locale);
        //Resources resources = new Resources(GeoTriplesWindow.class.getName(),locale);
        @SuppressWarnings("unused")
		URL url=classLoader.getResource("GeoTriplesWindow.json");
        
        Resources resources = new Resources("GeoTriplesWindow",locale);
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        System.out.println(classLoader.getResource("GeoTriplesWindow.bxml"));
        window = (GeoTriplesWindow)bxmlSerializer.readObject(classLoader.getResource("GeoTriplesWindow.bxml"),
            resources);
        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(GeoTriples.class, args);
    }

}
