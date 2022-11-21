package com.example.meganv2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.ImageView;


import java.sql.Date;
import java.text.BreakIterator;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, View.OnClickListener { //Aqui la app se implementa la API de voz.

    public static final int CODIGO_SELECCIONAR_ARCHIVO  = 1944;
    public static final int CODIGO_GALERIA_FOTOS        = 1945;

    private EditText edtTexto;
    private EditText edtLatitud;
    private EditText edtLongitud;
    private View     layout;
    private String   texto = "";
    private static final int RECONOCEDOR_VOZ = 7; //se crea un objeto con el reconcedor de voz con cualquier numero.
    private TextView escuchando; // se crea el objeto con el textview que se trabajo.
    private TextView respuesta; // tambien se crea el textview pero esta vez de la respuesta donde aparecera el texto.
    private ArrayList<Respuestas> respuest; // se crea un objeto array.
    private TextToSpeech leer; // aqui se implementa un objeto usando la api para leer lo que se le dira.
    Button btnCamara;
    ImageView imgView;
    private TelephonyManager mTelephonyManager;
    private Object view;




    @Override
    protected void onCreate(Bundle savedInstanceState) { //actividad principal donde se inicializa el codigo.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inicializar();
        getId();
        btnCamara = findViewById(R.id.btnCamara);
        imgView = findViewById(R.id.imageView);
        Button mDialButton = (Button) findViewById(R.id.btn_dial);
        final EditText mPhoneNoEt = (EditText) findViewById(R.id.et_phone_no);
        mTelephonyManager = (TelephonyManager) getSystemService(getApplicationContext().TELEPHONY_SERVICE);

        btnCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirCamara();
            }
        });

        mDialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNo = mPhoneNoEt.getText().toString();
                if(!TextUtils.isEmpty(phoneNo)) {
                    String dial = "tel:" + phoneNo;
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(dial)));
                }else {
                    Toast.makeText(MainActivity.this, "Enter a phone number", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void getId(){
        Button button_send = findViewById(R.id.button_send);
        button_send.setOnClickListener( this);
    }


    @SuppressLint("SetTextI18n")
    public void onClick(View view){
        switch (view.getId()){
            case R.id.button_send:
                TextView tv=findViewById(R.id.tv);
                @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter   =   new   SimpleDateFormat   ("HH:mm:ss");
                Date curDate =  new Date(System.currentTimeMillis());
                // Obtener la hora actual
                String   str   =   formatter.format(curDate);
                tv.setText(str);
        }
    }

    // En el método onResume(), podemos empezar a escuchar utilizando el método listen() del TelephonyManager,
    // pasándole la instancia de PhoneStateListener y el static LISTEN_CALL_STATE.
    // Paramos de escuchar en el método onStop(), pasando LISTEN_NONE como segundo argumento de listen().
    @Override
    protected void onResume() {
        super.onResume();
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }
    @Override
    protected void onStop() {
        super.onStop();
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
    }
// ...


    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    Toast.makeText(MainActivity.this, "CALL_STATE_IDLE", Toast.LENGTH_SHORT).show();
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Toast.makeText(MainActivity.this, "CALL_STATE_RINGING", Toast.LENGTH_SHORT).show();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Toast.makeText(MainActivity.this, "CALL_STATE_OFFHOOK", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == RECONOCEDOR_VOZ){ // este es el reconocedor de voz.
            ArrayList<String> reconocido = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String escuchado = reconocido.get(0); // aqui mismo se inicializa en cero como variable principal.
            escuchando.setText(escuchado);
            prepararRespuesta(escuchado);

        }

        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imgBitmap = (Bitmap) extras.get("data");
            imgView.setImageBitmap(imgBitmap);
        }
    }


    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 1);
        }
    }

    private void prepararRespuesta(String escuchado) { //aqui se prepara la respuesta despues de que la API la escucha
        String normalizar = Normalizer.normalize(escuchado, Normalizer.Form.NFD);
        String sintilde = normalizar.replaceAll("[^\\p{ASCII}]", ""); // en caso de que usen acentos.

        int resultado;
        String respuesta = respuest.get(0).getRespuesta();
        for (int i = 0; i < respuest.size(); i++){
            resultado = sintilde.toLowerCase().indexOf(respuest.get(i).getCuestion());
            if(resultado != -1){
                respuesta = respuest.get(i).getRespuesta();
                if(!operacion(respuest.get(i).getCuestion(), sintilde).equals("")){
                    respuesta = respuesta + operacion(respuest.get(i).getCuestion(), sintilde);

                }
            }
        }

        accionesExtra(respuesta);
        responder(respuesta);
        //ESTE METODO LO QUE HACE ES ESCUCHAR Y REALIZAR LAS ACCIONES
}
    
    /* METODO PARA REALIZAR ACCIONES ADEMAS DE RESPONDER <- RECIBE MISMO PARAMETRO DE RESPUESTA*/ 
    private void accionesExtra ( String accion ) {
        if ( accion.equals( "abriendo camara" ) ){
            abrirCamara();
        } // METODO HECHO PARA PODER AGREGAR MAS ACCIONES FUTURAS   
    }

    private String operacion(String cuestion, String escuchado) { //se puso este metodo par poner case if con las
        String rpta = "";                                         // 4 operaciones basicas .
        if(cuestion.equals("mas") || cuestion.equals("menos") || cuestion.equals("por") || cuestion.equals("entre")){
            rpta = operaciones(cuestion,escuchado); //respuesta devuelve cuestion y el escuchado de la API.

        }
        return rpta;

    }

    private String operaciones(String operador, String numeros) { // este metodo sirve para las operaciones
        String rpta = " ";                                        // aritmeticas basicas que resolvera el asistente.
        double respuesta = 0;
        String errorDivision = "";
        String[] numero = numeros.split(operador);
        double num1 = obtenerNumero(numero[0]); //primer numero de tipo doble.
        double num2 = obtenerNumero(numero[1]); // segunda variable numero de tipo double.
        switch (operador){
            case "mas":
            case "+":
                respuesta = num1 + num2; // aqui resuelve el metodo de suma.
            case "menos":
                respuesta = num1 - num2; // metodo de la resta en las operaciones.
                break;
            case "por":
                respuesta = num1 * num2; // metodo de multiplicacion.
                break;
            case "entre":
                if (num1 > 0){
                    respuesta = num1 / num2; //metodo de la multiplicacion con un metodo if.
                }else{
                    errorDivision = "ERROR: El primero numero numero no puedo ser menor a 0";//en caso de dar error.

                }
                break;
        }
        rpta = String.valueOf(respuesta) + errorDivision;
        return rpta;
    }

    private double obtenerNumero(String cadena){ //aqui metodo donde se obtiene un numero donde indica si es digito o no.
        double num; //variable numero de tipo doble.
        String n = "";
        char[] numero = cadena.toCharArray();
        for (int i=0; i<numero.length; i++){
            if(Character.isDigit(numero[i])){
                n = n + String .valueOf(numero[i]);
            }
        }
        num = Double.parseDouble(n);
        return num; //regresa un numero.
    }

    private void responder(String respuestita) { //despues que lee la API este metodo sirve para responder.
        respuesta.setText(respuestita);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            leer.speak(respuestita, TextToSpeech.QUEUE_FLUSH, null, null);
        }else{
            leer.speak(respuestita, TextToSpeech.QUEUE_FLUSH, null);
        }

    }

    public void inicializar(){ //aqui la API(textToSpeech cuando se habla esta lee y lo adjunta en los textfield)
        escuchando = (TextView)findViewById(R.id.jTextPregunta); //Revisar esta linea tv_escuchado
        respuesta = (TextView)findViewById(R.id.jTextRespuesta); //tv_Respuesta
        respuest = proveerDatos(); //aqui se implementa el metodo de proveerdatos que se escribio en la parte de abajo.
        leer = new TextToSpeech(this,this);

        //String palabra = ((TextView) findViewById(R.id.jTextPregunta)).getText().toString();
        //if(palabra == "camera"){
           // Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //startActivity(intent);
        //}
    }

    public void hablar(View v){ // el reconocedor de voz cuando uno habla.
        Intent hablar = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        hablar.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-MX"); //idioma español.
        startActivityForResult(hablar, RECONOCEDOR_VOZ); //metodo para hablar y aqui es


    }

    public ArrayList<Respuestas> proveerDatos() //esta es una pequeña base de datos o arreglo de diccionario manual.
    {
        ArrayList<Respuestas> respuestas = new ArrayList<>();
        respuestas.add(new Respuestas("defecto", "Lo siento no te entiendo"));
        respuestas.add(new Respuestas("chiste 3", "¿Cómo se dice disparo en árabe? Ahí-va-la-bala.\n"));
        respuestas.add(new Respuestas("chiste 2", "¿Cuál es el colmo de Aladdín? Tener mal genio."));
        respuestas.add(new Respuestas("hola", "hola que tal soy Megan y sere tu Asistente Virtual"));
        respuestas.add(new Respuestas("chiste 1", "Sabias que le dijo mi mama"));
        respuestas.add(new Respuestas("Hola Megan", "Hola que Tal si Necesitas algo solo dimelo"));
        respuestas.add(new Respuestas("Quien te desarrolló", "La empresa de SOFTGEM son mis desarrolladores"));
        respuestas.add(new Respuestas("adios", "Adios, cuidate mucho"));
        respuestas.add(new Respuestas("Megan me puedes ayudar con unos problemas", "Claro que si sera un placer ayudarte en lo que sea"));
        respuestas.add(new Respuestas("como estas", "muy bien y tu?"));
        respuestas.add(new Respuestas("por ", "la respuesta de la multiplicacion es"));
        respuestas.add(new Respuestas("entre ", "La respuesta de la division es" ));
        respuestas.add(new Respuestas("menos ", "La respuesta de la resta es" ));
        respuestas.add(new Respuestas("mas ", "La respuesta de la suma es" ));
        respuestas.add(new Respuestas("Abrir camara ", "abriendo camara" ));

        return respuestas;
    }


    @Override
    public void onInit(int status) {


    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
