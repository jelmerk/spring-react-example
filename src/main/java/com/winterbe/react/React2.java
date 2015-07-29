package com.winterbe.react;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.utils.V8ObjectUtils;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class React2 {


    public static void main(String[] args) throws Exception {

        V8 v8 = V8.createV8Runtime("global");
        v8.executeVoidScript(read("static/vendor/react.js"));
        v8.executeVoidScript(read("static/vendor/showdown.min.js"));
        v8.executeVoidScript(read("static/commentBox.js"));

        V8Object comment1 = new V8Object(v8)
                .add("author", "Jelmer")
                .add("text", "Just a test");


        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("author", "Maarten");
        commentMap.put("text", "react rules");


        V8Object comment2 = V8ObjectUtils.toV8Object(v8, commentMap);

        V8Array comments = new V8Array(v8).push(comment1).push(comment2);

        V8Array parameters = new V8Array(v8).push(comments);

        String html = v8.executeStringFunction("renderServer", parameters);

        System.out.println(html);

        comment1.release();
        comment2.release();

        comments.release();
        parameters.release();

        v8.release(true);

    }

    private static String read(String path) throws IOException {
        InputStream in = React2.class.getClassLoader().getResourceAsStream(path);
        return FileCopyUtils.copyToString(new InputStreamReader(in, UTF_8));
    }

}
