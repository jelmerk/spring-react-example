package com.winterbe.react;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.utils.V8ObjectUtils;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.*;

import static java.nio.charset.StandardCharsets.UTF_8;

// http://eclipsesource.com/blogs/2015/05/12/multithreaded-javascript-with-j2v8/

public class ReactRendering {

    private static final int NO_ENGINES = 16;

    private static ExecutorService executorService = Executors.newFixedThreadPool(NO_ENGINES);

    public static String render(Map<String, Object> model) {
        Future<String> future = executorService.submit(new ScriptExecutor(model));

        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new ReactRenderException(e);
        }
    }

    public static Future<String> renderAsync(Map<String, Object> model) {
        return executorService.submit(new ScriptExecutor(model));
    }

    private static final class ScriptExecutor implements Callable<String> {

        private static ThreadLocal<V8> engineHolder = new ThreadLocal<V8>() {
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
                    throw new IllegalStateException("Failed to load react", e);
                }
            }
        };

        private final Map<String, Object> model;

        public ScriptExecutor(Map<String, Object> model) {
            this.model = model;
        }

        @Override
        public String call() throws Exception {
            V8 v8 = engineHolder.get();

            V8Object v8Model = null;
            V8Array v8Parameters = null;

            try {
                v8Model = V8ObjectUtils.toV8Object(v8, model);
                v8Parameters = new V8Array(v8).push(v8Model);
                return v8.executeStringFunction("renderServer", v8Parameters);
            } finally {
                if (v8Model != null) {
                    v8Model.release();
                }
                if (v8Parameters != null) {
                    v8Parameters.release();
                }
            }
        }

        private static String read(String path) throws IOException {
            InputStream in = ScriptExecutor.class.getClassLoader().getResourceAsStream(path);
            return FileCopyUtils.copyToString(new InputStreamReader(in, UTF_8));
        }
    }
}
