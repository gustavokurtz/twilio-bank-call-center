package dev.gustavo.twilio.controller;

import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Say;
import dev.gustavo.twilio.service.MakePhoneCall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
public class MakeCall {

    private static final Logger logger = LoggerFactory.getLogger(MakeCall.class);

    @Autowired
    private MakePhoneCall service;

    // Classe interna para dados mockados da conta
    private static class DadosContaMock {
        String nomeTitular;
        String saldo; // Usaremos string para facilitar a leitura na mensagem de voz

        public DadosContaMock(String nomeTitular, String saldo) {
            this.nomeTitular = nomeTitular;
            this.saldo = saldo;
        }
    }

    // Mock de banco de dados com algumas contas
    private static final Map<String, DadosContaMock> contasMock = new HashMap<>();

    static {
        // CPF 1 (exemplo)
        contasMock.put("11122233344", new DadosContaMock("Gustavo Teste", "quinhentos e setenta e cinco reais e trinta centavos"));
        // CPF 2 (exemplo)
        contasMock.put("55566677788", new DadosContaMock("Leticia Exemplo", "mil duzentos e cinquenta reais"));
        // CPF 3 (exemplo)
        contasMock.put("00000000000", new DadosContaMock("Cliente VIP", "dez mil reais e cinquenta centavos"));
    }


    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Servidor funcionando!");
    }


    @GetMapping("/hello")
    public ResponseEntity<String> createCall() {
        try {
            String result = service.createCall();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Erro no controller: ", e);
            return ResponseEntity.status(500)
                    .body("Erro interno: " + e.getMessage());
        }
    }

    @PostMapping(value = "/voice", produces = MediaType.APPLICATION_XML_VALUE)
    public String createVoice(){
        return service.createVoiceMenu();
    }

    @PostMapping(value = "/voice/escolha", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> processarEscolha(@RequestParam(name = "Digits", required = false) String digits) {
        try {
            logger.info("Recebida escolha do usuário: {}", digits);

            if (digits == null || digits.trim().isEmpty()) {
                // Considera como opção inválida se nenhum dígito for recebido (ex: timeout no primeiro gather)
                digits = "fallback"; // Um valor que seu service.processarEscolha pode tratar como default/inválido
            }

            String twimlResponse = service.processarEscolha(digits);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML); // Twilio espera XML

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(twimlResponse);

        } catch (Exception e) {
            logger.error("Erro ao processar escolha: ", e);

            // Resposta de erro em TwiML
            Say errorSay = new Say.Builder("Desculpe, ocorreu um erro ao processar sua escolha. Tente novamente mais tarde.")
                    .voice(Say.Voice.POLLY_VITORIA)
                    .language(Say.Language.PT_BR)
                    .build();
            VoiceResponse errorResponse = new VoiceResponse.Builder().say(errorSay).build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);

            return ResponseEntity.status(500)
                    .headers(headers)
                    .body(errorResponse.toXml());
        }
    }


    @PostMapping(value = "/cpf", produces = MediaType.APPLICATION_XML_VALUE)
    public String processarCpf(@RequestParam("Digits") String cpfDigits) {
        logger.info("CPF Recebido para consulta: {}", cpfDigits);

        String mensagem;
        Say.Voice voz = Say.Voice.POLLY_CAMILA; // Voz padrão para este fluxo

        if (contasMock.containsKey(cpfDigits)) {
            DadosContaMock conta = contasMock.get(cpfDigits);
            mensagem = String.format("Olá %s. Seu saldo atual é de %s.", conta.nomeTitular, conta.saldo);
            logger.info("CPF {} encontrado. Titular: {}. Saldo: {}", cpfDigits, conta.nomeTitular, conta.saldo);
        } else {
            mensagem = "Desculpe, não encontramos informações para o CPF digitado. Por favor, verifique os números e tente novamente.";
            logger.warn("CPF {} não encontrado na base mockada.", cpfDigits);
        }

        Say responseSay = new Say.Builder(mensagem)
                .voice(voz)
                .language(Say.Language.PT_BR)
                .build();

        // Adiciona uma mensagem final e desliga a chamada.
        Say goodbyeSay = new Say.Builder("Obrigado por utilizar nossos serviços. Até logo!")
                .voice(voz)
                .language(Say.Language.PT_BR)
                .build();

        VoiceResponse voiceResponse = new VoiceResponse.Builder()
                .say(responseSay)
                .say(goodbyeSay)
                .build();

        return voiceResponse.toXml();
    }
}