package dev.gustavo.twilio.model;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MockUsers {

    private Map<String, Users> activeUsers;


    @PostConstruct
    public void mockUser(){
        activeUsers = new HashMap<>();

        activeUsers.put("11122233344", new Users("11122233344", "João Silva", "quinhentos reais"));
        activeUsers.put("55566677788", new Users("55566677788", "Maria Oliveira", "mil duzentos e cinquenta reais e setenta e cinco centavos"));
        activeUsers.put("00000000000", new Users("00000000000", "Gustavo Exemplo", "dez mil reais e cinquenta centavos"));
    }

    // Getter para acessar o mapa
    public Map<String, Users> getActiveUsers() {
        return activeUsers;
    }

    // Método de conveniência para buscar usuário diretamente
    public Users findUser(String cpf) {
        return activeUsers.get(cpf);
    }
}