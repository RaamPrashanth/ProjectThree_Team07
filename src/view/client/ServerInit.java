package view.client;

import network.server.Server;

import java.io.*;
import java.nio.channels.*;
/*
To make sure only one instance of Emoji Server running, when launched from Client.
 */
public class ServerInit {
  private String appName;
  private static File file;
  private FileChannel channel;
  private FileLock lock;

  private static ServerInit serverInitInstance;

  public static ServerInit getInstance() {
    if (serverInitInstance == null) {
      serverInitInstance = new ServerInit("EmojiServer");
    }
    return serverInitInstance;
  }

  public ServerInit(String appName) {
    this.appName = appName;
  }

  public boolean isAppActive() {
    try {
      file = new File
        (System.getProperty("user.home"), appName + ".tmp");
      channel = new RandomAccessFile(file, "rw").getChannel();

      try {
        lock = channel.tryLock();
      } catch (OverlappingFileLockException e) {
        // already locked
        closeLock();
        return true;
      }

      if (lock == null) {
        closeLock();
        return true;
      }

      Runtime.getRuntime().addShutdownHook(new Thread() {
        // destroy the lock when the JVM is closing
        public void run() {
          closeLock();
          deleteFile();
        }
      });
      return false;
    } catch (Exception e) {
      closeLock();
      return true;
    }
  }

  public void closeLock() {
    try {
      lock.release();
    } catch (Exception e) {
    }
    try {
      channel.close();
    } catch (Exception e) {
    }
  }

  public void deleteFile() {
    try {
      file.delete();
    } catch (Exception e) {
    }
  }

  static void loadServer() {
    ServerInit serverInstance = ServerInit.getInstance();

    if (serverInstance.isAppActive()) {
      System.out.println("Already active.");
    }
    else {
      System.out.println("NOT already active.");
      try {
        Runtime.getRuntime().exec("java -jar server.jar");
      }
      catch (Exception e) {  }
    }


//    try {
//      if (true) {
//
//        Runtime.getRuntime().exec("java -jar server.jar");
//      } else {
//        System.out.println("Server running");
//      }
//    } catch (Exception ex) {
//      ex.printStackTrace();
//    }
  }
}
