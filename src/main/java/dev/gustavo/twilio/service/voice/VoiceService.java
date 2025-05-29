package dev.gustavo.twilio.service.voice;

import com.twilio.http.HttpMethod;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Gather;
import com.twilio.twiml.voice.Say;
import dev.gustavo.twilio.service.setup.MakePhoneCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VoiceService {
    private static final Logger logger = LoggerFactory.getLogger(MakePhoneCall.class);

    @Value("${api.base.url}")
    private String BASE_URL;

    // Primeira interação - apresenta o menu
    public String construirMenuPrincipalVoz() {
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
    public String processarOpcaoSelecionada(String digits) {
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



    public String validarEProcessarOpcaoMenu(String digits){
        try {
            logger.info("Recebida escolha do usuário: {}", digits);
            if (digits == null || digits.trim().isEmpty()) {
                digits = "fallback";
            }
            return processarOpcaoSelecionada(digits);
        } catch (Exception e) {
            logger.error("Erro ao processar escolha: ", e);
            Say errorSay = new Say.Builder("Desculpe, ocorreu um erro ao processar sua escolha. Tente novamente mais tarde.")
                    .voice(Say.Voice.POLLY_VITORIA).language(Say.Language.PT_BR).build();
            VoiceResponse errorResponse = new VoiceResponse.Builder().say(errorSay).build();

            throw new RuntimeException("Erro no processamento: " + errorResponse.toXml());
        }
    }

}
