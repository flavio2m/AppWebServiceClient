package com.appwebserviceclient;

/**
 * Created by flavio2m@gmail.com on 29/09/2019.
 */
public class Discente {
    private long id;
    private String nome;
    private String email;
    private String numero;
    private String curso;
    private String ano;

    public Discente() {
    }

    public Discente(long id, String m, String n, String e, String c, String a) {
        this.id = id;
        this.numero = m;
        this.nome = n;
        this.email = e;
        this.curso = c;
        this.ano = a;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public String getAno() {
        return ano;
    }

    public void setAno(String ano) {
        this.ano = ano;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    @Override
    public String toString() {
        return "Discente{" +
                "  id=" + id +
                ", numero='" + numero + '\'' +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", curso='" + curso + '\'' +
                ", ano='" + ano + '\'' +
                '}';
    }
}

