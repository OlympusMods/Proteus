package com.github.olympus.proteus.client.ui.code;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class Language {
    private static final Pattern NUMBER = Pattern.compile("-?\\d+(\\.\\d+)?[a-zA-Z]?");
    private static final int INDENT = 2;

    public interface Highlighter {
        List<Span> highlight(String line);
    }

    public interface Validator {
        List<Problem> check(String source);
    }

    public record Entry(String name, String usage, String summary, String detail, List<String> examples,
            List<String> values, List<String> aliases) {
        public static Entry of(String name, String usage, String summary) {
            return new Entry(name.toLowerCase(Locale.ROOT), usage, summary, "", List.of(), List.of(), List.of());
        }

        public Entry detail(String text) {
            return new Entry(name, usage, summary, text, examples, values, aliases);
        }

        public Entry examples(String... lines) {
            return new Entry(name, usage, summary, detail, List.of(lines), values, aliases);
        }

        public Entry values(String... options) {
            return new Entry(name, usage, summary, detail, examples, List.of(options), aliases);
        }

        public Entry aliases(String... words) {
            List<String> lower = new ArrayList<>();
            for (String word : words) {
                lower.add(word.toLowerCase(Locale.ROOT));
            }
            return new Entry(name, usage, summary, detail, examples, values, List.copyOf(lower));
        }

        public String insert() {
            return usage.length() > name.length() ? name + " " : name;
        }

        boolean answersTo(String word) {
            return name.equals(word) || aliases.contains(word);
        }
    }

    public record Suggestion(String label, String insert, String hint, Entry entry) {
    }

    public record Completion(int prefixLength, List<Suggestion> items) {
        public static final Completion NONE = new Completion(0, List.of());

        public boolean isEmpty() {
            return items.isEmpty();
        }
    }

    private final String title;
    private final String comment;
    private final List<Entry> entries;
    private final Set<String> openers;
    private final Set<String> middles;
    private final String closer;
    private final Highlighter highlighter;
    private final Validator validator;

    private Language(Builder builder) {
        this.title = builder.title;
        this.comment = builder.comment;
        this.entries = List.copyOf(builder.entries);
        this.openers = Set.copyOf(builder.openers);
        this.middles = Set.copyOf(builder.middles);
        this.closer = builder.closer;
        this.highlighter = builder.highlighter;
        this.validator = builder.validator;
    }

    public static Builder builder(String title) {
        return new Builder(title);
    }

    public String title() {
        return title;
    }

    public List<Entry> entries() {
        return entries;
    }

    public Entry find(String word) {
        String lower = word.toLowerCase(Locale.ROOT);
        for (Entry entry : entries) {
            if (entry.answersTo(lower)) {
                return entry;
            }
        }
        return null;
    }

    public List<Span> highlight(String line) {
        if (highlighter != null) {
            return highlighter.highlight(line);
        }
        return defaultHighlight(line);
    }

    public List<Problem> check(String source) {
        return validator == null ? List.of() : validator.check(source);
    }

    public int indentAfter(String line) {
        int indent = line.length() - line.stripLeading().length();
        String word = firstWord(line);
        if (openers.contains(word) || middles.contains(word)) {
            indent += INDENT;
        }
        return indent;
    }

    public boolean closes(String line) {
        String word = line.strip().toLowerCase(Locale.ROOT);
        return closer != null && word.equals(closer) || middles.contains(word);
    }

    public static int indentWidth() {
        return INDENT;
    }

    public Completion complete(String text, int cursor, boolean force) {
        int lineStart = text.lastIndexOf('\n', cursor - 1) + 1;
        String before = text.substring(lineStart, cursor);
        String stripped = before.stripLeading();
        if (comment != null && stripped.startsWith(comment)) {
            return Completion.NONE;
        }
        int space = stripped.indexOf(' ');
        if (space < 0) {
            return completeWord(stripped, force);
        }
        return completeValue(stripped, space);
    }

    private Completion completeWord(String prefix, boolean force) {
        if (prefix.isEmpty() && !force) {
            return Completion.NONE;
        }
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<Suggestion> items = new ArrayList<>();
        for (Entry entry : entries) {
            if (entry.name().startsWith(lower) && !(entry.name().equals(lower) && !force)) {
                items.add(new Suggestion(entry.name(), entry.insert(), entry.summary(), entry));
            }
        }
        return items.isEmpty() ? Completion.NONE : new Completion(prefix.length(), items);
    }

    private Completion completeValue(String stripped, int space) {
        Entry entry = find(stripped.substring(0, space));
        if (entry == null || entry.values().isEmpty() || stripped.indexOf(' ', space + 1) >= 0) {
            return Completion.NONE;
        }
        String prefix = stripped.substring(space + 1);
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<Suggestion> items = new ArrayList<>();
        for (String value : entry.values()) {
            String compare = value.toLowerCase(Locale.ROOT);
            if (compare.startsWith(lower) && !compare.equals(lower)) {
                items.add(new Suggestion(value, value, entry.name(), entry));
            }
        }
        return items.isEmpty() ? Completion.NONE : new Completion(prefix.length(), items);
    }

    private static String firstWord(String line) {
        String stripped = line.strip();
        int space = stripped.indexOf(' ');
        return (space < 0 ? stripped : stripped.substring(0, space)).toLowerCase(Locale.ROOT);
    }

    private List<Span> defaultHighlight(String line) {
        List<Span> spans = new ArrayList<>();
        int length = line.length();
        int i = 0;
        while (i < length && line.charAt(i) == ' ') {
            i++;
        }
        if (i >= length) {
            return spans;
        }
        if (comment != null && line.startsWith(comment, i)) {
            spans.add(new Span(i, length, Kind.COMMENT));
            return spans;
        }
        int wordEnd = i;
        while (wordEnd < length && line.charAt(wordEnd) != ' ') {
            wordEnd++;
        }
        spans.add(new Span(i, wordEnd, find(line.substring(i, wordEnd)) != null ? Kind.KEYWORD : Kind.UNKNOWN));
        int at = wordEnd;
        while (at < length) {
            if (line.charAt(at) == ' ') {
                at++;
                continue;
            }
            int end = at;
            if (line.charAt(at) == '"') {
                end = line.indexOf('"', at + 1);
                end = end < 0 ? length : end + 1;
                spans.add(new Span(at, end, Kind.STRING));
            } else {
                while (end < length && line.charAt(end) != ' ') {
                    end++;
                }
                if (NUMBER.matcher(line.substring(at, end)).matches()) {
                    spans.add(new Span(at, end, Kind.NUMBER));
                }
            }
            at = end;
        }
        return spans;
    }

    public static final class Builder {
        private final String title;
        private final List<Entry> entries = new ArrayList<>();
        private String comment;
        private final Set<String> openers = new HashSet<>();
        private final Set<String> middles = new HashSet<>();
        private String closer;
        private Highlighter highlighter;
        private Validator validator;

        private Builder(String title) {
            this.title = title;
        }

        public Builder comment(String prefix) {
            this.comment = prefix;
            return this;
        }

        public Builder entry(Entry entry) {
            entries.add(entry);
            return this;
        }

        public Builder block(String close, String... opens) {
            this.closer = close.toLowerCase(Locale.ROOT);
            for (String open : opens) {
                openers.add(open.toLowerCase(Locale.ROOT));
            }
            return this;
        }

        public Builder middle(String... words) {
            for (String word : words) {
                middles.add(word.toLowerCase(Locale.ROOT));
            }
            return this;
        }

        public Builder highlighter(Highlighter value) {
            this.highlighter = value;
            return this;
        }

        public Builder validator(Validator value) {
            this.validator = value;
            return this;
        }

        public Language build() {
            return new Language(this);
        }
    }
}
