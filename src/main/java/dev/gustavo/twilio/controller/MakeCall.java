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

@RestController
public class MakeCall {

    private static final Logger logger = LoggerFactory.getLogger(MakeCall.class);

    @Autowired
    private MakePhoneCall service;


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

    @PostMapping("/voice")
    public String createVoice(){
        return service.createVoiceMenu();
    }

    // Endpoint que processa a escolha (1 ou 2)
    @PostMapping("/voice/escolha")
    public ResponseEntity<String> processarEscolha(@RequestParam(name = "Digits", required = false) String digits) {
        try {
            logger.info("Recebida escolha do usuário: {}", digits);

            // Se não recebeu nenhum dígito
            if (digits == null || digits.trim().isEmpty()) {
                digits = "0"; // Valor padrão para escolha inválida
            }

            String twimlResponse = service.processarEscolha(digits);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(twimlResponse);

        } catch (Exception e) {
            logger.error("Erro ao processar escolha: ", e);

            String errorTwiml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<Response><Say language=\"pt-BR\">Erro ao processar sua escolha.</Say></Response>";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);

            return ResponseEntity.status(500)
                    .headers(headers)
                    .body(errorTwiml);
        }
    }



}