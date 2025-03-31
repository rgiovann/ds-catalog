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