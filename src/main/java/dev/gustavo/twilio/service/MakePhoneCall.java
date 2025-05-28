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
    private static final String BASE_URL = "https://c5f6-2804-1cd8-ce23-5e0-7839-6cf1-5a18-989.ngrok-free.app"; // Mantenha seu URL do ngrok

    public String createCall() {
        try {
            if (ACCOUNT_SID == null || AUTH_TOKEN == null) {
                throw new IllegalStateException("Credenciais do Twilio não configuradas");
            }

            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

            String from = "+17275948202"; // Seu número Twilio
            String to = "+5532999165667";   // Número de destino

            Call call = Call.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(from),
                    URI.create(BASE_URL + "/voice") // Endpoint inicial que retorna o menu
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
            Say menuSay = new Say.Builder("Olá, tudo bem? Digite 1 se quer Falar com o suporte, ou 2 se quer consultar seu saldo.")
                    .voice(Say.Voice.POLLY_VITORIA)
                    .language(Say.Language.PT_BR)
                    .build();

            Gather gather = new Gather.Builder()
                    .action(BASE_URL + "/voice/escolha") // Endpoint que processará a escolha 1 ou 2
                    .method(HttpMethod.POST)
                    .timeout(10)
                    .numDigits(1) // Espera 1 dígito para a escolha do menu
                    .inputs(Gather.Input.DTMF)
                    .say(menuSay)
                    .build();

            Say fallbackSay = new Say.Builder("Não recebi sua escolha. Tchau!")
                    .voice(Say.Voice.POLLY_VITORIA)
                    .language(Say.Language.PT_BR)
                    .build();

            VoiceResponse voiceResponse = new VoiceResponse.Builder()
                    .gather(gather)
                    .say(fallbackSay) // Será dito se o Gather do menu expirar
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
            VoiceResponse voiceResponse; // A resposta TwiML será construída aqui

            switch (digits) {
                case "1":
                    Say gustavoSay = new Say.Builder("Oi! Aqui é o Gustavo! Suporte do Banco !")
                            .voice(Say.Voice.POLLY_RICARDO)
                            .language(Say.Language.PT_BR)
                            .build();
                    // Para a opção 1, apenas falamos a mensagem.
                    voiceResponse = new VoiceResponse.Builder().say(gustavoSay).build();
                    break;

                case "2":
                    // Usuário escolheu opção 2 - Solicitar CPF
                    Say cpfPromptSay = new Say.Builder("Olá! Digite seu CPF para consultar seu saldo!")
                            .voice(Say.Voice.POLLY_CAMILA)
                            .language(Say.Language.PT_BR)
                            .build();

                    // Criar o Gather para capturar os 11 dígitos do CPF
                    Gather gatherCPF = new Gather.Builder()
                            .action(BASE_URL + "/cpf") // Novo endpoint para processar o CPF
                            .method(HttpMethod.POST)
                            .timeout(30) // Aumentei o timeout para dar tempo de digitar o CPF
                            .numDigits(11) // Espera 11 dígitos
                            .inputs(Gather.Input.DTMF)
                            .say(cpfPromptSay) // Mensagem antes de coletar o CPF
                            .build();

                    // Mensagem de fallback se o usuário não digitar o CPF a tempo
                    Say noCpfInputSay = new Say.Builder("Não recebi seu CPF. Tchau!")
                            .voice(Say.Voice.POLLY_CAMILA)
                            .language(Say.Language.PT_BR)
                            .build();

                    // Para a opção 2, a resposta é o Gather para o CPF.
                    // O noCpfInputSay será dito se o Gather do CPF expirar.
                    voiceResponse = new VoiceResponse.Builder()
                            .gather(gatherCPF)
                            .say(noCpfInputSay)
                            .build();
                    break;

                default:
                    Say invalidOptionSay = new Say.Builder(
                            "Opção inválida. Por favor, digite 1 para Falar com o suporte ou 2 para consultar seu saldo. Tchau!"
                    )
                            .voice(Say.Voice.POLLY_VITORIA)
                            .language(Say.Language.PT_BR)
                            .build();
                    // Para opção inválida, apenas falamos a mensagem.
                    voiceResponse = new VoiceResponse.Builder().say(invalidOptionSay).build();
                    break;
            }

            String xml = voiceResponse.toXml();
            logger.info("Resposta TwiML gerada para escolha {}: {}", digits, xml);
            return xml;

        } catch (Exception e) {
            logger.error("Erro ao processar escolha: ", e);
            throw new RuntimeException("Erro ao processar escolha: " + e.getMessage());
        }
    }

}