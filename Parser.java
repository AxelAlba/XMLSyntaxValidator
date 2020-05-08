import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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

  // Returns true if char is a number
  boolean isNumber(char c) {
    if (c >= '0' && c <= '9')
      return true;

    return false;
  }

  // Returns true if char is an alphabet
  boolean isLetter(char c) {
    if (c >= 'a' && c <= 'z')
      return true;
    if (c >= 'A' && c <= 'Z')
      return true;

    return false;
  }

  // Returns true if tag is valid
  boolean isValidTag (String s)
  {
    for (int i = 0; i < s.length(); i++)
    {
      if(!(isLetter(s.charAt(i)) || isNumber(s.charAt(i))|| s.charAt(i) == '_'))
        return false;
    }
    return true;
  }

  // removes whitespaces
  String removeSpaces(String input) {
    return input.replaceAll("\\s+", "");
  }

  /*
   * returns a string of tags and attributes excluding '<' and '>'
   */
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

  // Checks the validity of XML declaration
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

  // Returns true if an attribute is valid
  boolean isAttributeValid(String s) {

    String[] splited = s.split("=");

    // if string doesn't follow the form attr="value", reject it
    if (splited.length != 2)
      return false;

    // if attribute name contains anything but alphabet, number, _, reject it
    // problem: it accepts attribute names starting with number 
    for (int i = 0; i < splited[0].length(); i++){
      if(!(isLetter(splited[0].charAt(i)) || isNumber(splited[0].charAt(i))|| splited[0].charAt(i) == '_'))
        return false;
    }

    // checks for illegal unescaped quotes WITHIN enclosing quotes 
    // e.g. "value"as"
    for (int i = 1; i < splited[1].length() - 1; i++){
      if((splited[1].charAt(i) == '"') && (splited[1].charAt(i-1) != '\\'))
        return false;
    }

    // if the string starts and end with quotes
    if (splited[1].charAt(0) == '"' && splited[1].charAt(splited[1].length() - 1) == '"') {
      if (splited[1].length() - 1 == 0) // if empty string, reject
        return false;
    } else { // if doesn't start and end with quotes
      return false;
    }

    return true;
  }

  /* tokenizer
   * it also checks validity of attr-value pair and checks the validity 
   * of tag  name
   */
  String readElement(String s, int i) {
    Pattern p = Pattern.compile("\"(.*?)\"");
    Matcher m = p.matcher(s);
    StringBuffer sb = new StringBuffer("");
    while (m.find()) { // reads attribute values
      m.appendReplacement(sb, "\"" + removeSpaces(m.group(1)) + "\""); // cleans whitespaces from attribute values
    }
    m.appendTail(sb); // append remaining parts after attributes

    // splits the string on whitespaces (tokenize)
    // e.g. ?xml, attr="value", attr2="value2"
    String[] splited = sb.toString().split("\\s+");

    boolean valid = true;
    if (i == 1){ // if closing tag, extract tag name excluding the closing  symbol '/'
      splited[0] = splited[0].substring(1, splited[0].length());
      if (splited.length > 1) return ""; // if closing tag consists of multiple names e.g. </color blue>
    }

    // input: element name, attribut-value pair
    // checks if the input is a valid attribute
    // it start at first attribute splited[1]
    int j = 1;
    if (splited.length > 1) {
      while (j < splited.length) {
        if (!(isAttributeValid(splited[j]))) { // if there exists one invalid attribute, return "" which means reject
          splited[0] = "";
          return splited[0];
        }
        j++;
      }
    }

    // rejects names w/ :, -, . 
    if (splited[0].contains(":") || splited[0].contains("-") || splited[0].contains("."))
      valid = false;

    if (!valid)
      splited[0] = "";

    return splited[0]; // if success, it returns tag name
  }

  // Returns true if input is a valid XML
  boolean isValidXml() {
    boolean wasEmpty = false;
    boolean valid = true;
    
    Stack<String> xmlStack = new Stack<>();

    // check for validity of XML declartion
    if (!isDeclarationValid()) {
      return false;
    }
    
    try {
      char test = (char) reader.read();
      while (test != (char) (-1)) { // reads one char at a time, 
        if (test == '<') {
          String line = readTag(); // returns tag names and attributes between '<' and '>'
          if (!(line.charAt(0) == '/')) { // if not a closing tag (line, 0)
            if (!(line.charAt(line.length() - 1) == '/')) { // if not a self closing tag
              line = readElement(line, 0); // returns tag name, if "" it is invalid
              if (!(line.equals(""))) { // if valid tag name
                if (wasEmpty) { 
                  return false;
                } 
                else { // if valid 
                  xmlStack.push(line);
                  if (!(isValidTag(xmlStack.peek()))) // reject if tag is invalid
                    return false;
                }
              } else { // if invalid tag name
                return false;
              }
            } else { // if self closing tag
              line = readElement(line, 0);
              if ((line.equals(""))) {
                return false;
              } 
            }
          } else { // if closing tag (line, 1)
            line = readElement(line, 1); // empty string if closing tag consist of multiple names e.g. </color blue> aka invalid tag
            if (!(line.equals(""))) { // if valid closing tag
              if (line.equals(xmlStack.pop())) {
                if (xmlStack.empty()) {
                  wasEmpty = true;
                }
              } else { // this means improper nesting
                return false;
              }
            } else { // invalid closing tag e.g. </color blue>
              return false;
            }
          }

        }
        test = (char) reader.read(); // keep on reading characters
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (!xmlStack.empty()) // if there are residues in stack, reject
      return false;

    return valid; // if you've reached this, xml is valid
  }

  public static void main(String[] args) {
    // Take input from terminal (HackerRank)
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