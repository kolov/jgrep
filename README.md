# jgrep
Search Java files on disk and print those matching conditions. 

# Usage

Java files are parsed but not actually compiled agains a classpath. 

    usage: jgrep: Search Java files on disk and print those matching conditions. Java files are parsed but not actually compiled agains a classpath. 
     -d,--dir <arg>               directory to scan
     --file-implements <arg>   File containing interfaces to implement
     -i,--implements <arg>        Implementing interface
     -p,--packages <arg>          Package Prefixes to match
     
     
# Example

    jgrep -d ~/projects/someproject --implements my.project.Service
    About to scan 4194 Java files (from 11962 files total)
    Searching matches for implements:
     - my.project.Service
    Found matches: 15
    /Users/assen/projects/someproject/src/main/java/nl/com/somorg/sercices/MyServiceImpl
    
# Q & A
Q: Why the trouble, my IDE can do that much better?  
A: For usage from the command line or scripts. This tool can list 80 files implementing 60 different interfaces.
