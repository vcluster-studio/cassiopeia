package xyz.vcluster.cassiopeia.authentication.provider;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 无加密的密码编译器.
 *
 * @author cassiopeia
 */
public class NoneEncoder implements PasswordEncoder {

    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
        }

        return (String) rawPassword;
    }

    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
        }

        return encodedPassword.contentEquals(rawPassword);
    }
}
