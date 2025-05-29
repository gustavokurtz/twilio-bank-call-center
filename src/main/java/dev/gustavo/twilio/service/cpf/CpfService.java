package dev.gustavo.twilio.service.cpf;

import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Say;
import dev.gustavo.twilio.model.MockUsers;
import dev.gustavo.twilio.model.Users;
import dev.gustavo.twilio.service.setup.MakePhoneCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CpfService {

    private static final Logger logger = LoggerFactory.getLogger(MakePhoneCall.class);

    @Autowired
    private MockUsers mock;

    public String executarConsultaSaldoCpf(String cpfDigits){
        logger.info("CPF Recebido para consulta: {}", cpfDigits);

        String mensagem;
        Say.Voice voz = Say.Voice.POLLY_CAMILA;

        // 1. Validação de formato do CPF
        if (cpfDigits == null || !cpfDigits.matches("\\d{11}")) {
            mensagem = "CPF inválido. Por favor, digite os onze números do seu CPF.";
            logger.warn("Formato de CPF inválido recebido: {}", cpfDigits);
        } else {
            // 2. Buscar o usuário no mapa 'activeUsers'
            Users foundUser = mock.findUser(cpfDigits);

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
