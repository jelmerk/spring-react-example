package com.winterbe.react;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Benjamin Winterberg
 */
@AllArgsConstructor
public class Comment {
    @Getter
    @Setter
    private String author;
    @Getter
    @Setter
    private String text;
}

