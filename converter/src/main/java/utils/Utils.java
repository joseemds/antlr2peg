package utils;


public class Utils {
	public static String sanitizeString(String s){
		if(s == null) return null;
        return s
            .replace("\\", "\\\\") 
            .replace("\"", "\\\"")
            .replace("'", "\\'");
		

	};
}
