public class Debug {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String BLACK_BOLD = "\033[1;30m";


    public static void printWithLines(boolean startWithNewLine, String str) {
        if (startWithNewLine)
            System.out.println();
        System.out.println("--------------------" + str + "--------------------");
    }

    public static void printBold(String str) {
        System.out.print(BLACK_BOLD + str + ANSI_RESET);
    }

    public static void printRed(String str) {
        System.out.print(ANSI_RED + str + ANSI_RESET);
    }

    public static void printPurple(String str) {
        System.out.print(ANSI_PURPLE + str + ANSI_RESET);
    }

}