package dev.gustavo.twilio.controller;

import dev.gustavo.twilio.service.setup.MakePhoneCall;
import dev.gustavo.twilio.service.cpf.CpfService;
import dev.gustavo.twilio.service.voice.VoiceService;
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

    @Autowired
    private CpfService cpfService;

    @Autowired
    private VoiceService voiceService;


    @GetMapping("/hello")
    public ResponseEntity<String> iniciarChamadaTelefonica() {
        try {
            String result = service.criarLigacao();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Erro no controller: ", e);
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }

    @PostMapping(value = "/voice", produces = MediaType.APPLICATION_XML_VALUE)
    public String exibirMenuInicialVoz() {
        return voiceService.construirMenuPrincipalVoz();
    }

    @PostMapping(value = "/voice/escolha", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> processarOpcaoMenuPrincipal(@RequestParam(name = "Digits", required = false) String digits) {
        try {
            String twimlResponse = voiceService.validarEProcessarOpcaoMenu(digits);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            return ResponseEntity.ok().headers(headers).body(twimlResponse);
        } catch (Exception e) {
            logger.error("Erro ao processar escolha: ", e);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            return ResponseEntity.status(500).headers(headers).body(e.getMessage().replace("Erro no processamento: ", ""));
        }
    }

    @PostMapping(value = "/cpf", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> consultarSaldoPorCpf(@RequestParam("Digits") String cpfDigits) {
        return ResponseEntity.ok(cpfService.executarConsultaSaldoCpf(cpfDigits));
    }
}