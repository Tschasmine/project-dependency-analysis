package com.example.app;

import com.example.list.LinkedList;

import static com.example.utilities.StringUtils.join;
import static com.example.utilities.StringUtils.split;
import static com.example.app.MessageUtils.getMessage;

import org.apache.commons.text.WordUtils;
import java.util.*;

public class App {
    public static void main(String[] args) {
        LinkedList tokens;
        tokens = split(getMessage());
        String result = join(tokens);
        System.out.println(WordUtils.capitalize(result));
    }
}
