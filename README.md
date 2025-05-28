# Simulação de Call Center Bancário com Twilio e Spring Web

Este projeto demonstra a criação de uma simulação de Unidade de Resposta Audível (URA) de um call center bancário. Ao acessar uma página web específica e atualizá-la, o sistema automaticamente realiza uma chamada telefônica para um número pré-configurado, guiando o usuário através de um menu interativo para consulta de saldo fictício via CPF.

## Visão Geral

A ideia central é mostrar como a API de voz da Twilio pode ser integrada a uma aplicação backend Java com Spring Web para criar interações telefônicas automatizadas. O fluxo simula uma experiência comum em atendimento bancário:

1.  O usuário (desenvolvedor/testador) atualiza uma página no navegador.
2.  A aplicação backend, ao receber essa requisição, instrui a Twilio a iniciar uma chamada para um número de telefone.
3.  Ao atender, o usuário ouve uma mensagem de boas-vindas e opções de menu.
4.  O usuário interage digitando números no teclado do telefone (DTMF).
5.  Conforme a opção escolhida (ex: consulta de saldo), o sistema solicita informações adicionais (ex: CPF).
6.  Com base no CPF digitado, o sistema consulta dados fictícios (mockados) e informa o saldo correspondente por voz.

## Funcionalidades

* **Chamada Automática:** Disparo de chamada telefônica ao atualizar uma determinada interface web.
* **URA Interativa:** Menu navegável por comandos de voz/DTMF ("Digite 1 para...", "Digite 2 para...").
* **Consulta de Saldo por CPF:** Simulação de verificação de saldo com base em CPFs fictícios pré-cadastrados.
* **Respostas de Voz Dinâmicas:** Uso de Text-To-Speech (TTS) da Twilio para gerar as falas do sistema, incluindo saudações personalizadas e informações de saldo.
* **Dados Mockados:** CPFs e saldos de exemplo para fins didáticos e de demonstração.

## Tecnologias Utilizadas

* **Java:** Linguagem de programação principal.
* **Spring Boot (Spring Web):** Framework para criação da aplicação backend e exposição de endpoints.
* **Twilio SDK para Java:** Biblioteca para interagir com a API da Twilio (especificamente a API de Voz programável).
* **TwiML (Twilio Markup Language):** Linguagem XML utilizada para instruir a Twilio sobre como lidar com as chamadas (o que dizer, quais ações tomar com base na entrada do usuário, etc.).
* **Maven:** Gerenciador de dependências e build do projeto.

## Como Funciona (Fluxo Simplificado)

1.  **Gatilho Web:** Um endpoint na aplicação Spring Boot é acessado (simulado pelo F5 na página).
2.  **Início da Chamada:** A aplicação Spring Boot utiliza o Twilio SDK para fazer uma requisição à API da Twilio, solicitando o início de uma chamada para o número do usuário.
3.  **Servidor TwiML:** A Twilio, ao conectar a chamada, faz uma requisição HTTP a um endpoint da nossa aplicação Spring Boot que retorna instruções em TwiML.
    * Este TwiML inicial geralmente contém a mensagem de boas-vindas e as primeiras opções do menu (usando verbos como `<Say>` e `<Gather>`).
4.  **Interação do Usuário:**
    * O usuário digita uma opção. A Twilio captura esses dígitos (DTMF).
    * A Twilio envia esses dígitos para outro endpoint da aplicação Spring Boot (definido no `<Gather>`).
5.  **Lógica da Aplicação:**
    * A aplicação processa a opção. Se for "consultar saldo", ela retorna um novo TwiML pedindo o CPF.
    * Após o usuário digitar o CPF, a Twilio envia o CPF para outro endpoint.
    * A aplicação "consulta" o saldo (usando os dados mockados) e retorna um TwiML final com a mensagem do saldo e o encerramento.


## Configuração e Execução

1.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/gustavokurtz/twilio-bank-call-center.git](https://github.com/gustavokurtz/twilio-bank-call-center.git)
    cd twilio-bank-call-center
    ```

2.  **Configure suas credenciais Twilio:**
    * Você precisará de um [Twilio Account SID], [Auth Token] e um [Twilio Phone Number] (número de telefone comprado ou verificado na Twilio capaz de fazer chamadas).

3.  **Construa e execute o projeto (Exemplo com Maven):**
    ```bash
    mvn spring-boot:run
    ```
    Ou importe o projeto em sua IDE de preferência e execute a classe principal da aplicação Spring Boot.

4.  **Teste:**
    * Acesse o endpoint configurado para disparar a chamada (geralmente um endpoint GET simples no navegador).
    * Você deverá receber uma ligação no número configurado em `TO_PHONE_NUMBER`.
    * Siga as instruções de voz.

## Observação Importante

* Este projeto foi desenvolvido utilizando uma conta gratuita da Twilio. Por isso, no início de cada chamada, uma mensagem padrão da Twilio ("This is a trial account...") pode ser reproduzida antes do fluxo da aplicação.

---
Sinta-se à vontade para adicionar mais seções como "Como Contribuir", "Licença", ou detalhar mais a configuração se necessário.
