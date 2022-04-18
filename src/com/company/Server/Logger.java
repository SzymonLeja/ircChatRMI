package com.company.Server;

import java.io.*;

public class Logger {
    private static String filename = "serverLog.log";

    public static void log(String message){
        System.out.println(message);
        try(FileWriter fw = new FileWriter(filename, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(message);
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
    }

}
