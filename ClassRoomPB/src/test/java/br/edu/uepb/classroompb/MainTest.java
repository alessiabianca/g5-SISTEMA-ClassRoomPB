package br.edu.uepb.classroompb;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.Test;

public class MainTest {
  @Test
  public void testMain() {
    InputStream sysInBackup = System.in;
    try {
      ByteArrayInputStream in = new ByteArrayInputStream("0\n".getBytes());
      System.setIn(in);
      Main.main(new String[] {});
    } catch (Exception e) {

    } finally {
      System.setIn(sysInBackup);
    }
  }

  @Test
  public void testInstantiate() {
    new Main();
  }
}
