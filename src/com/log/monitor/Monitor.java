package com.log.monitor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Monitor {

	Path logFile = Paths.get("logsFile/application.log");
    private int lines = 0;
    private int characters = 0;

    public static void main(String[] args)
    {
        new Monitor().run();
    }

    public void run()
    {
        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();

            try (BufferedReader in = new BufferedReader(new FileReader(logFile.toFile()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    lines++;
                    characters += line.length() + System.lineSeparator().length();
                    //System.out.println(characters);
                }
            }

            logFile.toAbsolutePath().getParent().register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

            do {
                WatchKey key = watcher.take();
                System.out.println("Waiting...");
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                    Path path = pathEvent.context();
//                    System.out.println();
//                    if (path.equals(logFile)) {
                    System.out.println(pathEvent.context().toFile());
                        try (BufferedReader in = new BufferedReader(new FileReader("logsFile/"+pathEvent.context().toFile()))) {
                            String line;
                            Pattern p = Pattern.compile("WARN|ERROR");
                            in.skip(characters);
                            System.out.println(lines);
                            while ((line = in.readLine()) != null) {
                                lines++;
                                characters += line.length() + System.lineSeparator().length();
                                if (p.matcher(line).find()) {
                                    // Do something
                                    System.out.println(line);
                                }
                            }
                        }
//                    }
                }
                key.reset();
            } while (true);
        } catch (IOException | InterruptedException ex) {
        	ex.printStackTrace();
          //  Logger.getLogger(LogScanner.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}
