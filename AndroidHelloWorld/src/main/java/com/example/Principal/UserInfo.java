package com.example.Principal;

/**
 * Singleton class that represents the data of the user that
 * has logged into the application.
 */
public class UserInfo {
    private static UserInfo instancia = null;
	private Boolean wasInitialized;

    private String email, userName, userId, birthdate;
    /**
     * NOT FOR USE! Exists only to prevent instantiation.
     */
    protected UserInfo() {
        // Exists only to prevent instantiation.
        email = "fiuber@example.com";
        userName = "Fiuber Fiuber";
        userId= "Fiuber0";
        birthdate= "09/01/2017";
        wasInitialized = false;
    }

    /**
     * Get the only singleton instance of the class.
     * @return  Current singleton instance
     */
    public static UserInfo getInstance() {
        if(instancia == null)
            instancia = new UserInfo();
        return instancia;
    }

    /**
     * Retrieves user email If UserInfo wasn't initialized,
     * returns default value.
     */
    public String getEmail(){
        return email;
    }

    /**
     * Retrieves user userName. If UserInfo wasn't initialized,
     * returns default value.
     */
    public String getUserName(){
        return userName;
    }

    /**
     * Retrieves user birthdate. If UserInfo wasn't initialized,
     * returns default value.
     */
    public String getBirthdate(){
        return birthdate;
    }

    /**
     * Retrieves user userId. If UserInfo wasn't initialized,
     * returns default value.
     */
    public String getUserId(){
        return userId;
    }

    /**
     * Returns true if the UserInfo was already initialized, or
     * false otherwise.
     */
    public Boolean wasInitialized(){
        return wasInitialized;
    }

    /**
     * Initialize the UserInfo struct with the given parameters.
     *
     */
    public void initializeUserInfo(String mail, String name, String id, String birthdate){
        email = mail;
        userName = name;
        userId = id;
        this.birthdate = birthdate;
        wasInitialized = true;
    }

    /**
     * Returns true if the UserInfo struct will change if it
     * were updated with the fields received (i.e. returns true
     * if any of the parameters received is different from the
     * current on the UserInfo struct).
     *
     */
    public Boolean infoWillChange(String mailNew, String nameNew, String idNew, String birthdateNew){
        Boolean areTheSame = email.equals(mailNew);
        areTheSame &= userName.equals(nameNew);
        areTheSame &= birthdate.equals(birthdateNew);
        areTheSame &= userId.equals(idNew);

        return (!areTheSame);
    }

    /**
     * Destroys all the current UserInfo information. After this call,
     * UserInfo will behave like if it was never initiliazed.
     *
     */
    public void seppuku(){
        email = "fiuber@example.com";
        userName = "Fiuber Fiuber";
        userId= "Fiuber0";
        birthdate= "09/01/2017";
        wasInitialized = false;
    }
}
