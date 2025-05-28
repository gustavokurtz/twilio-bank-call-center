package dev.gustavo.twilio.model;

public class Users {

    private String cpf;
    private String nomeTitular;
    private String saldo; // Usaremos string para facilitar a leitura na mensagem de voz

    public Users(String cpf, String nomeTitular, String saldo) {
        this.nomeTitular = nomeTitular;
        this.saldo = saldo;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNomeTitular() {
        return nomeTitular;
    }

    public void setNomeTitular(String nomeTitular) {
        this.nomeTitular = nomeTitular;
    }

    public String getSaldo() {
        return saldo;
    }

    public void setSaldo(String saldo) {
        this.saldo = saldo;
    }
}
