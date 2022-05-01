import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.*;

public class FileLoaderApp extends FileLoaderAbstractApp {
    private static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_RESET = "\u001B[0m";
    private final String FILE_DIR = "C:\\Users\\muzik\\Desktop";
    private ArrayList<Thread> threads;
    private ArrayList<FileLoader> filesToLoad;
    private Scanner sc;
    private String command;
    private String fileName;
    private static int countOfFileLoaders;

    public static void main(String[] args) {
        FileLoaderApp app = new FileLoaderApp();
    }

    @Override
    public void initialize() {
        threads = new ArrayList<Thread>();
        filesToLoad = new ArrayList<FileLoader>();
        sc = new Scanner(System.in);
    }

    @Override
    public void start() {
        System.out.println("Welcome to the File Loader!" + " Enter url to start downloading");
        command = sc.nextLine().trim().toLowerCase(Locale.ROOT);
        if (isValidURL(command)) {
            try {
                startDownload();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("\nHere's list of commands: " + "\nTo start downloading, print url of file" +
                "\nTo get info about all files, print" + ANSI_PURPLE + " INFO" + ANSI_RESET +
                "\nTo pause/resume download, print " + ANSI_PURPLE + "PAUSE/RESUME" + ANSI_RESET +
                "\nTo exit, print " + ANSI_PURPLE + "EXIT" + ANSI_RESET);
        while (!command.equals("exit")) {

            System.out.println("Print command or url: ");
            command = sc.nextLine().trim().toLowerCase(Locale.ROOT);
            if (command.equals("info")) {
                if (countOfFileLoaders == 0) {
                    System.out.println("No downloads to show info of");
                } else {
                    info();
                }
            } else if (command.equals("pause")) {
                pause();
            } else if (command.equals("resume")) {
                resume();
            } else if (isValidURL(command)) {
                try {
                    startDownload();
                } catch (IOException exc) {
                    System.out.println("Wrong input, try again.");
                }
            }
        }
        System.out.println("Closing the program...");
    }

    private void startDownload() throws IOException {
        URL url = new URL(command);
        setFileName(command);
        filesToLoad.add(new FileLoader(FILE_DIR, url, fileName));
        threads.add(new Thread(filesToLoad.get(countOfFileLoaders)));
        threads.get(countOfFileLoaders).start();
        countOfFileLoaders++;
    }

    private void info() {
        System.out.println("Print ID of the download to get info of it or print ALL for info about all downloads");
        infoSpecialized();
        command = sc.nextLine().trim().toLowerCase(Locale.ROOT);
        if (command.equals("all")) {
            int count = 0;
            for (FileLoader fl : filesToLoad) {
                System.out.println(fl.getInfo().append(String.format("%-15s", " ID:" + count)));
                count++;
            }
        } else {
            int id = Integer.parseInt(command);
            if (id > countOfFileLoaders) {
                throw new IllegalArgumentException("Wrong ID, try again");
            } else {
                System.out.println(filesToLoad.get(id).getInfo());
            }
        }
    }

    private void infoSpecialized() {
        int count = 0;
        for (FileLoader fl : filesToLoad) {
            System.out.println("ID: " + count + ", File name: " + fl.getFileName());
            System.out.println();
            count++;
        }
    }

    private void pause() {
        System.out.println("Print the ID of download that you want to set on pause: ");
        infoSpecialized();
        command = sc.nextLine().trim();
        int id = Integer.parseInt(command);
        if (id > countOfFileLoaders) {
            throw new IllegalArgumentException("Wrong ID, try again");
        } else {
            filesToLoad.get(id).setPause();
        }
    }

    private void resume() {
        System.out.println("Print the ID of download that you want to resume: ");
        infoSpecialized();
        command = sc.nextLine().trim();
        int id = Integer.parseInt(command);
        if (id > countOfFileLoaders) {
            throw new IllegalArgumentException("Wrong ID, try again");
        } else if (!filesToLoad.get(id).isPaused()) {
            System.out.println("Sorry, this download isn't paused");
        } else {
            filesToLoad.get(id).resume();
            threads.remove(threads.get(id));
            threads.add(id, new Thread(filesToLoad.get(id)));
            threads.get(id).start();
        }
    }

    private static boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            System.out.println("Unknown command, try again");
            return false;
        }
    }

    private void setFileName(String url) {
        String regex = "^((?:https)||(?:http)||(?:ftp)||(?:pop3)||(?:smtp)):\\/\\/([\\w.\\/]*)\\/([\\w-]+)((?:[.][\\w]+)||([png\\/]))";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        boolean t = matcher.find();
        fileName = matcher.group(3);
    }
}
