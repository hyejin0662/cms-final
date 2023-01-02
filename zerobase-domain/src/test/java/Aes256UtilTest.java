import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zerobase.domain.util.Aes256Util;
import org.junit.jupiter.api.Test;


class Aes256UtilTest {

    @Test
    void encrypt() {
        String encrypt = Aes256Util.encrypt("Hello World!");
        assertEquals(Aes256Util.decrypt(encrypt) , "Hello World!");
    }

}