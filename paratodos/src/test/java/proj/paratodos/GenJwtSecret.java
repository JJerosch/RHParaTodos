package proj.paratodos;

import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

public class GenJwtSecret {
    public static void main(String[] args) {
        var key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        System.out.println(Encoders.BASE64.encode(key.getEncoded()));
    }
}
