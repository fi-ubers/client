package com.example.android;

/**
 * A simple interface to use along with {@link ConexionRest} objects. All classes
 * implementing this interface can execute their executeUpdate method right after
 * {@link ConexionRest} have finished their request.
 */
interface RestUpdate {

	/**
	 * This method is only invoked AFTER {@link ConexionRest} request has been responsed.
	 * @param servResponse The REST API response from server reached.
	 */
	public void executeUpdate(String servResponse);
}