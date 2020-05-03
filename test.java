import java.io.*;
import java.util.*;

public class test {
  public static void main(String[] args) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    // Reading data using readLine
    String name = "";
    String line = "";
    while ((line = reader.readLine()) != null) {
      name += line;
      if(line.isEmpty()) break;
    }
    System.out.println(name);
  }
}

