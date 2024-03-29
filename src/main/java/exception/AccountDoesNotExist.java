package exception;



public class AccountDoesNotExist extends RuntimeException {

    private static final long serialVersionUID = 1180822918717228267L;

    public AccountDoesNotExist(String message) {
        super(message);
    }

}