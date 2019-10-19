package com.appwebserviceclient;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by flavio2m@gmail.com on 01/10/2019
 */
public class AddDiscente extends AppCompatActivity implements View.OnClickListener {
    EditText tNome, tNumero, tEmail, tAno, tCurso, tId;
    Discente discente;
    Button bConfirma;
    Button bCancela;
    boolean novoDiscente = false;

    //Obs: não pode utilizar 127.0.0.1:xx ou localhost:xx porque é o endereço do dispositovo com android
    //Se utilizar a porta 80 não necesidade de especificar (está implícita)
    String endServidor = "https://flavio2m.pythonanywhere.com/discentes/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_discente);

        this.tNumero = findViewById(R.id.tnumero);
        this.tNome = findViewById(R.id.tnome);
        this.tEmail = findViewById(R.id.temail);
        this.tAno = findViewById(R.id.tano);
        this.tCurso = findViewById(R.id.tcurso);
        this.tId = findViewById(R.id.tid);
        this.bConfirma = findViewById(R.id.btnconfirma);
        this.bCancela = findViewById(R.id.btncancela);


        //Método que atualiza os dados caso seja edição
        atualizaDados();

        bConfirma.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                discente.setNome(tNome.getText().toString());
                discente.setEmail(tEmail.getText().toString());
                discente.setAno(tAno.getText().toString());
                discente.setCurso(tCurso.getText().toString());
                discente.setNumero(tNumero.getText().toString());

                if(novoDiscente)
                    new UploadJsonAsyncTask().execute(endServidor);
                else{
                    //define o id (já possui cadastro)
                    discente.setId(Integer.parseInt(tId.getText().toString()));
                    new UploadJsonAsyncTask().execute(endServidor+ discente.getId() + "/");
                }

                Intent t = new Intent(getBaseContext(), MainActivity.class);
                startActivity(t);
            }
        });

        bCancela.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Cancelado!", Toast.LENGTH_SHORT).show();
                Intent t = new Intent(getBaseContext(), MainActivity.class);
                startActivity(t);
            }
        });
    }

    private void atualizaDados() {
        this.discente = new Discente();
        Bundle params = getIntent().getExtras();

        if (params != null) {
            this.tId.setText(Integer.toString(params.getInt("id")));
            this.tNome.setText(params.getString("nome"));
            this.tEmail.setText(params.getString("email"));
            this.tNumero.setText(params.getString("numero"));
            this.tCurso.setText(params.getString("curso"));
            this.tAno.setText(params.getString("ano"));
        }
        else
            this.novoDiscente = true;

    }

    @Override
    public void onClick(View view) {

    }

    class UploadJsonAsyncTask extends AsyncTask<String, Void, ArrayList<Discente>> {
        String msgRetorno;

        //Exibe pop-up indicando que está sendo feito o download do JSON
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getBaseContext(), "Enviado cadastro para o servidor...", Toast.LENGTH_SHORT).show();
        }

        //Acessa o serviço do JSON e retorna a lista de discentes
        @Override
        protected ArrayList<Discente> doInBackground(String... params) {
            //params[0] é o parâmetro String da classe
            String urlString = params[0];
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                if (novoDiscente)
                    connection.setRequestMethod("POST");
                else
                    connection.setRequestMethod("PATCH");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept-Charset", "UTF-8");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.connect();

                JSONObject jsonObject= new JSONObject();
                jsonObject.put("nome", discente.getNome());
                jsonObject.put("email", discente.getEmail());
                jsonObject.put("numero", discente.getNumero());
                jsonObject.put("curso", discente.getCurso());
                jsonObject.put("ano", discente.getAno());

                //Se é atualização de cadastro, define o ID. Se for cadastro novo o banco gera a sequência
                if (!novoDiscente)
                    jsonObject.put("id", discente.getId());

                System.out.println(jsonObject.toString());

                DataOutputStream os = new DataOutputStream(connection.getOutputStream());
                os.writeBytes(String.valueOf(jsonObject));
                os.flush();
                os.close();

                //Verifica a resposta do webservice
                //200 OK = atualizado com sucesso
                //201 Created = cadastrado com sucesso
                int responseCode = connection.getResponseCode();
                System.out.println(responseCode);
                if (responseCode == 201)
                    msgRetorno = "Discente atualizado com sucesso!";
                else if(responseCode == 200)
                    msgRetorno = "Discente cadastrado com sucesso!";
                else
                    msgRetorno = "Erro ao enviar dados para o servidor: " + responseCode;

                //fecha conexão
                connection.disconnect();

            } catch (Exception e) {
                System.out.println(e.toString());
            }
            return null;
        }

        private ArrayList<Discente> getDiscente(String jsonResposta) {
            ArrayList<Discente> listDiscentes = new ArrayList<Discente>();
            try {

                JSONArray discentesJson = new JSONArray(jsonResposta);
                JSONObject discente;

            } catch (JSONException e) {
                Log.e("Erro", "Erro no parsing do JSON", e);
            }

            return listDiscentes;
        }

        //Depois de executada a chamada do serviço
        @Override
        protected void onPostExecute(ArrayList<Discente> discentes) {
            Toast.makeText(getBaseContext(), msgRetorno, Toast.LENGTH_SHORT).show();
        }

    }

}
