package com.winterbe.react;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

/**
 * @author Benjamin Winterberg
 */
@Controller
public class MainController {

    private CommentService service;
    private ObjectMapper mapper;

    @Autowired
    public MainController(CommentService service) {
        this.service = service;
        this.mapper = new ObjectMapper();
    }

    @Bean
    public JsxViewResolver resolver() {
        return new JsxViewResolver();
    }

    @RequestMapping("/")
    public ModelAndView index(Map<String, Object> model) throws Exception {
        List<Comment> comments = service.getComments();
        String data = mapper.writeValueAsString(comments);

        ModelAndView mav = new ModelAndView("commentbox");
        mav.addObject("data", data);
        mav.addObject("comments", comments);
        mav.addObject("server", true);
        return mav;
    }
}
