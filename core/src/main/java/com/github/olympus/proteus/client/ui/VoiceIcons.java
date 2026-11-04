package com.github.olympus.proteus.client.ui;

public final class VoiceIcons {
    public static final String MIC = "proteus:mic";
    public static final String MIC_OFF = "proteus:mic_off";
    public static final String SPEAKER = "proteus:speaker";
    public static final String SPEAKER_OFF = "proteus:speaker_off";
    public static final String SLIDERS = "proteus:sliders";
    public static final String RECORD = "proteus:record";
    public static final String EYE = "proteus:eye";
    public static final String EYE_OFF = "proteus:eye_off";

    private static final String[] MIC_ROWS = {
        "..####..",
        ".######.",
        ".######.",
        ".######.",
        ".######.",
        "#.####.#",
        "#......#",
        ".#....#.",
        "..####..",
        "...##...",
        "...##...",
        ".######."
    };
    private static final String[] SPEAKER_ROWS = {
        "....#.......",
        "...##...#...",
        ".####.#..#..",
        ".####..#.#..",
        ".####..#.#..",
        ".####.#..#..",
        "...##...#...",
        "....#......."
    };
    private static final String[] SLIDERS_ROWS = {
        "...##.......",
        "...##.......",
        "############",
        "...##.......",
        "...##.......",
        "............",
        "............",
        ".......##...",
        ".......##...",
        "############",
        ".......##...",
        ".......##..."
    };
    private static final String[] SPEAKER_OFF_ROWS = {
        "....#.......",
        "...##.......",
        ".####.#...#.",
        ".####..#.#..",
        ".####...#...",
        ".####..#.#..",
        "...##.#...#.",
        "....#......."
    };
    private static final String[] RECORD_ROWS = {
        "..####..",
        ".######.",
        "########",
        "########",
        "########",
        "########",
        ".######.",
        "..####.."
    };
    private static final String[] EYE_ROWS = {
        "...####...",
        ".##....##.",
        "#..####..#",
        "#.######.#",
        "#.######.#",
        "#..####..#",
        ".##....##.",
        "...####..."
    };

    private static boolean done;

    private VoiceIcons() {
    }

    public static synchronized void register() {
        if (done) {
            return;
        }
        done = true;
        plain(MIC, 8, 12, MIC_ROWS);
        slashed(MIC_OFF, 8, 12, MIC_ROWS);
        plain(SPEAKER, 12, 8, SPEAKER_ROWS);
        plain(SPEAKER_OFF, 12, 8, SPEAKER_OFF_ROWS);
        plain(SLIDERS, 12, 12, SLIDERS_ROWS);
        plain(RECORD, 8, 8, RECORD_ROWS);
        plain(EYE, 10, 8, EYE_ROWS);
        slashed(EYE_OFF, 10, 8, EYE_ROWS);
    }

    private static void plain(String name, int width, int height, String[] rows) {
        Icons.register(name, width, height, (c, cx, cy, color) -> Pixels.draw(c, cx, cy, color, rows));
    }

    private static void slashed(String name, int width, int height, String[] rows) {
        Icons.register(name, width, height, (c, cx, cy, color) -> {
            Pixels.draw(c, cx, cy, color, rows);
            Pixels.slash(c, cx, cy, Math.max(width, height), color);
        });
    }
}
