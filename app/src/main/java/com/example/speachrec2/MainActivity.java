package com.example.speachrec2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest.permission;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Button startB;
    private TextView resTxt;
    private SpeechRecognizer speechRecognizer;
    private Intent intent;
    private Context context=this;
    private Map<String,Contact> contactMap ;

    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ActivityCompat.requestPermissions(this, new String[] {permission.RECORD_AUDIO, permission.CALL_PHONE ,  permission.READ_CONTACTS}, PackageManager.PERMISSION_GRANTED);
        if (ContextCompat.checkSelfPermission(this, permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this, permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this, permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED
        ){

            Toast.makeText(context, "the app will not work properly without this permissions please press allow", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[] {permission.RECORD_AUDIO, permission.CALL_PHONE ,  permission.READ_CONTACTS}, PackageManager.PERMISSION_GRANTED);
        }


        startB = findViewById(R.id.startButton);
        resTxt = findViewById(R.id.resText);

        setUpSpeachRecognition();
        loadContacts();

        startB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speechRecognizer.startListening(intent);

            }
        });
    }
    private void setUpSpeachRecognition(){
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "he-IL");

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
                speechRecognizer.stopListening();
            }

            @Override
            public void onError(int i) {
                speechRecognizer.stopListening();
            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> resList = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                resTxt.setText(resList.get(0));
                String [] res = resList.get(0).split(" ");

                /* lokking for the root קשר in the spoken text*/
                for (int i = 0; i <res.length-1 ; i++) {
                    if (res[i].contains("קשר")){
                        String callTo = searchContact(res[i+1]);
                        if (callTo ==null)callTo = searchContact(res[i+1].substring(1));
                        Toast.makeText(MainActivity.this, contactMap.get(callTo).getNumbers().get(0), Toast.LENGTH_LONG).show();
                        call(contactMap.get(callTo).getNumbers().get(0));
                    }
                }
                speechRecognizer.stopListening();
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });





    }
    private void loadContacts(){
        if (contactMap == null)contactMap = new HashMap<>();
        List<String> nameList= new ArrayList<>();
        List<String> numberList= new ArrayList<>();
        Cursor androidContacts =null;
        Cursor numberCursor =null;
        ContentResolver contentResolver = getContentResolver();
        try {
            androidContacts = contentResolver.query( ContactsContract.Contacts.CONTENT_URI, null, null, null);
        } catch (EnumConstantNotPresentException e){
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }

        while (androidContacts!=null&&androidContacts.moveToNext()){
            String contentName = androidContacts.getString(androidContacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            nameList.add(contentName);
            String contactId = androidContacts.getString(androidContacts.getColumnIndex(ContactsContract.Contacts._ID));
            Contact contact = new Contact(contentName, contactId);
            contactMap.put(contentName, contact);
            if (androidContacts.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER ) > 0){
                numberCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?" ,new String []{contactId}, null);
                while (numberCursor!=null&&numberCursor.moveToNext()){
                    String number = numberCursor.getString(numberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    contact.addNumber(number);
                }
            }
        }
        Toast.makeText(context, " contacts loaded",Toast.LENGTH_SHORT).show();
        androidContacts.close();
        numberCursor.close();
    }
    private String searchContact(String name ){
        String[] nameParts = name.split(" ");
        for (String part : nameParts) {
            for (String contactName : contactMap.keySet()) {
                if (contactName!=null && contactName.contains(part)){
                    return contactName;
                }
            }
        }
        return null;
    }
    private void call (String number){
        /* checking if the app have calling permission*/
        if (ContextCompat.checkSelfPermission(this, permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
            Toast.makeText(context, " no permeation",Toast.LENGTH_SHORT).show();
            return;
        }
        number = number.trim();
        String dial = "tel:"+number;
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
    }

}
