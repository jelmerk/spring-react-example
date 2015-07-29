package com.winterbe.react;


import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.utils.V8ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class React {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ThreadLocal<V8> engineHolder = new ThreadLocal<V8>() {
        @Override
        protected V8 initialValue() {
            try {
                V8 v8 = V8.createV8Runtime("global");
                v8.executeVoidScript(read("static/vendor/react.js"));
                v8.executeVoidScript(read("static/vendor/showdown.min.js"));
                v8.executeVoidScript(read("static/commentBox.js"));
                return v8;
            } catch(IOException e) {
                throw new RuntimeException("Whatever", e);
            }
        }
    };

    public  String renderCommentBox(List<Comment> comments) {
        try {
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

        } catch (Exception e) {
            throw new IllegalStateException("failed to render react component", e);
        }
    }

    private String read(String path) throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream(path);
        return FileCopyUtils.copyToString(new InputStreamReader(in, UTF_8));
    }
}
