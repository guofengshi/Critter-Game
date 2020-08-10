package parse;

import java.io.IOException;
import java.io.Reader;

public class LookAheadBuffer {
   
   private final Reader in;
   private final char[] buf;
   private int hd = 0; // index we pop from
   private int tl = 0; // index we push to
   private final int capacity;
   public final static char EOF = 0x7F; // ascii DEL - indicates end of stream

   LookAheadBuffer(int capacity, Reader in) {
      this.capacity = capacity;
      this.in = in;
      buf = new char[capacity + 1]; // need one more than capacity
   }

   LookAheadBuffer(LookAheadBuffer input){
      this.capacity = input.capacity;
      this.in = input.in;
      this.buf = input.buf;
      this.hd = input.hd;
      this.tl = input.tl;
   }
   
   public char next() throws IOException {
      if (!empty()) return pop();
      int r = in.read();
      return r == -1 ? EOF : (char)r;
   }
   
   public char peek(int n) throws IOException {
      assert n < capacity;
      while (size() <= n) {
         int r = in.read();
         push(r == -1 ? EOF : (char)r);
      }
      return buf[(hd + n) % buf.length];
   }
   
   public char peek() throws IOException {
      return peek(0);
   }
   
   private int size() {
      return (tl - hd + buf.length) % buf.length;
   }

   public char scanAndPeek() throws IOException {
      next();
      return peek();
   }

   private boolean empty() {
      return size() == 0;
   }
   
   private boolean full() {
      return size() == capacity;
   }
   
   private void push(char c) {
      assert !full();
      buf[tl++] = c;
      tl %= buf.length;
   }
   
   private char pop() {
      assert !empty();
      char c = buf[hd++];
      hd %= buf.length;
      return c;
   }
   
}
