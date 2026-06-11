package com.motd.be.common.filter.security_filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.motd.be.common.utils.CookieUtils;
import com.motd.be.module.member.auth.facade.AuthFacade;
import com.motd.be.redis.domain.repository.RedisBanListRepository;
import com.motd.be.redis.domain.repository.RedisBlackListRepository;

import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
abstract class AbstractJwtFilterTest {

	@Mock
	protected RedisBlackListRepository redisBlackListRepository;
	@Mock
	protected RedisBanListRepository redisBanListRepository;
	@Mock
	protected CookieUtils cookieUtils;
	@Mock
	protected FilterChain filterChain;
	@Mock
	protected AuthFacade authFacade;

	@BeforeEach
	void setupKeys() {
		JwtProviderTestAccessor.setKeys(
			Keys.hmacShaKeyFor(
				"tokenSecrettokenSecrettokenSecretasdbnasbdjhhasjdhkjashjdk".getBytes()),
			Keys.hmacShaKeyFor(
				"refreshSecretrefreshSecretrefreshSecretaskdjalksjdjashdjkhasjkdhajskd".getBytes())
		);
	}

	protected MockHttpServletRequest createRequest() {
		return new MockHttpServletRequest();
	}

	protected MockHttpServletResponse createResponse() {
		return new MockHttpServletResponse();
	}
}
