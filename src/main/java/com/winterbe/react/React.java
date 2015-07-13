package com.winterbe.react;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

public class React {

    private Log logger = LogFactory.getLog("React");

    private ThreadLocal<NashornScriptEngine> engineHolder = new ThreadLocal<NashornScriptEngine>() {
        @Override
        protected NashornScriptEngine initialValue() {
//            NashornScriptEngine nashornScriptEngine = (NashornScriptEngine) new ScriptEngineManager().getEngineByName("nashorn");
            NashornScriptEngine nashornScriptEngine = (NashornScriptEngine) new NashornScriptEngineFactory().getScriptEngine(new String[] {"-ot=true","-pcc"});
            try {
                nashornScriptEngine.eval(read("static/nashorn-polyfill.js"));
                nashornScriptEngine.eval(read("static/vendor/react.js"));
                nashornScriptEngine.eval(read("static/vendor/mocit.js"));
                nashornScriptEngine.eval(read("static/vendor/underscore.js"));
//                nashornScriptEngine.eval(read("static/commentBox.js"));
                nashornScriptEngine.eval(read("static/helloworld.js"));
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
            return nashornScriptEngine;
        }
    };

    public  String renderCommentBox(List<Comment> comments) {
        try {
            Object html = engineHolder.get().invokeFunction("renderServer", comments);
            return String.valueOf(html);
        }
        catch (Exception e) {
            throw new IllegalStateException("failed to render react component", e);
        }
    }

    private Reader read(String path) {
        InputStream in = getClass().getClassLoader().getResourceAsStream(path);
        return new InputStreamReader(in);
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
            DateTime start = new DateTime();
            Object html = engineHolder.get().invokeFunction("renderServer");
            DateTime end = new DateTime();

            logger.info("Start: "+ start.toString()+", end: "+ end.toString());
            Duration dur = new Duration(start, end);
            logger.info("Render took: " + dur.toPeriod().getMillis()+ " miliseconds");
            return String.valueOf(html);
        }
        catch (Exception e) {
            throw new IllegalStateException("failed to render react component", e);
        }
    }
}