// javac 1.8.0_66
// java version "1.8.0_66"
// Java(TM) SE Runtime Environment (build 1.8.0_66-b17)
// Java HotSpot(TM) 64-Bit Server VM (build 25.66-b17, mixed mode)
// ----------------------------------
// 3.19.0-64-generic GNU/Linux
// Distributor ID:	Ubuntu
// Release:	14.04
// Codename:	trusty

// SpreadSheet reads in a csv file containing cells of postfix expressions, and outputs a csv file with expressions evaluated.

import java.util.*;
import java.io.*;

public class SpreadSheet {
  public static void main(String[] args) {
    // read input CSV file from command line
    if( args.length <= 0 || args[0].trim().length() <= 0 ){
      System.out.println("Please enter an input file name (i.e. $> java Spreadsheet input1.csv)");
      System.exit(1);
    }

    String[][] rowset = readFile(args[0]);

    // ierate through and evaluate each cell
    int rownum = 0;
    for( String[] row : rowset ) {
      int colnum = 0;
      for(String cell : row){
        if( cell != null ) {
          String[] cell_tokens = cell.split(" ");
          ArrayList<CellRef> refchain = new ArrayList<CellRef>();
          CellRef thisref = new CellRef(rownum, colnum);
          refchain.add(thisref);
          System.out.println("Calling evaluateCell(" + rownum + ", " + colnum + ", rowset)");
          rowset[rownum][colnum] = evaluateCell(rownum, colnum, rowset, refchain);
        }
        colnum++;
      }
      rownum++;
    }

    // print evaluated spreadsheet out
    try {
      FileWriter w = new FileWriter(args[0] + "_output.csv");
      for( String[] row : rowset ) {
        int i = 0;
        for(String cell : row) {
          if( i > 0 ) {
            w.append(", " + cell.trim());
          } else {
            w.append(cell.trim());
          }

          i++;
        }
        w.append("\n");
      }
      w.flush();
      w.close();
    } catch (IOException e) {
      System.out.println("Error writing output file.");
    }
  }

  // Takes a cell reference and evaluates that cell's expression
  // NOTE: for cell references within cell expressions, this function will be called recursively.
  // could run into performance issues when evaluating a deep-nested reference chain.
  private static String evaluateCell(int row, int col, String[][] rowset, ArrayList<CellRef> refchain){
    // get cell contents
    System.out.println("rowset[" + row + "][" + col + "] = " + rowset[row][col]);
    String cell_content = rowset[row][col].trim();

    String[] cell_tokens = cell_content.split(" ");
    OpStack opstack = new OpStack(cell_tokens.length);

    double firstPopVal, secondPopVal;

    for( String token: cell_tokens ){
      token = token.trim();
      System.out.println("Token: <" + token + ">");

      if(token.equals("#ERR")) {
        return "#ERR";
      }

      // return zero for empty cell
      if( token.equals("") ) {
        return "0";
      }

      if(isRef(token)){
        // token is another cell reference: evaluate it
        System.out.println("Token type: REFERENCE <" + token + ">");
        System.out.println("token.substring(1): " + token.substring(1));
        System.out.println("token.substring(0,1): " + token.substring(0,1));

        int thiscol = atoi(token.substring(0,1));
        int thisrow = Integer.parseInt(token.substring(1))-1;

        if( thisrow < 0 || thisrow >= rowset.length  ){
          return "#ERR";
        }

        if( thiscol < 0 || thiscol >= rowset[thisrow].length ){
          return "#ERR";
        }

        CellRef thisref = new CellRef(thisrow, thiscol);
        if( refchain.contains(thisref) ){
          return "#ERR";
        }

        refchain.add(thisref);
        System.out.println("Calling evaluateCell(" + thisrow + ", " + thiscol + ", rowset)");
        token = evaluateCell(thisrow, thiscol, rowset, refchain);
      }

      if(!isOp(token) && isNumeric(token)) {
        // token is a value/operand
        System.out.println("Token type: VALUE/OPERAND <" + token + ">");
        opstack.push(Double.parseDouble(token));
        opstack.printStack();
      } else if(isOp(token)) {
        // token is a function/operator
        // we cater for + - / *, which need atleast two operands
        // TODO: what about the unary - operator?? (we may need to implement operation objects which can inform how many operands
        // they need in minimum)
        System.out.println("Token type: FUNCTION/OPERATOR <" + token + ">");
        if( opstack.size() < 2 ){
          // rowset[row][col] = "#ERR";
          return "#ERR";
        } else {
          // pop top two values
          // TODO: check if valid values are popped (or check stacksize is atleast 2)
          firstPopVal = opstack.pop();
          secondPopVal = opstack.pop();

          // operate
          if( token.equals("+") ) {
            opstack.push(secondPopVal + firstPopVal);
            opstack.printStack();
          } else if( token.equals("-") ) {
            opstack.push(secondPopVal - firstPopVal);
            opstack.printStack();
          } else if( token.equals("/") ) {
            opstack.push(secondPopVal / firstPopVal);
            opstack.printStack();
          } else if( token.equals("*") ) {
            opstack.push(secondPopVal * firstPopVal);
            opstack.printStack();
          }
        }
      } else {
        // token is not a cell reference, not an operator, and not numeric (an "abc", perhaps?)
        return "#ERR";
      }
    }

    if(opstack.size() == 1) {
      return String.valueOf(opstack.pop());
    } else {
      return "#ERR";
    }
  }

