package tcp;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class Auth {
	private Map<String, String> users = new HashMap<String, String>();

	Auth() {
		this.users.put("jesse", "ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413"); // 123456
		this.users.put("jhonatan", "93f4a4e86cf842f2a03cd2eedbcd3c72325d6833fa991b895be40204be651427652c78b9cdbdef7c01f80a0acb58f791c36d49fbaa5738970e83772cea18eba1"); // 123mudar
		this.users.put("rodrigo", "3f1835a63e7a938adb4aef0ce4cab36dd220d24f5ee0e3dce11f9d8af67f2131c17181e00ca5c68ba4114d076c5aaa86c2bd3fda521d7cf9ed91bbbc8178c672"); // sistemas operacionais
	}
	
	public boolean verifyUser(String username, String password) {
		String pass = this.users.get(username);
		
		if (pass.equals(null)) {
			return false;
		}
		
		return pass.equals(password);
	}
	
	public String getHash(String password) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-512");
	    digest.reset();
	    digest.update(password.getBytes("utf8"));
	    return String.format("%0128x", new BigInteger(1, digest.digest()));
	}
}
