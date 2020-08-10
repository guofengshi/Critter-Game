package clientserver;

/**
 * this bundle is used for the login request
 */
class LoginBundle {

    String level;
    String password;

    LoginBundle(String l, String p) {
        level = l;
        password = p;
    }
}