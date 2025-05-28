package dev.gustavo.twilio.service;

import com.twilio.Twilio;
import com.twilio.http.HttpMethod;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Say;
import com.twilio.twiml.voice.Gather;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

@Service
public class MakePhoneCall {

    private static final Logger logger = LoggerFactory.getLogger(MakePhoneCall.class);

    // Credenciais
    private static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    private static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
    private static final String BASE_URL = "https://c5f6-2804-1cd8-ce23-5e0-7839-6cf1-5a18-989.ngrok-free.app";

    public String createCall() {
        try {
            if (ACCOUNT_SID == null || AUTH_TOKEN == null) {
                throw new IllegalStateException("Credenciais do Twilio não configuradas");
            }

            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

            String from = "+17275948202";
            String to = "+5532999165667";

            Call call = Call.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(from),
                    URI.create(BASE_URL + "/voice")
            ).setMethod(HttpMethod.POST).create();

            logger.info("Chamada criada com sucesso. Call SID: {}", call.getSid());
            return "Chamada criada com sucesso! Call SID: " + call.getSid();

        } catch (Exception e) {
            logger.error("Erro ao criar chamada: ", e);
            throw new RuntimeException("Erro ao criar chamada: " + e.getMessage());
        }
    }

    // Primeira interação - apresenta o menu
    public String createVoiceMenu() {
        try {
            // Criar o elemento Say com a pergunta
            Say menuSay = new Say.Builder("Olá, tudo bem? Digite 1 se quer ouvir Gustavo, ou 2 se quer ouvir Leticia")
                    .voice(Say.Voice.POLLY_VITORIA)  // Voz feminina brasileira
                    .language(Say.Language.PT_BR)
                    .build();

            // Criar o Gather para capturar a escolha
            Gather gather = new Gather.Builder()
                    .action(BASE_URL + "/voice/escolha")
                    .method(HttpMethod.POST)
                    .timeout(10)
                    .numDigits(1)
                    .inputs(Gather.Input.DTMF)
                    .say(menuSay)
                    .build();

            // Mensagem de fallback se não escolher nada
            Say fallbackSay = new Say.Builder("Não recebi sua escolha. Tchau!")
                    .voice(Say.Voice.POLLY_VITORIA)
                    .language(Say.Language.PT_BR)
                    .build();

            // Criar a resposta TwiML
            VoiceResponse voiceResponse = new VoiceResponse.Builder()
                    .gather(gather)
                    .say(fallbackSay)
                    .build();

            String xml = voiceResponse.toXml();
            logger.info("Menu TwiML gerado: {}", xml);
            return xml;

        } catch (Exception e) {
            logger.error("Erro ao gerar menu de voz: ", e);
            throw new RuntimeException("Erro ao gerar menu de voz: " + e.getMessage());
        }
    }

    // Processa a escolha do usuário (1 ou 2)
    public String processarEscolha(String digits) {
        try {
            logger.info("Usuário escolheu: {}", digits);

            Say responseSay;

            if ("1".equals(digits)) {
                // Usuário escolheu opção 1 - Gustavo (voz masculina)
                responseSay = new Say.Builder("Oi! Aqui é o Gustavo! Como você está? Espero que esteja tendo um ótimo dia!")
                        .voice(Say.Voice.POLLY_RICARDO)  // Voz masculina brasileira
                        .language(Say.Language.PT_BR)
                        .build();

            } else if ("2".equals(digits)) {
                // Usuário escolheu opção 2 - Leticia (voz feminina)
                responseSay = new Say.Builder("Olá! Eu sou a Leticia! É um prazer falar com você. Como posso ajudar hoje?")
                        .voice(Say.Voice.POLLY_CAMILA)  // Voz feminina brasileira diferente
                        .language(Say.Language.PT_BR)
                        .build();

            } else {
                // Opção inválida
                responseSay = new Say.Builder("Opção inválida. Por favor, digite 1 para Gustavo ou 2 para Leticia. Tchau!")
                        .voice(Say.Voice.POLLY_VITORIA)
                        .language(Say.Language.PT_BR)
                        .build();
            }

            // Criar resposta TwiML com a mensagem escolhida
            VoiceResponse voiceResponse = new VoiceResponse.Builder()
                    .say(responseSay)
                    .build();

            String xml = voiceResponse.toXml();
            logger.info("Resposta TwiML gerada para escolha {}: {}", digits, xml);
            return xml;

        } catch (Exception e) {
            logger.error("Erro ao processar escolha: ", e);
            throw new RuntimeException("Erro ao processar escolha: " + e.getMessage());
        }
    }

}