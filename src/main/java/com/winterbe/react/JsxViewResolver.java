package com.winterbe.react;

import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;

public class JsxViewResolver implements ViewResolver {

    @Setter
    String suffix = ".js";
    @Setter String indexFile = "/templates/index.html";

    private React renderer;
//    private NodeContentRenderer renderer;
    private MessageFormat format;

    @PostConstruct
    public void init() throws IOException {
        renderer = new React();
//        renderer = new NodeContentRenderer();
        ClassPathResource res = new ClassPathResource(indexFile);
        String template = IOUtils.toString(res.getInputStream());
        format = new MessageFormat(template);
    }

    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        return new JsxView(renderer, viewName + suffix, format);
    }
}
