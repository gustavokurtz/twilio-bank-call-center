package dev.gustavo.twilio.service.setup;

import com.twilio.Twilio;
import com.twilio.http.HttpMethod;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;
import dev.gustavo.twilio.model.MockUsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

@Service
public class MakePhoneCall {

    private static final Logger logger = LoggerFactory.getLogger(MakePhoneCall.class);

    @Autowired
    private MockUsers mock;

    // Credenciais
    private static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    private static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");

    @Value("${api.base.url}")
    private String BASE_URL;// Mantenha seu URL do ngrok

    public String criarLigacao() {
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


}
