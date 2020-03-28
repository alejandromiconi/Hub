package hub.commons;

import java.util.ArrayList;
import java.util.List;

public class Commons {
	
	public static String replaceCtrls(String word) {
		return word.replaceAll("\\p{Cc}", "");
	}

	public static List<String>getTokens(String word , String separators) {
		
		List<String> tokens = new ArrayList<String>();
		
		if (word == null || word.isEmpty()) return tokens;
		
		boolean instr = false;
		
		String token = "";
		
		for(int i=0 ; i<word.length() ; i++) {
			
			char c = word.charAt(i);
			
			if (instr) {
				
				if (c == '"') {
					instr = false;
					tokens.add(token);
					token = "";
				}

				else {
					token += c;
				}
			}

			else if (c == '"') {
				instr = true;
			}
			
			else if (Character.isWhitespace(c));
			
			else if (c<32 || c>127); // Excluimos caracteres raros!
			
			else {
				
				boolean found = false;
				for(int s = 0; s<separators.length() ; s++) {
					if (c == separators.charAt(s)) {
						found = true;
					}
				}
				
				if (found) {
					tokens.add(token);
					token = "";
				}
				
				else token += c;
			}
		}

		if (!token.isEmpty()) {
			tokens.add(token);
		}
		
		return tokens;
	}
}
