package com.winterbe.react;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.utils.V8ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.util.FileCopyUtils;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class React {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private Log logger = LogFactory.getLog("React");

    private ThreadLocal<V8> engineHolder = new ThreadLocal<V8>() {
        @Override
        protected V8 initialValue() {
            try {
                V8 v8 = V8.createV8Runtime("global");
                v8.executeVoidScript(read("static/vendor/react.js"));
                v8.executeVoidScript(read("static/vendor/mocit.js"));
                v8.executeVoidScript(read("static/vendor/underscore.js"));
//                v8.executeVoidScript(read("static/commentBox.js"));
                v8.executeVoidScript(read("static/helloworld.js"));
                return v8;
            } catch(IOException e) {
                throw new RuntimeException("Whatever", e);
            }
        }
    };

    public  String renderCommentBox(List<Comment> comments) {
        V8 v8 = engineHolder.get();

        V8Array commentsArray = null;
        V8Array parameters = null;
        try {
            List<Map> commentsMap = comments.stream()
                    .map(comment -> objectMapper.convertValue(comment, Map.class))
                    .collect(Collectors.toList());

            commentsArray = V8ObjectUtils.toV8Array(v8, commentsMap);
            parameters = new V8Array(v8).push(commentsArray);
            return v8.executeStringFunction("renderServer", parameters);

        } finally {
            if (commentsArray != null) {
                commentsArray.release();
            }
            if (parameters != null) {
                parameters.release();
            }
        }
    }

    public String renderToString(String s, Map<String, Object> model) {
        if (!s.equals("./commentbox.js")) {
//            throw new RuntimeException("View not found");
            return renderHelloWorld();
        } else {
            List<Comment> comments = (List<Comment>) model.get("comments");
            return renderCommentBox(comments);
        }
    }

    private String renderHelloWorld() {
        try {
            V8 v8 = engineHolder.get();

            V8Array parameters = null;


            DateTime start = new DateTime();

            String html;
            try {
                parameters = new V8Array(v8);
                html = v8.executeStringFunction("renderServer", parameters);
            } finally {
                if (parameters != null) {
                    parameters.release();
                }
            }
            DateTime end = new DateTime();

            logger.info("Start: "+ start.toString()+", end: "+ end.toString());
            Duration dur = new Duration(start, end);
            logger.info("Render took: " + dur.toPeriod().getMillis()+ " miliseconds");

            return html;
        }
        catch (Exception e) {
            throw new IllegalStateException("failed to render react component", e);
        }
    }


    private String read(String path) throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream(path);
        return FileCopyUtils.copyToString(new InputStreamReader(in, UTF_8));
    }
}