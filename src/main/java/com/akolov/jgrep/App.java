package com.akolov.jgrep;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;


public class App {
    public static void main(String[] args) throws IOException {

        Options options = new Options();

        options.addOption(new Option("d", "dir", true, "directory to scan") {
            {
                setRequired(true);
            }
        });
        options.addOption(new Option("i", "implements", true, "Implementing interface") {
            {
                setRequired(false);
            }
        });
        options.addOption(new Option(null, "file-implements", true, "File containing interfaces to implement ") {
            {
                setRequired(false);
            }
        });
        options.addOption(new Option("p", "packages", true, "Package Prefixes to match ") {
            {
                setRequired(false);
            }
        });


        CommandLineParser parser = new BasicParser();
        CommandLine line = null;
        try {
            // parse the command line arguments
            line = parser.parse(options, args);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            usage(options);
            return;
        }


        String dir = line.getOptionValue("d");
        if (dir == null) {
            usage(options);
            return;
        }


        Jgrep jgrep = new Jgrep(dir,
                line.getOptionValue("implements"),
                line.getOptionValue("file-implements"),
                line.getOptionValue("packages")
        );

        jgrep.run();

    }

    private static void usage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("jgrep: Scans a folder and lists Java file matching some conditions.", options);
    }
}
