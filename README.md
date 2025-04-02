<h1>Projeto DS Catalog </h1>
<p><h4> O projeto DS Catalog, consiste em um sistema backend de busca de produtos/categorias construído usando Spring Boot e sua API utiliza a arquitetura REST, 
com nível de maturidade 3 (Richardson Maturity Model). O diagrama de classes está descrito abaixo.
</strong></a>.<br></h4>

<h4>
🛑 <b>CARACTERISTICAS PRINCIPAIS DO PROJETO</b>
</h4>

-   [x] Autenticação utilizando Spring Security (OAuth2/JWT) e token JWT, possuindo também refresh de token caso necessário
-   [x] Rotas protegidas por nivel de acesso do usuário (role)
-   [x] Descrição dos endpoints (swagger) em http://localhost:8080/swagger-ui.html#/product-resource
-   [x] Implementação de queries usando JPQL para filtrar produtos por nome e por categoria
-   [x] Tratamento de diversas exceções de forma a encaminhar mensagem user-friendly para o front-end.
-   [x] Implementação de 37 testes unitários usando Mockito e Junit 5
-   [x] Json do Postman


<p> Os requests do Postman estão disponíveis para importação no arquivo <i>DSCATALOG.postman_collection.json</i>
</strong></a>.<br>

![diagrama de classes](https://github.com/rgiovann/image-repo/blob/main/dscatalog_class_diagram.jpg)

## Migração para Spring Security 3.X (Branch: security-3x-migration)

Esta branch contém a migração da aplicação do Spring Security 2.X para o 3.X, implementando um fluxo de autenticação baseado em OAuth2/JWT com suporte ao `grant_type=password`. Abaixo está a lista de modificações realizadas para adaptar o código à nova versão do Spring Security:

### Modificações Principais

1. **Reestruturação para Arquitetura de Cadeias de Filtros**:
   - Substituí a configuração monolítica do Spring Security 2.X por cadeias de filtros (`SecurityFilterChain`).
   - Criei `AuthorizationServerConfig` para o Authorization Server (endpoint `/oauth2/**`) e `ResourceServerConfig` para proteger recursos (endpoint `/api/**`).

2. **Ajuste de Dependências**:
   - Removi ou substituí o SpringFox (Swagger), que não suporta Spring Boot 3.X/Spring Security 6.X, possivelmente adotando Springdoc OpenAPI como alternativa.

3. **Implementação Manual do `grant_type=password`**:
   - O Spring Security 3.X não oferece suporte nativo ao `grant_type=password`. Implementei:
     - `OAuth2ResourceOwnerPasswordAuthenticationConverter`: Converte a requisição POST `/oauth2/token` em um `OAuth2ResourceOwnerPasswordAuthenticationToken`.
     - `OAuth2ResourceOwnerPasswordAuthenticationProvider`: Autentica o usuário manualmente com `UserDetailsService` e `PasswordEncoder`, retornando um `OAuth2AccessTokenAuthenticationToken`.

4. **Correção de ClassCastException**:
   - Resolvi um erro de cast (`UsernamePasswordAuthenticationToken` para `OAuth2AccessTokenAuthenticationToken`) no `OAuth2TokenEndpointFilter`, ajustando o `authenticate` para gerar o tipo correto de token OAuth2.

5. **Eliminação de Ciclos de Dependência**:
   - Refatorei `AuthorizationServerConfig` para remover o ciclo com `OAuth2ResourceOwnerPasswordAuthenticationProvider`, movendo o `RegisteredClientRepository` para `ClientConfig`.
   - Corrigido um ciclo em `ClientConfig` removendo um `@PostConstruct` que dependia do próprio bean.

6. **Configuração de Chaves JWT com RSA**:
   - Inicialmente usei uma chave simétrica (`OCT`/`HS256`), mas mudei para uma chave assimétrica (`RSA`/`RS256`) para alinhar com o comportamento padrão do `NimbusJwtEncoder`.
   - Configurei o `JwtConfig` para gerar um par de chaves RSA e usar a chave pública no `JwtDecoder`.

7. **Customização do Token e Resposta JSON**:
   - Adicionei `userFirstName` e `userId` ao payload do JWT via `JwtTokenEnhancer`.
   - Corrigi os campos `firstName` e `userId` na resposta JSON do `/oauth2/token`, usando valores do `JwtClaimsSet` no `additionalParameters`.

### Testando a Aplicação

Para gerar um token:
```bash
curl -X POST "http://localhost:8080/oauth2/token" \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "grant_type=password&username=maria@gmail.com&password=123456&client_id=myclientid&client_secret=myclientsecret"