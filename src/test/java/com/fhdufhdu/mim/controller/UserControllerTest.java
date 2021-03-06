package com.fhdufhdu.mim.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fhdufhdu.mim.dto.user.UserInfoDto;
import com.fhdufhdu.mim.dto.user.UserLoginDto;
import com.fhdufhdu.mim.dto.user.UserSignUpDto;
import com.fhdufhdu.mim.entity.Role;
import com.fhdufhdu.mim.mock.WithMockCustomUser;
import com.fhdufhdu.mim.security.JwtTokenProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Slf4j
public class UserControllerTest {
	@Autowired
	WebApplicationContext wac;

	MockMvc mock;
	ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setup() {
		mock = MockMvcBuilders
				.webAppContextSetup(wac)
				.apply(springSecurity())
				.build();
	}

	@Test
	public void ?????????() throws Exception {
		UserLoginDto user = UserLoginDto.builder().id("fhdufhdu").pw("fhdufhdu").build();
		mock.perform(
				post("/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(user)))
				.andExpect(status().isOk())
				.andExpect(header().exists(JwtTokenProvider.ACCESS_HEADER))
				.andExpect(header().exists(JwtTokenProvider.REFRESH_HEADER));
	}

	@Test
	public void ????????????() throws Exception {
		UserSignUpDto user = UserSignUpDto.builder().id("testUser").pw("testUser").build();
		mock.perform(
				post("/sign-up")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(user)))
				.andExpect(status().isCreated());

		mock.perform(
				post("/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(user)))
				.andExpect(status().isOk())
				.andExpect(header().exists(JwtTokenProvider.ACCESS_HEADER))
				.andExpect(header().exists(JwtTokenProvider.REFRESH_HEADER));
	}

	@Test
	public void ?????????_??????_??????() throws Exception {
		UserSignUpDto user = UserSignUpDto.builder().id("testUser").pw("testUser").nickName("testUser").build();
		mock.perform(
				post("/sign-up")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(user)))
				.andExpect(status().isCreated());

		ResultActions checkId = mock.perform(get("/users/id/testUser"));
		checkId
				.andExpect(content().string("true"));

		ResultActions checkId1 = mock.perform(get("/users/id/testUser1"));
		checkId1
				.andExpect(content().string("false"));
	}

	@Test
	public void ?????????_??????_??????() throws Exception {
		UserSignUpDto user = UserSignUpDto.builder().id("testUser").pw("testUser").nickName("testUser").build();
		mock.perform(
				post("/sign-up")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(user)))
				.andExpect(status().isCreated());

		ResultActions checkId = mock.perform(get("/users/nick-name/testUser"));
		checkId
				.andExpect(content().string("true"));

		ResultActions checkId1 = mock.perform(get("/users/nick-name/testUser1"));
		checkId1
				.andExpect(content().string("false"));
	}

	@Test
	@WithMockCustomUser(username = "fhdufhdu", roles = { Role.USER })
	public void ????????????????????????_??????() throws Exception {
		ResultActions actions = mock.perform(get("/users/fhdufhdu"));

		actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is("fhdufhdu")));
	}

	@Test
	@WithMockCustomUser(username = "admin", roles = { Role.ADMIN })
	public void ????????????????????????_?????????() throws Exception {
		ResultActions actions = mock.perform(get("/users/fhdufhdu"));

		actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is("fhdufhdu")));

	}

	@Test
	@WithMockCustomUser(username = "another", roles = { Role.USER })
	public void ????????????????????????_??????() throws Exception {
		ResultActions actions = mock.perform(get("/users/fhdufhdu"));

		actions
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockCustomUser(username = "admin", roles = { Role.ADMIN })
	public void ??????????????????() throws Exception {
		ResultActions actions = mock.perform(get("/users"));

		actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()", greaterThan(1)));
	}

	@Test
	@WithMockCustomUser(username = "fhdufhdu", roles = { Role.USER })
	public void ??????????????????_??????() throws Exception {
		UserInfoDto user = UserInfoDto.builder().id("fhdufhdu").nickName("modified").build();
		ResultActions actions = mock.perform(put("/users/fhdufhdu")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user)));

		actions
				.andExpect(status().isOk());

		ResultActions info = mock.perform(get("/users/fhdufhdu"));

		info
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nickName", is("modified")));
	}

	@Test
	@WithMockCustomUser(username = "admin", roles = { Role.ADMIN })
	public void ??????????????????_?????????() throws Exception {
		UserInfoDto user = UserInfoDto.builder().id("fhdufhdu").nickName("modified").build();
		ResultActions actions = mock.perform(put("/users/fhdufhdu")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user)));

		actions
				.andExpect(status().isOk());

		ResultActions info = mock.perform(get("/users/fhdufhdu"));

		info
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nickName", is("modified")));
	}

	@Test
	@WithMockCustomUser(username = "another", roles = { Role.USER })
	public void ??????????????????_??????() throws Exception {
		UserInfoDto user = UserInfoDto.builder().id("fhdufhdu").nickName("modified").build();
		ResultActions actions = mock.perform(put("/users/fhdufhdu")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user)));

		actions
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockCustomUser(username = "fhdufhdu", roles = { Role.USER })
	public void ??????????????????_??????() throws Exception {
		ResultActions actions = mock.perform(delete("/users/fhdufhdu"));

		actions
				.andExpect(status().isOk());

		ResultActions info = mock.perform(get("/users/fhdufhdu"));

		info
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockCustomUser(username = "admin", roles = { Role.ADMIN })
	public void ??????????????????_?????????() throws Exception {
		ResultActions actions = mock.perform(delete("/users/fhdufhdu"));

		actions
				.andExpect(status().isOk());

		ResultActions info = mock.perform(get("/users/fhdufhdu"));

		info
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockCustomUser(username = "another", roles = { Role.USER })
	public void ??????????????????_??????() throws Exception {
		ResultActions actions = mock.perform(delete("/users/fhdufhdu"));

		actions
				.andExpect(status().isBadRequest());
	}
}
