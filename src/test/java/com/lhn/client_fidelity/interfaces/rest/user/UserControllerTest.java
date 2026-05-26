package com.lhn.client_fidelity.interfaces.rest.user;

import com.lhn.client_fidelity.application.user.CreateUserUseCase;
import com.lhn.client_fidelity.application.user.UserRepository;
import com.lhn.client_fidelity.domain.user.User;
import com.lhn.client_fidelity.interfaces.rest.error.RestExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

	private FakeUserRepository repository;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		repository = new FakeUserRepository();
		Clock clock = Clock.fixed(Instant.parse("2026-05-25T23:30:00Z"), ZoneOffset.UTC);
		CreateUserUseCase useCase = new CreateUserUseCase(repository, clock);
		mockMvc = MockMvcBuilders
				.standaloneSetup(new UserController(useCase))
				.setControllerAdvice(new RestExceptionHandler(clock))
				.build();
	}

	@Test
	void createsCommerce() throws Exception {
		mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "type": "COMMERCE",
								  "name": "Padaria Central",
								  "contactName": "Maria Silva",
								  "email": "MARIA@PADARIA.COM",
								  "phone": "+55 11 99999-9999",
								  "governmentIdentifier": "11.222.333/0001-81"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", notNullValue()))
				.andExpect(jsonPath("$.type").value("COMMERCE"))
				.andExpect(jsonPath("$.email").value("maria@padaria.com"))
				.andExpect(jsonPath("$.governmentIdentifier").value("11222333000181"));
	}

	@Test
	void createsCommerceClient() throws Exception {
		mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "type": "COMMERCE_CLIENT",
								  "name": "Joao Souza",
								  "email": "joao@email.com",
								  "phone": "(11) 98888-8888"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", notNullValue()))
				.andExpect(jsonPath("$.type").value("COMMERCE_CLIENT"))
				.andExpect(jsonPath("$.governmentIdentifier").doesNotExist())
				.andExpect(jsonPath("$.contactName").doesNotExist());
	}

	@Test
	void returnsBadRequestForMalformedJson() throws Exception {
		mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("MALFORMED_JSON"));
	}

	@Test
	void returnsBadRequestForMissingType() throws Exception {
		mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("UNKNOWN_USER_TYPE"));
	}

	@Test
	void returnsBadRequestForUnknownType() throws Exception {
		mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "type": "UNKNOWN",
								  "name": "Joao Souza",
								  "email": "joao@email.com",
								  "phone": "(11) 98888-8888"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("UNKNOWN_USER_TYPE"));
	}

	@Test
	void returnsUnprocessableEntityForValidationFailure() throws Exception {
		mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "type": "COMMERCE_CLIENT",
								  "name": "Joao Souza",
								  "email": "invalid",
								  "phone": "(11) 98888-8888"
								}
								"""))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
				.andExpect(jsonPath("$.details[0].field").value("email"));
	}

	@Test
	void returnsUnprocessableEntityForUnknownField() throws Exception {
		mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "type": "COMMERCE_CLIENT",
								  "name": "Joao Souza",
								  "email": "joao@email.com",
								  "phone": "(11) 98888-8888",
								  "unexpected": "value"
								}
								"""))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
	}

	@Test
	void returnsConflictForDuplicateCommerce() throws Exception {
		repository.commerceExists = true;

		mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "type": "COMMERCE",
								  "name": "Padaria Central",
								  "contactName": "Maria Silva",
								  "email": "maria@padaria.com",
								  "phone": "+55 11 99999-9999",
								  "governmentIdentifier": "11.222.333/0001-81"
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("COMMERCE_ALREADY_EXISTS"));
	}

	@Test
	void returnsConflictForDuplicateCommerceClient() throws Exception {
		repository.commerceClientExists = true;

		mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "type": "COMMERCE_CLIENT",
								  "name": "Joao Souza",
								  "email": "joao@email.com",
								  "phone": "(11) 98888-8888"
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("COMMERCE_CLIENT_ALREADY_EXISTS"));
	}

	@Test
	void returnsInternalServerErrorForUnexpectedFailure() throws Exception {
		repository.throwUnexpectedOnSave = true;

		mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "type": "COMMERCE_CLIENT",
								  "name": "Joao Souza",
								  "email": "joao@email.com",
								  "phone": "(11) 98888-8888"
								}
								"""))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.code").value("USER_CREATION_FAILED"));
	}

	private static class FakeUserRepository implements UserRepository {

		private boolean commerceExists;
		private boolean commerceClientExists;
		private boolean throwUnexpectedOnSave;

		@Override
		public boolean existsCommerceByGovernmentIdentifier(String governmentIdentifier) {
			return commerceExists;
		}

		@Override
		public boolean existsCommerceClientByEmail(String email) {
			return commerceClientExists;
		}

		@Override
		public Optional<User> findByEmail(String email) {
			return Optional.empty();
		}

		@Override
		public Optional<User> findByPhone(String phone) {
			return Optional.empty();
		}

		@Override
		public User save(User user) {
			if (throwUnexpectedOnSave) {
				throw new IllegalStateException("Database unavailable");
			}
			return user;
		}
	}
}
