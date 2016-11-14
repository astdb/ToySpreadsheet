// javac 1.8.0_66
// java version "1.8.0_66"
// Java(TM) SE Runtime Environment (build 1.8.0_66-b17)
// Java HotSpot(TM) 64-Bit Server VM (build 25.66-b17, mixed mode)
// ----------------------------------
// 3.19.0-64-generic GNU/Linux
// Distributor ID:	Ubuntu
// Release:	14.04
// Codename:	trusty

// stack to implement postfix expression evaluation: each 'cell' in the spreadSheet has an
// expression in postfix (i.e. 'reverse polish') notation

import java.util.*;
import java.io.*;

public class CellRef {
  public int row;
  public int column;

  public CellRef(int row, int col){
    this.row = row;
    this.column = col;
  }

  public boolean equals(Object o){
    if( !(o instanceof CellRef) ){
      return false;
    }

    CellRef cr = (CellRef)o;

    if(cr.row == this.row && cr.column == this.column) {
      return true;
    }

    return false;
  }
}
