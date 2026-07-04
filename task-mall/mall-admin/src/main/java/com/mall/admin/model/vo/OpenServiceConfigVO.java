package com.mall.admin.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class OpenServiceConfigVO implements Serializable {
    private static final long serialVersionUID = 7262127564572807385L;

    private String title;
    private String message;
    private List<Bot> bots = new ArrayList<>();

    @Data
    public static class Bot implements Serializable {
        private static final long serialVersionUID = 5503024463413308515L;

        private String botName;
        private String description;
    }
}