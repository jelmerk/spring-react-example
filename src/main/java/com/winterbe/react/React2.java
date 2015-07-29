package com.winterbe.react;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.utils.V8ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class React2 {


    public static void main(String[] args) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        V8 v8 = V8.createV8Runtime("global");
        v8.executeVoidScript(read("static/vendor/react.js"));
        v8.executeVoidScript(read("static/vendor/showdown.min.js"));
        v8.executeVoidScript(read("static/commentBox.js"));


        Comment c = new Comment("Sander", "I like meetings");

        Map<String,Object> comment1 = objectMapper.convertValue(c, Map.class);

        Map<String, Object> comment2 = new HashMap<>();
        comment2.put("author", "Maarten");
        comment2.put("text", "React rules");

        List<Map<String, Object>> comments = Arrays.asList(comment1, comment2);

        V8Array commentsArray = V8ObjectUtils.toV8Array(v8, comments);

        V8Array parameters = new V8Array(v8).push(commentsArray);

        String html = v8.executeStringFunction("renderServer", parameters);

        System.out.println(html);

        commentsArray.release();
        parameters.release();

        v8.release(true);

    }

    private static String read(String path) throws IOException {
        InputStream in = React2.class.getClassLoader().getResourceAsStream(path);
        return FileCopyUtils.copyToString(new InputStreamReader(in, UTF_8));
    }

}
