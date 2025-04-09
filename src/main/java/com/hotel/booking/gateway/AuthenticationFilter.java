package com.hotel.booking.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import lombok.NoArgsConstructor;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
  @Autowired
  private RouterValidator routerValidator;// custom route validator

  @Autowired
  private JwtUtils jwtUtil;

  public AuthenticationFilter() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      ServerHttpRequest request = exchange.getRequest();

      if (routerValidator.isSecured.test(request)) {
        if (this.isAuthMissing(request))
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization header is missing");
        // return this.onError(exchange, "Authorization header is missing in request",
        // HttpStatus.UNAUTHORIZED);

        String header = this.getAuthHeader(request);
        String token = header.substring(7, header.length());

        if (!jwtUtil.validateJwtToken(token)) {
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization header is expired");
          // return this.onError(exchange, "Authorization header is invalid",
          // HttpStatus.UNAUTHORIZED);
        }

        this.populateRequestWithHeaders(exchange, token);
      }

      return chain.filter(exchange);
    };

  }

  /* PRIVATE */

  // private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus
  // httpStatus) {
  // ServerHttpResponse response = exchange.getResponse();
  // Exception error = new ResponseStatusException(httpStatus, err);
  // response.setStatusCode(httpStatus);
  // response.writeWith(Mono.error(error));
  // return response.setComplete();
  // }

  private String getAuthHeader(ServerHttpRequest request) {
    return request.getHeaders().getOrEmpty("Authorization").get(0);
  }

  private boolean isAuthMissing(ServerHttpRequest request) {
    return !request.getHeaders().containsKey("Authorization");
  }

  private void populateRequestWithHeaders(ServerWebExchange exchange, String token) {
    Claims claims = jwtUtil.getAllClaimsFromToken(token);
    exchange.getRequest().mutate()
        .header("id", String.valueOf(claims.get("id")))
        .header("role", String.valueOf(claims.get("role")))
        .build();
  }

  @NoArgsConstructor
  public static class Config {

  }
}
