package com.appwebserviceclient;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by flavio2m@gmail.com on 29/09/2019.
 * Essa implementação foi realizada baseado no artigo publicado por:
 * Matheus Brandino (https://www.alura.com.br/artigos/facilitando-as-requisicoes-utilizando-okhttp-no-android)
 * Monise Costa (http://www.matera.com/blog/post/receber-dados-via-json-em-um-aplicativo-android)
 * https://androidlift.info/2015/09/21/android-web-service-example/
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ListView list;
    ArrayList<String> relacaoDiscentes = new ArrayList<>();
    ArrayList<Discente> listDiscentes = new ArrayList<>();
    ArrayAdapter<String> adapter;
    Button btNovo, btEditar, btExcluir, btAtualizar;
    int selecionado = -1;
    boolean excluirDiscente;

    //Obs: não pode utilizar 127.0.0.1:xx ou localhost:xx porque é o endereço do dispositovo com android
    //Se utilizar a porta 80 não necesidade de especificar (está implícita)
    String endServidor = "https://flavio2m.pythonanywhere.com/discentes/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.list = findViewById(R.id.lista);
        this.btNovo = findViewById(R.id.btNovo);
        this.btEditar = findViewById(R.id.btEditar);
        this.btExcluir = findViewById(R.id.btExcluir);
        this.btAtualizar = findViewById(R.id.btAtualizar);

        atualizaListaDiscentes();
        this.adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, relacaoDiscentes);
        this.list = (ListView) findViewById(R.id.lista);
        this.list.setAdapter(adapter);
        this.list.setOnItemClickListener(escutaLista);

        this.btNovo.setOnClickListener(this);
        this.btEditar.setOnClickListener(this);
        this.btExcluir.setOnClickListener(this);
        this.btAtualizar.setOnClickListener(this);

        System.out.println(relacaoDiscentes);
        System.out.println(listDiscentes);

    }

    /*
        Thread para fazer o download das informações sem travar a tela do usuário
        Artigo com excelente explicações:
            https://www.devmedia.com.br/trabalhando-com-asynctask-no-android/33481
     */
    class DownloadJsonAsyncTask extends AsyncTask<String, Void, ArrayList<Discente>> {
        String msgExclusao = "";

        //Exibe pop-up indicando que está sendo feito o download do JSON
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Verifica se eh exclusão e atualização da lista
            if(excluirDiscente)
                Toast.makeText(getBaseContext(), "Excluindo discentes...", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getBaseContext(), "Fazendo download da lista de Discentes!", Toast.LENGTH_LONG).show();
        }

        //Acessa o serviço do JSON e retorna a lista de discentes
        @Override
        protected ArrayList<Discente> doInBackground(String... params) {
            //params[0] é o parâmetro String da classe
            String urlString = params[0];
            try {

                //verifica se é preciso excluir discente e antes de atualizar a lista
                if(excluirDiscente){

                    //O webservice retorna um json com o objeto excluído ou um json vazio se houve erro
                    String urlExcluir = endServidor + listDiscentes.get(selecionado).getId() + "/";
                    System.out.println(urlExcluir);

                    URL url = new URL(urlExcluir);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("DELETE");
                    connection.connect();

                    //response 204 No Content = excluído com sucesso ou retorna algum código de erro
                    int responseCode = connection.getResponseCode();
                    System.out.println(responseCode);
                    if (responseCode == 204)
                        msgExclusao = "Discente excluído com sucesso!";

                    else
                        msgExclusao = "Houve algum erro ao excluir o discente: responseCode = " + responseCode;

                    //finaliza conexão
                    connection.disconnect();

                    //Remove da lista também
                    listDiscentes.remove(selecionado);

                    selecionado = -1;
                    excluirDiscente = false;

                }

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.connect();
                String jsonResposta = new Scanner(connection.getInputStream()).nextLine();

                ArrayList listDiscentes = getDiscente(jsonResposta);

                //resposta do servidor
                System.out.println(connection.getResponseCode());

                //finaliza conexão
                connection.disconnect();

                //como utilizar esse retorno?
                return listDiscentes;


            } catch (Exception e) {
                System.out.println(e.toString());
                return new ArrayList<Discente>();
            }
        }

        private ArrayList<Discente> getDiscente(String jsonResposta) {
            ArrayList<Discente> listDiscentes = new ArrayList<Discente>();
            try {

                JSONArray discentesJson = new JSONArray(jsonResposta);
                JSONObject discente;

                //percorre a lista de pessoas no formato json
                for (int i = 0; i < discentesJson.length(); i++) {

                    discente = new JSONObject(discentesJson.getString(i));

                    Discente objDiscente = new Discente();
                    objDiscente.setId(discente.getInt("id"));
                    objDiscente.setNome(discente.getString("nome"));
                    objDiscente.setNumero(discente.getString("numero"));
                    objDiscente.setEmail(discente.getString("email"));
                    objDiscente.setCurso(discente.getString("curso"));
                    objDiscente.setAno(discente.getString("ano"));
                    listDiscentes.add(objDiscente);

                    System.out.println(discente);
                }

            } catch (JSONException e) {
                System.out.println(e.toString());

            }

            return listDiscentes;
        }

        //Depois de executada a chamada do serviço
        @Override
        protected void onPostExecute(ArrayList<Discente> result) {
            //Atualiza o adapter (lista dos discentes)
            //Por rodar em uma thread, as vezes demora a ser executado e quando conclui já já concluiu
            //o método atualizarlizarListaDiscentes()
            adapter.clear();
            relacaoDiscentes = new ArrayList<>();
            for (Discente d : listDiscentes){
                relacaoDiscentes.add(String.format("id:%s  %s", d.getId(), d.getNome()));
            }
            adapter.addAll(relacaoDiscentes);
            adapter.notifyDataSetChanged();


            if (!msgExclusao.isEmpty())
                Toast.makeText(getBaseContext(), msgExclusao.toString() + " Lista de Discentes atualizada!", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getBaseContext(), "Lista de Discentes atualizada!", Toast.LENGTH_SHORT).show();

        }
    }

    public void atualizaListaDiscentes(){
        try{
            //Atualiza vetor com relação dos discentes no webservice
            //Passa o parâmetro format=json
            listDiscentes = new DownloadJsonAsyncTask().execute(endServidor).get();

        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }

    private AdapterView.OnItemClickListener escutaLista = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int posicao, long id) {
            if (posicao > -1) {

                //atualiza o discente selecionado posição do selecionado
                Discente discente = listDiscentes.get(posicao);
                selecionado = posicao;

                String saida = String.format(" ID.:%s \n Nome.: %s \n E-mail: %s \n Matrícula.: %s \n" +
                                             " Curso: %s \n Ano.: %s \n", discente.getId(),
                        discente.getNome(), discente.getEmail(), discente.getNumero(),
                        discente.getCurso(), discente.getAno());

                Toast.makeText(getBaseContext(),saida,Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(getBaseContext(),"clique num discente",Toast.LENGTH_SHORT).show();
        };
    };

    @Override
    public void onClick(View view) {
        if (view == btNovo){
            Intent it = new Intent(getBaseContext(),AddDiscente.class);
            startActivity(it);
        }
        else if (view == this.btEditar){

            if (selecionado > 0) {
                Discente d = listDiscentes.get(this.selecionado);
                Intent it = new Intent(getBaseContext(), AddDiscente.class);
                Bundle bundle = new Bundle();
                bundle.putInt("id", (int) d.getId());
                bundle.putString("nome", d.getNome());
                bundle.putString("email", d.getEmail());
                bundle.putString("numero", d.getNumero());
                bundle.putString("curso", d.getCurso());
                bundle.putString("ano", d.getAno());
                it.putExtras(bundle);
                startActivity(it);
            }
            else
                Toast.makeText(getBaseContext(), "Clique sobre o discente que quer editar.", Toast.LENGTH_SHORT).show();
        }
        else if(view == btExcluir){
            if(selecionado >= 0){
                AlertDialog alerta;

                //Cria o gerador do AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                //define o titulo
                builder.setTitle("Atenção");

                //define a mensagem
                builder.setMessage("Confirma a exclusão do discente " + relacaoDiscentes.get(selecionado).toString() + "?");

                //define um botão como positivo
                builder.setPositiveButton("Positivo", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        //defini que tem aluno para excluir
                        excluirDiscente = true;

                        //chama método para atualizar lista (com exclusão)
                        atualizaListaDiscentes();
                    }
                });
                //define um botão como negativo.
                builder.setNegativeButton("Negativo", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

                //cria o AlertDialog
                alerta = builder.create();
                //Exibe
                alerta.show();

            }
            else
                Toast.makeText(getBaseContext(), "Clique sobre o discente que quer editar.", Toast.LENGTH_SHORT).show();
        }
        else if (view == btAtualizar){
            this.atualizaListaDiscentes();
        }
    }
}
