package com.winterbe.react;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by svandenberg on 7/9/15.
 */
public class NodeContentRenderer {

    private Logger logger = LoggerFactory.getLogger("NodeContentRenderer");

    CloseableHttpClient httpclient = HttpClients.createDefault();


    public String renderToString(String s, Map<String, Object> model) throws IOException {
        String viewName = "./helloworld.js".equals(s) ? "L1CarsSearchPanel" : s;
            HttpGet httpget = new HttpGet("http://localhost:3000/"+viewName);
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream instream = entity.getContent();
                    try {
                        String ret = convertStreamToString(instream);
                        return ret;
                    } catch (Exception ex) {
                        logger.info("Error while creating String from node");
                    } finally {
                        instream.close();
                    }
                }
            } catch (Exception ex) {
               logger.info("Error while retrieving node content");
            } finally {
                response.close();
            }
        return "meh";
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