  // Take an expression-component token (from a cell) and indicates if it's a cell reference (i.e. A1, B5 etc)
  private static boolean isRef(String token){
    token = token.trim();

    if( isOp(token) || isNumeric(token) ) {
      return false;
    }

    // a reference token must be at least two characters long (i.e. A1, B1 etc)
    if( token.length() < 2 ){
      return false;
    }

    String thiscol_str = token.substring(0,1);  // "A" part of a reference token such as A34
    String thisrow_str = token.substring(1);    // "34" part of a reference token such as A34

    if(!isNumeric(thisrow_str)) {
      return false;
    }

    return true;
  }

  // Identify if a given string is an operator
  private static boolean isOp(String op){
    op = op.trim();
    if( op.equals("+") || op.equals("-") || op.equals("/") || op.equals("*") ) {
      return true;
    }

    return false;
  }

  // checks if a given string is numeric http://stackoverflow.com/a/1102916 :s
  public static boolean isNumeric(String str) {
    if( str == null ){
      return false;
    }

    try {
      double d = Double.parseDouble(str);
    } catch(NumberFormatException nfe){
      return false;
    }
    return true;
  }

  // Takes a case-insensitive alpha letter(A/a-Z/z) from the first part of a cell reference and converts into a corresponding index int (A->0 to Z->25)
  // all valid return values would be >= 0 (rowset[][] index)
  // TODO: this will later have to be modified to take in multiple-letter column references such as AB, AAB etc.
  private static int atoi(String letter_str) {
    letter_str = letter_str.trim();
    if( letter_str.length() != 1 ) {
      // ATM, only converting A-Z to 0-25
      return -1;
    }

    letter_str = letter_str.toUpperCase();
    char letter_char = letter_str.charAt(0);
    return Character.getNumericValue(letter_char)-10;
  }

  // read in a 'spreadsheet' in a given filename
  private static String[][] readFile(String filename){
		Scanner sheet = null;
    Scanner precount_rows = sheet;

		try {
			sheet = new Scanner(new File(filename));
      precount_rows = new Scanner(new File(filename));
		}	catch( FileNotFoundException e ) {
			System.out.println("No such file");
		}

    int sheet_rows = 0;
    while( precount_rows.hasNextLine() ) {
      String thisLine = precount_rows.nextLine();
      sheet_rows++;
    }

    // array of rows
    String[][] rowset = new String[sheet_rows][];

    int rowcount = 0;
		while( sheet.hasNextLine() ) {
      // read row
      String this_row = sheet.nextLine();

      // split row into individual cells
      String[] cells_this_row = this_row.split(",");
      rowset[rowcount] = cells_this_row;
      rowcount++;
    }

    return rowset;
  }
}
