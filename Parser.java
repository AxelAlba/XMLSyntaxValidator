import java.io.*;
import java.util.*;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.lang.*;

public class Parser {

  BufferedReader reader;

  public Parser(String s) {
    try {
      reader = new BufferedReader(new StringReader(s));
      char c = (char) reader.read();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  boolean isNumber(char c) {
    if (c >= '0' && c <= '9')
      return true;

    return false;
  }

  boolean isLetter(char c) {
    if (c >= 'a' && c <= 'z')
      return true;
    if (c >= 'A' && c <= 'Z')
      return true;

    return false;
  }

  boolean isValidTag (String s)
  {
    for (int i = 0; i < s.length(); i++)
    {
      if(!(isLetter(s.charAt(i)) || isNumber(s.charAt(i))|| s.charAt(i) == '_'))
        return false;
    }
    return true;
  }

  String removeSpaces(String input) {
    return input.replaceAll("\\s+", "");
  }

  // reads until ">"
  String readTag() {
    String line = "";
    try {
      char test = (char) reader.read();
      while (test != '>') {
        line += test;
        test = (char) reader.read();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }

    // removes unnecessary spaces
    line = line.replaceAll("( )+", " ");
    line = line.replace("= ", "=");
    line = line.replace(" =", "=");
    line = line.replace(" ?", "?");

    return line;
  }

  boolean isDeclarationValid() {
    int i = 14;
    String s = "";

    try {
      if ((char) reader.read() == '<') {
        String line = readTag();
        if (line.substring(0, 14).equals("?xml version=\"") && line.charAt(line.length() - 1) == '?') {
          while (line.charAt(i) != '\"' && line.charAt(i) != (char) (-1))
            s += line.charAt(i++);
          try {
            Double.parseDouble(s);
            s = "";
            if (line.contains("encoding=\"")) {
              if (line.substring(i += 2, i + 10).equals("encoding=\"")) {
                i += 10;
                while (line.charAt(i) != '\"') {
                  s += line.charAt(i++);
                }
              }
            }
            if (!(line.length() - 2 == i))
              return false;
          } catch (NumberFormatException e) {
            return false;
          }
        } else
          return false;
      } else
        return false;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  boolean isAttributeValid(String s) {

    String[] splited = s.split("=");
    if (splited.length != 2)
      return false;

    for (int i = 0; i < splited[0].length(); i++){
      if(!(isLetter(splited[0].charAt(i)) || isNumber(splited[0].charAt(i))|| splited[0].charAt(i) == '_'))
        return false;
    }

    for (int i = 1; i < splited[1].length() - 1; i++){
      if((splited[1].charAt(i) == '"') && (splited[1].charAt(i-1) != '\\'))
        return false;
    }

    if (splited[1].charAt(0) == '"' && splited[1].charAt(splited[1].length() - 1) == '"') {
      if (splited[1].length() - 1 == 0)
        return false;
    } else {
      return false;
    }

    return true;
  }

  String readElement(String s, int i) {
    Pattern p = Pattern.compile("\"(.*?)\"");
    Matcher m = p.matcher(s);
    StringBuffer sb = new StringBuffer("");
    while (m.find()) {
      m.appendReplacement(sb, "\"" + removeSpaces(m.group(1)) + "\"");
    }
    m.appendTail(sb);

    String[] splited = sb.toString().split("\\s+");

    boolean valid = true;
    if (i == 1){
      splited[0] = splited[0].substring(1, splited[0].length());
      if (splited.length > 1) return "";
    }

    int j = 1;
    if (splited.length > 1) {
      while (j < splited.length) {
        if (!(isAttributeValid(splited[j]))) {
          splited[0] = "";
          return splited[0];
        }
        j++;
      }
    }

    if (splited[0].contains(":") || splited[0].contains("-") || splited[0].contains("."))
      valid = false;

    if (!valid)
      splited[0] = "";
    return splited[0];
  }

  boolean isValidXml() {
    boolean wasEmpty = false;
    boolean valid = true;
    // what we should use
    Stack<String> xmlStack = new Stack<>();

    if (!isDeclarationValid()) {
      return false;
    }
    // only for testing
    ArrayList<String> strings = new ArrayList<>();
    try {
      char test = (char) reader.read();
      while (test != (char) (-1)) {
        if (test == '<') {
          String line = readTag();
          strings.add(line);
          if (!(line.charAt(0) == '/')) {
            if (!(line.charAt(line.length() - 1) == '/')) {
              line = readElement(line, 0);
              if (!(line.equals(""))) {
                if (wasEmpty) {
                  return false;
                } else {
                  xmlStack.push(line);
                  if (!(isValidTag(xmlStack.peek())))
                    return false;
                }
              } else {
                return false;
              }
            } else {
              line = readElement(line, 0);
              if ((line.equals(""))) {
                return false;
              } 
            }
          } else {
            line = readElement(line, 1);
            if (!(line.equals(""))) {
              if (line.equals(xmlStack.pop())) {
                if (xmlStack.empty()) {
                  wasEmpty = true;
                }
              } else {
                return false;
              }
            } else {
              return false;
            }
          }

        }
        test = (char) reader.read();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (!xmlStack.empty())
      return false;
    return valid;
  }

  public static void main(String[] args) {
    Scanner in = new Scanner(new BufferedInputStream(System.in));
    String userInputResult = "";
    while (in.hasNextLine()) {
      String line = in.nextLine();
      userInputResult += "\n" + line;
    }
    in.close();

    try {
      Parser parser = new Parser(userInputResult.toString());

      if (parser.isValidXml())
        System.out.println("YES");
      else
        System.out.println("NO");

    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}