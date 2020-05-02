import java.io.*;
import java.util.*;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Parser {

  BufferedReader reader;

  public Parser(File file){
    try{
      reader = new BufferedReader(new FileReader(file));
    }
    catch(Exception e){
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

  String removeSpaces(String input) {
    return input.replaceAll("\\s+", "");
  }

//reads until ">"
  String readTag () {
    String line = "";
    try {
      char test = (char)reader.read();
      while (test != '>') {
        line += test;
        test = (char)reader.read();
      }
      System.out.println(line);
      test = (char)reader.read();
    } catch(Exception e) {
      e.printStackTrace();
    }

    //removes unnecessary spaces
    line = line.replaceAll("( )+", " ");
    line = line.replace("= ", "=");
    line = line.replace(" =", "=");
    line = line.replace(" ?", "?");

    return line;
  }


//check here if the line is the xml declaration
  /* 
    1.) starts with ? and ends with ?
    2.) first string is 'xml'. Has attributes, version and encoding.
    3.) version should have a value of a float.
    4.) encoding -> any string
    5.) DO NOT PUSH TO STACK.
  */
//works
  boolean isDeclarationValid () {
    int i = 14;
    String s = "";

    try {
      if ((char)reader.read() == '<') {
        String line = readTag();
        if (line.substring(0, 14).equals("?xml version=\"") && line.charAt( line.length() - 1 ) == '?') {
          while (line.charAt(i) != '\"' && line.charAt(i) != (char)(-1)) 
           s += line.charAt(i++); 
          try {
            Double.parseDouble(s);
            s = "";
            if (line.contains("encoding=\"")) {
              if (line.substring(i += 2, i + 10).equals("encoding=\"")) {
                i += 10;
                while (line.charAt(i) != '\"'){ s += line.charAt(i++); }
              }
            }
            if (!(line.length() - 2 == i)) return false;
          } catch (NumberFormatException e) { 
            return false;
           }
        } 
        else return false; 
      } 
      else  return false; 
    } catch(Exception e) {
      e.printStackTrace();
    }

    return true;
  }

//Checkes if "something="blablabla" is valid"
//works
  boolean isAttributeValid (String s) {
    boolean valid = true;
    System.out.println(s);

    String[] splited = s.split("=");
    if (splited.length != 2) return false;

    if (splited[0].contains(":") || splited[0].contains("-") || splited[0].contains(".")) 
      valid = false;
    
    if (splited[1].charAt(0) == '"' && splited[1].charAt(splited[1].length() - 1) == '"') {
      if (splited[1].length() - 1 == 0) return false;
    } else {
      valid = false;
    }

    return valid;
  }

//may problema dito
//splits spaces inside quotation marks <- di ko pa mafix <-- fixed
  String readElement (String s, int i) {
   Pattern p = Pattern.compile("\"(.*?)\"");
   Matcher m = p.matcher(s);
   StringBuffer sb = new StringBuffer("");
   while (m.find()) {
       m.appendReplacement(sb, "\"" + removeSpaces(m.group(1)) + "\"");
   }
   m.appendTail(sb);
   
   String[] splited = sb.toString().split("\\s+");

    boolean valid = true;
    if (i == 1) 
      splited[0] = splited[0].substring(1, splited[0].length());

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

    if (!valid) splited[0] = "";
    return splited[0];
  }


  //this function returns either yes or no if the file is a valid XML or not.
  boolean isValidXml(){
    boolean valid = true;
    //what we should use
    Stack<String> xmlStack  = new Stack<>();

    if (!isDeclarationValid()) return false;

    //only for testing
    ArrayList<String> strings = new ArrayList<>();
    try {
      char test = (char)reader.read();
      while (test != (char)(-1))
      {
        if (test == '<')
        {
          String line = readTag();

          //line -> only takes the string inside '<' and '>'
          //testing if the parsing is correct

          strings.add(line);

          /*  
            Main goal here is to push and pop from the stack to determine if it is valid or not. However, in between these lines, an error could occur which would make it not valid

            Notes: 
             - How to implement root element syntax? (link for XML Syntax: https://www.tutorialspoint.com/xml/xml_syntax.htm )
          */


          //check here if the line is the start tag
            /*
              1.)check if there are spaces, if there is, check if the attributes are valid
              2.) check if the line does end with '/' -> tag was ended. DO NOT PUSH TO STACK.
              3.) if it is a valid start stack, PUSH only the element to the stack i.e. <element> -> push 'element'
            */

          //check here if it is the end tag
          /*
            1.) end tags should start with '/'.
            2.) end tags should have the same element name(case-sensitive) as the start tag.
            3.) end tags should have no attributes.
            4.) Pop from stack if it is a valid end tag.
          */
          
          //this works
          if (!(line.charAt(0) == '/')) {
            if(!(line.charAt(line.length()-1) == '/'))
            {
              line = readElement(line, 0);
              if (!(line.equals(""))){
                xmlStack.push(line);
                System.out.println("Pushed " + line + " " + valid);
              } 
              else return false;
            }
            else {
              line = readElement(line, 0);
              if (!(line.equals(""))){
                System.out.println("This line ended by itself: " + line + " -> " + valid);
              } 
              else return false;
            }
          } 
          else {
            line = readElement(line, 1);
            if (!(line.equals(""))){
              if (line.equals(xmlStack.pop())){
                System.out.println("Popped " + line + " " + valid);
              }
              else return false; 
            }
            else return false; 
          }

        }
        test = (char)reader.read();
      }
      for (int i = 0; i < strings.size(); i++)
        System.out.println(strings.get(i));
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    return valid;
  } 


  public static void main(String[] args) {
    /*
      Are we suppose to use something like this for the input of sir in hackerrank?
    */
    //URL Input_url = ClassLoader.getSystemResource("input.txt");  
    File file = null;
    String xmlFile = "input.xml";

    try {
      //file = new File(Input_url.toURI());
      file = new File(xmlFile);
      Parser parser = new Parser(file);

      if (parser.isValidXml()) 
        System.out.println("YES");
      else System.out.println("NO");

    }
    catch (Exception e){
      e.printStackTrace();
    }
  }
}