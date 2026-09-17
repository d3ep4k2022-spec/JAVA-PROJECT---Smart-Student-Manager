package com.studentmanager;

/**
 * Entry point of the Smart Student Manager.
 *
 * <p>Kept deliberately tiny. Its only job is to translate command-line arguments into
 * application configuration and hand control to {@link StudentManagerApp}, so that the
 * application object itself can be constructed and driven by a test without going
 * through {@code main}.</p>
 *
 * <p>Usage:</p>
 * <pre>
 *   java -cp out com.studentmanager.Main            # normal run
 *   java -cp out com.studentmanager.Main --seed     # wipe data/ and load demo records
 *   java -cp out com.studentmanager.Main --help     # print options and exit
 * </pre>
 */
public final class Main {

    public static void main(String[] args) {
        boolean seed = false;

        for (String arg : args) {
            switch (arg) {
                case "--seed" -> seed = true;
                case "--help", "-h" -> {
                    printUsage();
                    return;
                }
                default -> {
                    System.out.println("Unknown option: " + arg);
                    printUsage();
                    return;
                }
            }
        }

        StudentManagerApp app = new StudentManagerApp();
        if (seed) {
            app.seedDemoData();
        }
        app.run();
    }

    private static void printUsage() {
        System.out.println("""
                Smart Student Manager

                Usage: java -cp out com.studentmanager.Main [options]

                Options:
                  --seed      Replace data/*.csv with the bundled demo records, then start.
                  --help,-h   Show this message and exit.
                """);
    }

    private Main() {
    }
}
