package com.example.android;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by ale on 11/23/17.
 */
public class UserStatusTest {
	@Test
	public void getCode() throws Exception {
		int code = UserStatus.NO_STATE.getCode();
		assertEquals(code, 0);
		code = UserStatus.P_IDLE.getCode();
		assertEquals(code, 1);
		code = UserStatus.D_ON_DUTY.getCode();
		assertEquals(code, 11);
		code = UserStatus.P_ARRIVED.getCode();
		assertEquals(code, 6);
	}

	@Test
	public void tripCreationEnabled() throws Exception {
		UserStatus st = UserStatus.P_IDLE;
		assertTrue(st.tripCreationEnabled());
		st = UserStatus.D_TRAVELLING;
		assertFalse(st.tripCreationEnabled());
		st = UserStatus.P_WAITING_CONFIRMATION;
		assertFalse(st.tripCreationEnabled());
		st = UserStatus.D_ON_DUTY;
		assertFalse(st.tripCreationEnabled());
	}

	@Test
	public void chatEnabled() throws Exception {
		UserStatus st = UserStatus.P_IDLE;
		assertFalse(st.chatEnabled());
		st = UserStatus.D_TRAVELLING;
		assertTrue(st.chatEnabled());
		st = UserStatus.P_WAITING_CONFIRMATION;
		assertFalse(st.chatEnabled());
		st = UserStatus.P_TRAVELLING;
		assertTrue(st.chatEnabled());
		st = UserStatus.D_ON_DUTY;
		assertFalse(st.chatEnabled());
		st = UserStatus.D_GOING_TO_PIKCUP;
		assertTrue(st.chatEnabled());
		st = UserStatus.P_WAITING_DRIVER;
		assertTrue(st.chatEnabled());
		st = UserStatus.D_WAITING_COFIRMATION;
		assertFalse(st.chatEnabled());
	}

	@Test
	public void chooseTripEnabled() throws Exception {
		UserStatus st = UserStatus.P_IDLE;
		assertFalse(st.choosePassengerEnabled());
		st = UserStatus.D_TRAVELLING;
		assertFalse(st.choosePassengerEnabled());
		st = UserStatus.P_WAITING_CONFIRMATION;
		assertFalse(st.choosePassengerEnabled());
		st = UserStatus.D_ON_DUTY;
		assertTrue(st.choosePassengerEnabled());
	}

	@Test
	public void createFromCode() throws Exception {
		UserStatus st = UserStatus.createFromCode(5);
		assertEquals(st, UserStatus.P_TRAVELLING);
	}

}