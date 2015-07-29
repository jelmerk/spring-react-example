package com.winterbe.react;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static java.nio.charset.StandardCharsets.UTF_8;

public class React2 {


    public static void main(String[] args) throws Exception {

        V8 v8 = V8.createV8Runtime("global");
        v8.executeVoidScript(read("static/vendor/react.js"));
        v8.executeVoidScript(read("static/vendor/showdown.min.js"));
        v8.executeVoidScript(read("static/commentBox.js"));

        V8Object comment = new V8Object(v8)
                .add("author", "Jelmer")
                .add("text", "Just a test");

        V8Array comments = new V8Array(v8).push(comment);

        V8Array parameters = new V8Array(v8).push(comments);

        String html = v8.executeStringFunction("renderServer", parameters);

        System.out.println(html);

        comment.release();
        comments.release();
        parameters.release();

        v8.release(true);

    }

    private static String read(String path) throws IOException {
        InputStream in = React2.class.getClassLoader().getResourceAsStream(path);
        return FileCopyUtils.copyToString(new InputStreamReader(in, UTF_8));
    }

}
