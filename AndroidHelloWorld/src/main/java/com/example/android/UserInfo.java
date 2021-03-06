package com.example.android;

import java.util.ArrayList;

/**
 * Singleton class that represents the data of the user that
 * has logged into the application.
 */
public class UserInfo {
    private static UserInfo instancia = null;
	private Boolean wasInitialized;
    private int integerId;
    private String email, firstName, lastName, country, userId, birthdate;
    private boolean isDriver;
    private ArrayList<CarInfo> cars;
    private String password, fbToken, appServerToken;
    private UserStatus status;
    private OtherUsersInfo other;

    /**
     * NOT FOR USE! Exists only to prevent instantiation.
     */
    protected UserInfo() {
        // Exists only to prevent instantiation.
        isDriver = false;
        integerId = -1;
        email = "fiuber@example.com";
        firstName = "Fiuber";
        lastName = "Fiuber";
        userId= "Fiuber0";
        birthdate= "09/01/2017";
        country="Turkmenistan";
        fbToken = "";
        password = "";
        appServerToken = "";
        wasInitialized = false;
        cars = null;
        other = null;
        status = UserStatus.NO_STATE;
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
     * Retrieves user firstName. If UserInfo wasn't initialized,
     * returns default value.
     */
    public String getFirstName(){
        return firstName;
    }

    /**
     * Retrieves user lastName. If UserInfo wasn't initialized,
     * returns default value.
     */
    public String getLastName(){
        return lastName;
    }

    /**
     * Retrieves user country. If UserInfo wasn't initialized,
     * returns default value.
     */
    public String getCountry(){
        return country;
    }

    /**
     * Retrieves user password. If UserInfo wasn't initialized,
     * returns default value.
     */
    public String getPassword(){
        return password;
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
     * Retrieves user integerId. If UserInfo wasn't initialized,
     * returns default value.
     */
    public int getIntegerId(){
        return integerId;
    }

    /**
     * Sets value of user integerId.
     * @param id The user's integer id
     */
    public void setIntegerId(int id){
        integerId = id;
    }

    /**
     * Retrieves user fbToken. If UserInfo wasn't initialized,
     * returns default value.
     */
    public String getFbToken() {
        return fbToken;
    }

    /**
     * Retrieves user appServerToken. If UserInfo wasn't initialized,
     * returns default value.
     */
    public String getAppServerToken() {
        return appServerToken;
    }

    /**
     * Returns true if the UserInfo was already initialized, or
     * false otherwise.
     */
    public Boolean wasInitialized(){
        return wasInitialized;
    }

    /**
     * Returns true if the user is a driver, or
     * false otherwise.
     */
    public Boolean isDriver(){
        return isDriver;
    }

    /**
     * Retrieves user list of cars. If UserInfo wasn't initialized,
     * returns default value.
     */
    public ArrayList<CarInfo> getCars(){
        return cars;
    }

    /**
     * Retrieves user {@link UserStatus}. If UserInfo wasn't initialized,
     * returns default value.
     */
    public UserStatus getUserStatus(){
        return status;
    }

    /**
     * Sets the user {@link UserStatus} to the received value.
     * @param status {@link UserStatus} to use for the user.
     */
    public void setUserStatus(UserStatus status){
        this.status = status;
    }

    /**
     * Initialize the UserInfo struct with the given parameters.
     *
     */
    public void initializeUserInfo(String id, String mail, String fName, String lName, String country, String birthdate, String password, String fbToken, String appServToken){
        email = mail;
        firstName = fName;
        lastName = lName;
        userId = id;
        this.country = country;
        this.birthdate = birthdate;
        this.fbToken = fbToken;
        this.password = password;
        appServerToken = appServToken;
        wasInitialized = true;
    }

    /**
     * Returns true if the UserInfo struct will change if it
     * were updated with the fields received (i.e. returns true
     * if any of the parameters received is different from the
     * current on the UserInfo struct).
     *
     */
    public Boolean infoWillChange(String mailNew, String nameNew, String surnameNew, String idNew, String birthdateNew, String countryNew){
        Boolean areTheSame = email.equals(mailNew);
        areTheSame &= firstName.equals(nameNew);
        areTheSame &= birthdate.equals(birthdateNew);
        areTheSame &= userId.equals(idNew);
        areTheSame &= lastName.equals(surnameNew);
        areTheSame &= country.equals(countryNew);

        return (!areTheSame);
    }

    /**
     * Sets the current user as driver instead of
     * passenger. Basically a role change. if newCarsList
     * is not null, also updates the user cars' list.
     * @param newCarsList {@link ArrayList} of the {@link CarInfo} to set to the driver
     */
    public void setAsDriver(ArrayList<CarInfo> newCarsList){
        isDriver = true;
        if(newCarsList != null)
            this.cars = newCarsList;
    }

    /**
     * Destroys all the current UserInfo information. After this call,
     * UserInfo will behave like if it was never initiliazed.
     *
     */
    public void seppuku(){
        isDriver = false;
        integerId = -1;
        email = "fiuber@example.com";
        firstName = "Fiuber";
        lastName = "Fiuber";
        userId= "Fiuber0";
        birthdate= "09/01/2017";
        country="Turkmenistan";
        fbToken = "";
        password = "";
        appServerToken = "";
        wasInitialized = false;
        cars = null;
        status = UserStatus.NO_STATE;
        other = null;
    }

    public OtherUsersInfo getOtherUser(){
        return other;
    }

    public void setOtherUser(OtherUsersInfo other){
        this.other = other;
    }

}
