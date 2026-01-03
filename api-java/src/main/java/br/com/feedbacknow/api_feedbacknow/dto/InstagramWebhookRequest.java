package br.com.feedbacknow.api_feedbacknow.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InstagramWebhookRequest {

    private String object;
    private List<Entry> entry;


    public static class Entry {
        private String id;
        private long time;
        private List<Change> changes;
    }

    public static class Change {
        private String field;
        private Value value;
    }

    public static class Value {
        private List<Message> messages;
    }

    public static class Message {
        private String id;
        private From from;
        private String text;
        private long timestamp;
    }

    public static class From {
        private String id;
        private String name;
        }
    }
