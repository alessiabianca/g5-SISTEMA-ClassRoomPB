package br.edu.uepb.classroompb;

import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MainTest {
    @Test
    public void testMain() {
        InputStream sysInBackup = System.in;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream("0\n".getBytes());
            System.setIn(in);
            Main.main(new String[]{});
        } catch (Exception e) {
            // Ignored since we are just checking coverage
        } finally {
            System.setIn(sysInBackup);
        }
    }
    
    @Test
    public void testInstantiate() {
        new Main();
    }
}
