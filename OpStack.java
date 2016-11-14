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

public class OpStack {
  private double[] stackstore;
  private int size;
  private int capacity;

  public OpStack(int maxsize){
    this.stackstore = new double[maxsize];
    this.capacity = maxsize;
    this.size = 0;
  }

  public void printStack(){
    if (this.size == 0) {
      System.out.println("[<empty>] (" + this.size + ")");
      return;
    }

    System.out.println(Arrays.toString(this.stackstore));
  }

  public int size(){
    return this.size;
  }

  public boolean push(double item) {
    if (this.size < this.capacity) {
      System.out.println("Pushing <" + item + "> on..");
      this.stackstore[this.size] = item;
      this.size++;
      return true;
    }

    return false;
  }

  public double pop() {
    if( this.size > 0 && this.size <= this.capacity ) {
      size--;
      System.out.println("Popping <" + this.stackstore[size] + "> off..");
      double item = this.stackstore[size];
      this.stackstore[size] = 0.0;
      return item;
    }

    // TODO: you probably need something better than -1 to return here
    return -1.0;
  }
}
