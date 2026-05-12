package utils;

import org.junit.jupiter.api.Test;

public class UtilsTest {

	@Test
	public void test(){
assert Utils.sanitizeString("'") .equals("'\\''");  // bare quote
assert Utils.sanitizeString("'\\''").equals("'\\''"); // already escaped
assert Utils.sanitizeString("hello").equals("'hello'");
assert Utils.sanitizeString("it's").equals("'it\\'s'");
	}

	
}
