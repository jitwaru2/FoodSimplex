import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class HelperTest {
  @Test
  public void testThing() {
  	int x = Helper.repeat(4);
  	assertEquals(5, x);
  }
}