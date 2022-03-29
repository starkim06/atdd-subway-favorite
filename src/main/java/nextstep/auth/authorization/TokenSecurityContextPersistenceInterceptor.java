package nextstep.auth.authorization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import nextstep.auth.context.Authentication;
import nextstep.auth.context.SecurityContext;
import nextstep.auth.token.JwtTokenProvider;

public class TokenSecurityContextPersistenceInterceptor extends SecurityContextInterceptor {

	private JwtTokenProvider jwtTokenProvider;

	public TokenSecurityContextPersistenceInterceptor(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	protected SecurityContext getSecurityContext(HttpServletRequest request) {
		String credentials = AuthorizationExtractor.extract(request, AuthorizationType.BEARER);
		if (!jwtTokenProvider.validateToken(credentials)) {
			return null;
		}

		return extractSecurityContext(credentials);
	}

    private SecurityContext extractSecurityContext(String credentials) {
        try {
            String payload = jwtTokenProvider.getPayload(credentials);
            TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {
            };

            Map<String, String> principal = new ObjectMapper().readValue(payload, typeRef);
            return new SecurityContext(new Authentication(principal));
        } catch (Exception e) {
            return null;
        }
    }
}
