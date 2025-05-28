package dev.gustavo.twilio.controller;

import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Say;
import dev.gustavo.twilio.model.Users; // Sua classe Users
import dev.gustavo.twilio.service.MakePhoneCall;
import jakarta.annotation.PostConstruct; // Para inicializar após a construção
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

    // Mapa para armazenar os usuários. A chave é o CPF (String).
    private Map<String, Users> activeUsers;

    // Este método será chamado automaticamente após o controller ser construído
    // e as dependências injetadas. Ideal para inicializar seus dados.
    @PostConstruct
    public void initializeUsers() {
        activeUsers = new HashMap<>();

        // Instancie e adicione seus objetos Users ao mapa
        // e getters como getCpf(), getNome() (ou getNomeTitular()), getSaldo().
        activeUsers.put("11122233344", new Users("11122233344", "João Silva", "quinhentos reais"));
        activeUsers.put("55566677788", new Users("55566677788", "Maria Oliveira", "mil duzentos e cinquenta reais e setenta e cinco centavos"));
        activeUsers.put("00000000000", new Users("00000000000", "Gustavo Exemplo", "dez mil reais e cinquenta centavos"));

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
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }

    @PostMapping(value = "/voice", produces = MediaType.APPLICATION_XML_VALUE)
    public String createVoice() {
        return service.createVoiceMenu();
    }

    @PostMapping(value = "/voice/escolha", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> processarEscolha(@RequestParam(name = "Digits", required = false) String digits) {
        try {
            logger.info("Recebida escolha do usuário: {}", digits);
            if (digits == null || digits.trim().isEmpty()) {
                digits = "fallback";
            }
            String twimlResponse = service.processarEscolha(digits);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            return ResponseEntity.ok().headers(headers).body(twimlResponse);
        } catch (Exception e) {
            logger.error("Erro ao processar escolha: ", e);
            Say errorSay = new Say.Builder("Desculpe, ocorreu um erro ao processar sua escolha. Tente novamente mais tarde.")
                    .voice(Say.Voice.POLLY_VITORIA).language(Say.Language.PT_BR).build();
            VoiceResponse errorResponse = new VoiceResponse.Builder().say(errorSay).build();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            return ResponseEntity.status(500).headers(headers).body(errorResponse.toXml());
        }
    }

    @PostMapping(value = "/cpf", produces = MediaType.APPLICATION_XML_VALUE)
    public String processarCpf(@RequestParam("Digits") String cpfDigits) {
        logger.info("CPF Recebido para consulta: {}", cpfDigits);

        String mensagem;
        Say.Voice voz = Say.Voice.POLLY_CAMILA;

        // 1. Validação de formato do CPF
        if (cpfDigits == null || !cpfDigits.matches("\\d{11}")) {
            mensagem = "CPF inválido. Por favor, digite os onze números do seu CPF.";
            logger.warn("Formato de CPF inválido recebido: {}", cpfDigits);
        } else {
            // 2. Buscar o usuário no mapa 'activeUsers'
            Users foundUser = activeUsers.get(cpfDigits);

            if (foundUser != null) {
                // Usuário encontrado! Use os dados dele.
                // Supondo que sua classe Users tem getNome() e getSaldo()
                mensagem = String.format("Olá %s. Seu saldo atual é de %s.", foundUser.getNomeTitular(), foundUser.getSaldo());
                logger.info("CPF {} encontrado. Usuário: {}. Saldo: {}", cpfDigits, foundUser.getNomeTitular(), foundUser.getSaldo());
            } else {
                // Usuário não encontrado no mapa
                mensagem = "Desculpe, não encontramos informações para o CPF digitado. Por favor, verifique os números e tente novamente.";
                logger.warn("CPF {} com formato válido, mas não encontrado no mapa de usuários ativos.", cpfDigits);
            }
        }

        Say responseSay = new Say.Builder(mensagem)
                .voice(voz)
                .language(Say.Language.PT_BR)
                .build();

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